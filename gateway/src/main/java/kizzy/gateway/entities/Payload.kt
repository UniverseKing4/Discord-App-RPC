package kizzy.gateway.entities

import kizzy.gateway.entities.op.OpCode
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Payload(
    @SerialName("t")
    val t: String? = null,
    @SerialName("s")
    val s: Int? = null,
    @SerialName("op")
    val op: OpCode? = null,
)

@Serializable
data class PayloadData<T>(
    @SerialName("d")
    val d: T? = null,
)

@Serializable
data class OutgoingPayload<T>(
    @SerialName("op")
    val op: OpCode? = null,
    @SerialName("d")
    val d: T? = null,
)
