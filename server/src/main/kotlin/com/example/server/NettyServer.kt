package com.example.server

import io.netty.bootstrap.ServerBootstrap
import io.netty.buffer.Unpooled
import io.netty.channel.*
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.codec.http.*
import io.netty.handler.logging.LoggingHandler
import mu.KotlinLogging
import java.nio.charset.StandardCharsets


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
                            HttpServerCodec(),
                            SimpleHttpServerHandler()
                        )
                    }
                })
                .option<Int>(ChannelOption.SO_BACKLOG, 128)
            val channel: Channel = bootstrap.bind(8080).sync().channel()
            println("HTTP Server started on port 8080.")
            channel.closeFuture().sync()
        } finally {
            workerGroup.shutdownGracefully()
            bossGroup.shutdownGracefully()
        }
    }
}

class SimpleHttpServerHandler : SimpleChannelInboundHandler<HttpRequest>() {
    private val log = KotlinLogging.logger {}

    override fun channelInactive(ctx: ChannelHandlerContext) {
        log.debug { "HTTP client: ${ctx.channel().remoteAddress()} disconnect" }
    }

    private var requestCounter = 0

    @Throws(java.lang.Exception::class)
    override fun channelRead0(ctx: ChannelHandlerContext, request: HttpRequest?) {
        // Increment the request counter
        requestCounter++

        // log the information of the request
        log.debug { "HTTP client: ${ctx.channel().remoteAddress()} request: $request" }
        // method
        log.debug { "HTTP method: ${request?.method()}" }
        // uri
        log.debug { "HTTP uri: ${request?.uri()}" }
        // headers
        log.debug { "HTTP headers: ${request?.headers()}" }


        // Create a simple response
        val responseContent = "This is request #$requestCounter"
        val response = DefaultFullHttpResponse(
            HttpVersion.HTTP_1_1, HttpResponseStatus.OK,
            Unpooled.copiedBuffer(responseContent, StandardCharsets.UTF_8)
        )

        // Set content type and length headers
        response.headers()[HttpHeaders.Names.CONTENT_TYPE] = "text/plain; charset=UTF-8"
        response.headers()[HttpHeaders.Names.CONTENT_LENGTH] = response.content().readableBytes()

        // Keep-Alive header if the connection should be kept open
        if (HttpHeaders.isKeepAlive(request)) {
            log.debug { "HTTP request is keep alive, add header keep_alive, and just keep the connection alive" }
            response.headers()[HttpHeaders.Names.CONNECTION] = HttpHeaders.Values.KEEP_ALIVE
        }

        // Write the response and close the channel if needed
        ctx.writeAndFlush(response)
        log.debug { "Server response: ${responseContent}" }
        if (!HttpHeaders.isKeepAlive(request)) {
            log.debug { "HTTP request is keep alive, close it now" }
            ctx.close()
        }
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        cause.printStackTrace()
        ctx.close()
    }
}