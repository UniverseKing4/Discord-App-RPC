package rpc.gateway.entities.op

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

class OpCodeSerializer : KSerializer<OpCode> {
    override val descriptor: SerialDescriptor
        get() = PrimitiveSerialDescriptor("OpCode", PrimitiveKind.INT)

    override fun deserialize(decoder: Decoder): OpCode {
        val opCode = decoder.decodeInt()
        return opCodeMap[opCode] ?: OpCode.UNKNOWN
    }

    override fun serialize(encoder: Encoder, value: OpCode) {
        encoder.encodeInt(value.value)
    }

    companion object {
        /** Pre-built O(1) lookup map instead of linear scan on every message. */
        private val opCodeMap: Map<Int, OpCode> =
            OpCode.entries.associateBy { it.value }
    }
}