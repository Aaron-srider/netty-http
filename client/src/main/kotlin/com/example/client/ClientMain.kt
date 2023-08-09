package com.example.client

import cn.hutool.core.thread.ThreadUtil
import mu.KotlinLogging
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.stereotype.Component

@SpringBootApplication
class ClientMain

fun main(args: Array<String>) {
    runApplication<ClientMain>(*args)
}
@Component
class TestRunner: CommandLineRunner {

    private val log = KotlinLogging.logger {}

    override fun run(vararg args: String?) {
        log.info { "write some here" }
        ThreadUtil.execAsync { NettyClient().connect("127.0.0.1", 8080) }
    }

}