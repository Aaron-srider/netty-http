package com.example.base

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ByteToMessageDecoder
import io.netty.handler.codec.MessageToByteEncoder
import java.util.*

class Protocol {
}


class ProtocolDecoder : ByteToMessageDecoder() {
    override fun decode(ctx: ChannelHandlerContext, byteBuf: ByteBuf, out: MutableList<Any>) {
        if (byteBuf.isReadable(ProtocolMessage.headerLen())) {
            val bodyLen = getProtocolMessageBodyLength(byteBuf)
            val totalLen = ProtocolMessage.headerLen() + bodyLen;
            if (byteBuf.isReadable(totalLen)) {
                val data = ByteArray(totalLen)
                byteBuf.readBytes(data)
                val protocolMessage = ProtocolMessage()
                protocolMessage.fromBytes(data)
                out.add(protocolMessage)
            }
        }
    }

    fun getProtocolMessageBodyLength(byteBuf: ByteBuf): Int {
        byteBuf.markReaderIndex()
        byteBuf.skipBytes(ProtocolMessage.skipForMessageLen());
        val length = byteBuf.readInt()
        byteBuf.resetReaderIndex()
        return length
    }
}


class ProtoEncoder : MessageToByteEncoder<ProtocolMessage>() {
    override fun encode(ctx: ChannelHandlerContext, msg: ProtocolMessage, out: ByteBuf) {
        var data: ByteArray = msg.toBytes()
        out.writeBytes(data)
    }
}

class ProtocolMessage : ModelSerialization {
    companion object {
        fun headerLen(): Int {
            return 25;
        }

        fun skipForMessageLen(): Int {
            return 9;
        }
    }


    // ======================   Header of protocol message, protocol related   ======================
    var version: Byte? = null
    var messageId: Long? = null
    var messageLength: Int? = null
    var receiveId: ByteArray? = null
    var sendId: ByteArray? = null
    var messageType: Int? = null
    // ======================   Body of protocol message   ======================
    /**
     * PDU
     */
    var body: ByteArray? = null

    override fun fromBytes(data: ByteArray) {
        var buf = Unpooled.buffer();
        buf.writeBytes(data)
        version = buf.readByte();
        messageId = buf.readLong();
        messageLength = buf.readInt();
        receiveId = ByteArray(4)
        buf.readBytes(receiveId)
        sendId = ByteArray(4)
        buf.readBytes(sendId)
        messageType = buf.readInt();
        body = ByteArray(messageLength!!)
        buf.readBytes(body)
    }

    override fun toBytes(): ByteArray {
        var buf = Unpooled.buffer();
        buf.writeByte(version!!.toInt())
        buf.writeLong(messageId!!)
        buf.writeInt(messageLength!!)
        buf.writeBytes(receiveId)
        buf.writeBytes(sendId)
        buf.writeInt(messageType!!)
        buf.writeBytes(body)
        return Arrays.copyOf(buf.array(), buf.writerIndex())
    }
}


class MessageType {
    companion object {
        val SERVER_GREET = 0x01
        val CLIENT_SMELL = 0x02

    }
}