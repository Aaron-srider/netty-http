package com.example.server

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.*
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.codec.http.*
import io.netty.handler.logging.LoggingHandler
import io.netty.util.CharsetUtil
import mu.KotlinLogging


/**
 * Discards any incoming data.
 */
class NettyServer(private val host: String, private val port: Int) {
    private val log = KotlinLogging.logger {}
    fun run() {
        val bossGroup: EventLoopGroup = NioEventLoopGroup()
        val workerGroup: EventLoopGroup = NioEventLoopGroup()

        try {
            val bootstrap = ServerBootstrap()
            bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel::class.java)
                .childHandler(object : ChannelInitializer<SocketChannel>() {
                    @Throws(Exception::class)
                    override fun initChannel(ch: SocketChannel) {
                        ch.pipeline().addLast(
                            LoggingHandler(),
                             HttpResponseEncoder(),
                            SimpleHttpServerHandler()
                        )
                    }
                })
                .option<Int>(ChannelOption.SO_BACKLOG, 128)
                .childOption<Boolean>(ChannelOption.SO_KEEPALIVE, true)
            val channel: Channel = bootstrap.bind(8080).sync().channel()
            println("HTTP Server started on port 8080.")
            channel.closeFuture().sync()
        } finally {
            workerGroup.shutdownGracefully()
            bossGroup.shutdownGracefully()
        }
    }
}

class SimpleHttpServerHandler : SimpleChannelInboundHandler<Any>() {
    private val log = KotlinLogging.logger {}
    @Throws(java.lang.Exception::class)
    override fun channelRead0(ctx: ChannelHandlerContext, request: Any) {
        log.debug { "server read msg, request: ${request}" }
        val content = "Hello, this is a simple HTTP server using Netty!"
        val response = DefaultFullHttpResponse(
            HttpVersion.HTTP_1_1,
            HttpResponseStatus.OK,
            ctx.alloc().buffer().writeBytes(content.toByteArray(CharsetUtil.UTF_8))
        )
        response.headers().set("Content-Type", "text/plain; charset=UTF-8")
        response.headers().set("Content-Length", response.content().readableBytes())
        ctx.writeAndFlush(response)
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        cause.printStackTrace()
        ctx.close()
    }
}