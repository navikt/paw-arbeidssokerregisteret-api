package no.nav.paw.arbeidssoekerregisteret.app

import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import java.time.Duration
import java.util.concurrent.CompletableFuture
import kotlin.system.exitProcess


fun main() {
    val logger = LoggerFactory.getLogger("app")
    logger.info("Starter")
    val result = uploadSchema()
        .exceptionally { exception ->
            logger.error("Schema upload failed", exception)
            HttpStatusCode.InternalServerError
        }

    embeddedServer(Netty, port = 8080) {
        routing {
            get("/isAlive") {
                getCodeAndMsg(result).let { (code, msg) ->
                    call.respond(code, msg)
                }
            }
            get("/isReady") {
                getCodeAndMsg(result).let { (code, msg) ->
                    call.respond(code, msg)
                }
            }
        }
    }.start(wait = false)
    result.join()
    logger.info("Opplasting ferdig")
    Thread.sleep(Duration.ofMinutes(5).toMillis())
    logger.info("Avslutter jobb")
    exitProcess(if (result.get() == HttpStatusCode.OK) 0 else 1)
}

fun uploadSchema(): CompletableFuture<HttpStatusCode> =
    CompletableFuture.supplyAsync {
        Thread.sleep(1000)
        HttpStatusCode.InternalServerError
    }

fun getCodeAndMsg(result: CompletableFuture<HttpStatusCode>): Pair<HttpStatusCode, String> = when {
    !result.isDone -> Pair(HttpStatusCode.ServiceUnavailable, "not done yet")
    result.isCancelled -> Pair(HttpStatusCode.InternalServerError, "Schema upload cancelled")
    else -> Pair(
        result.get(),
        if (result.get() == HttpStatusCode.OK) "Schema upload successful" else "Schema upload failed"
    )
}