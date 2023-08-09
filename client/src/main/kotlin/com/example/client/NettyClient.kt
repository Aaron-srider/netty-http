package com.example.client

import io.netty.bootstrap.Bootstrap
import io.netty.buffer.Unpooled
import io.netty.channel.*
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.handler.codec.http.*
import io.netty.handler.logging.LoggingHandler
import io.netty.util.CharsetUtil
import mu.KotlinLogging
import java.net.URI


class NettyClient {
    private val log = KotlinLogging.logger {}
    fun connect(host: String, port: Int) {
        val group: EventLoopGroup = NioEventLoopGroup()

        try {
            val uri = URI("http://localhost:8080")
            val host = uri.host
            val port = uri.port
            val bootstrap = Bootstrap()
            bootstrap.group(group)
                .channel(NioSocketChannel::class.java)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .handler(object : ChannelInitializer<SocketChannel>() {
                    @Throws(Exception::class)
                    override fun initChannel(ch: SocketChannel) {
                        ch.pipeline()
                            .addLast(
                                LoggingHandler(),
                                HttpClientCodec(),
                                SimpleHttpClientHandler()
                            )
                    }
                })
            val channel = bootstrap.connect(host, port).sync().channel()
            log.debug { "client connect to server, host: $host, port: $port" }
            val request: HttpRequest = DefaultFullHttpRequest(
                HttpVersion.HTTP_1_1, HttpMethod.GET, uri.rawPath,
                Unpooled.copiedBuffer("Hello from the client!", CharsetUtil.UTF_8)
            )
            val headers: HttpHeaders = DefaultHttpHeaders()
            headers.set("Host", "$host:$port")
            headers.set("User-Agent", "Netty HttpClient")
            request.headers().add(headers)
            channel.writeAndFlush(request)
            log.debug { "client send request to server: ${request}" }
            channel.closeFuture().sync()
        } finally {
            group.shutdownGracefully()
        }
    }
}


internal class SimpleHttpClientHandler : SimpleChannelInboundHandler<Any>() {
    private var response: HttpResponse? = null
private val log = KotlinLogging.logger {}
    @Throws(java.lang.Exception::class)
    override fun channelRead0(ctx: ChannelHandlerContext, msg: Any) {
        log.debug { "client read msg, msg: ${msg}" }
        if (msg is HttpResponse) {
            response = msg
            System.out.println("Response Status: " + response!!.status())
            System.out.println("Response Headers: " + response!!.headers())
        }
        if (msg is LastHttpContent) {
            println("Received full response content.")
            ctx.close()
        }
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        cause.printStackTrace()
        ctx.close()
    }
}