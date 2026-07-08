package rpc.gateway

import com.my.rpc.domain.interfaces.Logger
import com.my.rpc.domain.interfaces.NoOpLogger
import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import io.ktor.websocket.*
import rpc.gateway.entities.Heartbeat
import rpc.gateway.entities.Identify.Companion.toIdentifyPayload
import rpc.gateway.entities.OutgoingPayload
import rpc.gateway.entities.Payload
import rpc.gateway.entities.PayloadData
import rpc.gateway.entities.Ready
import rpc.gateway.entities.Resume
import rpc.gateway.entities.op.OpCode
import rpc.gateway.entities.op.OpCode.*
import rpc.gateway.entities.presence.Presence
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

open class DiscordWebSocketImpl(
    private val token: String,
    private val logger: Logger = NoOpLogger
) : DiscordWebSocket {
    private val gatewayUrl = "wss://gateway.discord.gg/?v=10&encoding=json"

    @Volatile
    private var websocket: DefaultClientWebSocketSession? = null

    @Volatile
    private var sequence = 0

    @Volatile
    private var sessionId: String? = null

    @Volatile
    private var heartbeatInterval = 0L

    @Volatile
    private var resumeGatewayUrl: String? = null

    private var heartbeatJob: Job? = null

    @Volatile
    private var connected = false

    @Volatile
    private var heartbeatAcknowledged = true

    private val sendMutex = Mutex()

    /** Signalled when the socket is authenticated (READY received). */
    private val connectionReady = CompletableDeferred<Unit>()

    private var client: HttpClient = HttpClient {
        install(WebSockets)
    }
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private val job = SupervisorJob()
    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Default

    override suspend fun connect() {
        coroutineContext.cancelChildren() // Cancel any previous connection attempts
        launch {
            try {
                logger.i("Gateway", "Connect called")
                val url = resumeGatewayUrl ?: gatewayUrl
                websocket = client.webSocketSession(url)

                // start receiving messages
                websocket!!.incoming.receiveAsFlow()
                    .collect {
                        when (it) {
                            is Frame.Text -> {
                                val jsonString = it.readText()
                                onMessage(jsonString)
                            }
                            else -> {}
                        }
                    }
                handleClose()
            } catch (e: Exception) {
                logger.e("Gateway", e.message ?: "")
                close()
            }
        }
    }

    /** Recoverable close codes where we should reconnect. */
    private fun isRecoverableCloseCode(code: Int): Boolean {
        return code in setOf(4000, 4001, 4002, 4003, 4005, 4007, 4009) || code in 1000..1001
    }

    private suspend fun handleClose() {
        heartbeatJob?.cancel()
        connected = false
        val close = websocket?.closeReason?.await()
        val closeCode = close?.code?.toInt() ?: -1
        logger.w(
            "Gateway", "Closed with code: ${close?.code}, " +
                    "reason: ${close?.message}, " +
                    "can_reconnect: ${isRecoverableCloseCode(closeCode)}"
        )
        if (isRecoverableCloseCode(closeCode)) {
            delay(200.milliseconds)
            connect()
        } else
            close()
    }

    private suspend fun onMessage(jsonString: String) {
        val payload = json.decodeFromString<Payload>(jsonString)
        logger.d("Gateway", "Received op:${payload.op}, seq:${payload.s}, event :${payload.t}")

        payload.s?.let {
            sequence = it
        }
        when (payload.op) {
            DISPATCH -> payload.handleDispatch(jsonString)
            HEARTBEAT -> sendHeartBeat()
            RECONNECT -> reconnectWebSocket()
            INVALID_SESSION -> handleInvalidSession()
            HELLO -> handleHello(jsonString)
            HEARTBEAT_ACK -> {
                heartbeatAcknowledged = true
                logger.d("Gateway", "Heartbeat ACK received")
            }
            else -> {}
        }
    }

    open fun Payload.handleDispatch(jsonString: String) {
        when (this.t.toString()) {
            "READY" -> {
                val ready = decodePayloadData<Ready>(jsonString) ?: return
                sessionId = ready.sessionId
                resumeGatewayUrl = ready.resumeGatewayUrl + "/?v=10&encoding=json"
                logger.i("Gateway", "resume_gateway_url updated to $resumeGatewayUrl")
                logger.i("Gateway", "session_id updated to $sessionId")
                connected = true
                connectionReady.complete(Unit)
                return
            }
            "RESUMED" -> {
                logger.i("Gateway", "Session Resumed")
                connected = true
                connectionReady.complete(Unit)
            }
            else -> {}
        }
    }

    private suspend inline fun handleInvalidSession() {
        logger.i("Gateway", "Handling Invalid Session")
        logger.d("Gateway", "Sending Identify after 150ms")
        delay(150)
        sendIdentify()
    }

    private suspend inline fun handleHello(jsonString: String) {
        if (sequence > 0 && !sessionId.isNullOrBlank()) {
            sendResume()
        } else {
            sendIdentify()
        }
        heartbeatInterval = decodePayloadData<Heartbeat>(jsonString)?.heartbeatInterval ?: return
        logger.i("Gateway", "Setting heartbeatInterval= $heartbeatInterval")
        startHeartbeatJob(heartbeatInterval)
    }

    private inline fun <reified T> decodePayloadData(jsonString: String): T? {
        return json.decodeFromString<PayloadData<T>>(jsonString).d
    }

    private suspend fun sendHeartBeat() {
        logger.i("Gateway", "Sending $HEARTBEAT with seq: $sequence")
        send(
            op = HEARTBEAT,
            d = if (sequence == 0) "null" else sequence.toString(),
        )
    }

    private suspend inline fun reconnectWebSocket() {
        websocket?.close(
            CloseReason(
                code = 4000,
                message = "Attempting to reconnect"
            )
        )
    }

    private suspend fun sendIdentify() {
        logger.i("Gateway", "Sending $IDENTIFY")
        send(
            op = IDENTIFY,
            d = token.toIdentifyPayload()
        )
    }

    private suspend fun sendResume() {
        logger.i("Gateway", "Sending $RESUME")
        send(
            op = RESUME,
            d = Resume(
                seq = sequence,
                sessionId = sessionId,
                token = token
            )
        )
    }

    private fun startHeartbeatJob(interval: Long) {
        heartbeatJob?.cancel()
        heartbeatAcknowledged = true
        heartbeatJob = launch {
            // Per Discord spec: first heartbeat after interval * jitter
            val jitter = Math.random()
            delay((interval * jitter).toLong())
            while (isActive) {
                if (!heartbeatAcknowledged) {
                    logger.w("Gateway", "No heartbeat ACK received — zombie connection detected, reconnecting")
                    reconnectWebSocket()
                    return@launch
                }
                heartbeatAcknowledged = false
                sendHeartBeat()
                delay(interval)
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun isWebSocketConnected(): Boolean {
        return websocket?.incoming != null && websocket?.outgoing?.isClosedForSend == false
    }

    private suspend inline fun <reified T> send(op: OpCode, d: T?) {
        sendMutex.withLock {
            if (websocket?.isActive == true) {
                val payload = json.encodeToString(
                    OutgoingPayload(
                        op = op,
                        d = d,
                    )
                )
                websocket?.send(Frame.Text(payload))
            }
        }
    }

    override fun close() {
        heartbeatJob?.cancel()
        heartbeatJob = null
        coroutineContext.cancelChildren()
        resumeGatewayUrl = null
        sessionId = null
        connected = false
        // Non-blocking close — schedule on a coroutine to avoid ANR
        val ws = websocket
        websocket = null
        if (ws != null) {
            CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
                try {
                    ws.close()
                } catch (_: Exception) {
                    // Already closed or failed — safe to ignore
                }
                try {
                    client.close()
                } catch (_: Exception) {
                    // Safe to ignore
                }
                logger.e("Gateway", "Connection to gateway closed")
            }
        }
    }

    override suspend fun sendActivity(presence: Presence) {
        // Wait for socket to be connected to account with a timeout instead of busy-wait
        try {
            withTimeout(30.seconds) {
                connectionReady.await()
            }
        } catch (e: TimeoutCancellationException) {
            logger.e("Gateway", "Timed out waiting for gateway connection")
            return
        }
        logger.i("Gateway", "Sending $PRESENCE_UPDATE")
        send(
            op = PRESENCE_UPDATE,
            d = presence
        )
    }

}
