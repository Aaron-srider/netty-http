package com.example.client

import io.netty.bootstrap.Bootstrap
import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufAllocator
import io.netty.channel.*
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.handler.codec.http.*
import io.netty.handler.logging.LoggingHandler
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
            channel.closeFuture().addListener {
                val cause = it.cause()
                val success = it.isSuccess
                log.debug { "client close channel, success: ${success}" }
                log.debug { "client close channel, cause: ${cause}" }
                log.debug { "client close channel" }
                group.shutdownGracefully()
            }
            log.debug { "client connect to server, host: $host, port: $port" }


            for (i in 0..4) {
                val request: HttpRequest = DefaultFullHttpRequest(
                    HttpVersion.HTTP_1_1, HttpMethod.GET, uri.rawPath
                )
                val headers: HttpHeaders = DefaultHttpHeaders()
                headers["Host"] = "$host:$port"
                headers["User-Agent"] = "Netty HttpClient"
                request.headers().add(headers)

                // Send the request
                channel.writeAndFlush(request).sync()
                log.debug { "client send request to server: ${request}" }
            }

        }catch (e: Exception) {
            group.shutdownGracefully()
        }
    }
}


class SimpleHttpClientHandler : SimpleChannelInboundHandler<Any>() {
    private val alloc = ByteBufAllocator.DEFAULT
    private var content: ByteBuf? = null
    private val log = KotlinLogging.logger {}
    override fun channelRead0(ctx: ChannelHandlerContext, response: Any) {
        log.debug { "http client receive response from ${ctx.channel().remoteAddress()}" }
        log.debug { "response type: ${response.javaClass.simpleName}" }

        if(response is LastHttpContent) {
            log.debug { "last http response content" }
        }

        if(response is HttpResponse) {
            val headers = response.headers()
            log.debug { "headers: ${headers}" }
            log.debug { "headers type: ${headers.javaClass}" }
            val status = response.status()
            log.debug { "status: ${status}" }
            log.debug { "status type: ${status.javaClass}" }
            if(!status.equals(HttpResponseStatus.OK)) {
                log.debug { "response status not ok" }
            }
        }

    }

    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        cause.printStackTrace()
        ctx.close()
    }
}