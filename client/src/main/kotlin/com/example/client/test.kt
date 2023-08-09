package com.example.client

import io.netty.channel.Channel
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.handler.codec.http.FullHttpResponse
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker
import io.netty.handler.codec.http.websocketx.WebSocketFrame


class test {
}


class WebSocketClientHandler(private val handshaker: WebSocketClientHandshaker) : SimpleChannelInboundHandler<Any?>() {
    @Throws(Exception::class)
    override fun channelActive(ctx: ChannelHandlerContext) {
        handshaker.handshake(ctx.channel())
    }

    @Throws(Exception::class)
    override fun channelRead0(ctx: ChannelHandlerContext, msg: Any?) {
        val ch: Channel = ctx.channel()
        if (!handshaker.isHandshakeComplete) {
            handshaker.finishHandshake(ch, msg as FullHttpResponse?)
            println("WebSocket Client connected!")
            return
        }
        if (msg is WebSocketFrame) {
            val frame = msg
            if (frame is TextWebSocketFrame) {
                println("Received message: " + frame.text())
            } else if (frame is CloseWebSocketFrame) {
                println("WebSocket Client received closing")
                ch.close()
            }
        }
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        cause.printStackTrace()
        if (!handshaker.isHandshakeComplete) {
            println("Handshake failed: " + cause.message)
        }
        ctx.close()
    }
}