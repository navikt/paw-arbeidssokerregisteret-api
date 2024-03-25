package no.nav.paw.arbeidssoekerregisteret.app

import io.confluent.kafka.schemaregistry.SchemaProvider
import io.confluent.kafka.schemaregistry.avro.AvroSchema
import io.confluent.kafka.schemaregistry.avro.AvroSchemaProvider
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClientConfig
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClientFactory
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*
import kotlinx.coroutines.*
import no.nav.paw.arbeidssokerregisteret.api.v1.Periode
import org.slf4j.LoggerFactory
import java.time.Duration
import java.util.concurrent.CompletableFuture
import kotlin.system.exitProcess


const val SCHEMA_REG_URL = "KAFKA_SCHEMA_REGISTRY"
const val SCHEMA_REG_USER = "KAFKA_SCHEMA_REGISTRY_USER"
const val SCHEMA_REG_PASSWORD = "KAFKA_SCHEMA_REGISTRY_PASSWORD"

private fun schemaRegistryConfig(): Map<String, Any> =
    mapOf(
        SchemaRegistryClientConfig.BASIC_AUTH_CREDENTIALS_SOURCE to "USER_INFO",
        SchemaRegistryClientConfig.USER_INFO_CONFIG to "${System.getenv(SCHEMA_REG_USER)}:${System.getenv(SCHEMA_REG_PASSWORD)}",
    )

fun main() {
    val logger = LoggerFactory.getLogger("app")
    logger.info("Starter")
    val client = SchemaRegistryClientFactory.newClient(
        listOf(System.getenv(SCHEMA_REG_URL)),
        10,
        listOf(AvroSchemaProvider().also { it.configure(schemaRegistryConfig()) }),
        schemaRegistryConfig(),
        emptyMap()
    )
    val result = uploadSchema(client)
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
    if (result.get() == HttpStatusCode.OK) {
        exitProcess(0)
    } else {
        Thread.sleep(Duration.ofMinutes(5).toMillis())
        exitProcess(1)
    }
}

fun uploadSchema(schemaRegistryClient: SchemaRegistryClient): CompletableFuture<HttpStatusCode> =
    CompletableFuture.supplyAsync {
        val avroSchema = AvroSchema(Periode.`SCHEMA$`)
        schemaRegistryClient.register("paw.arbeidssokerperioder-beta-v15", avroSchema, true)
        HttpStatusCode.OK
    }

fun getCodeAndMsg(result: CompletableFuture<HttpStatusCode>): Pair<HttpStatusCode, String> = when {
    !result.isDone -> Pair(HttpStatusCode.ServiceUnavailable, "not done yet")
    result.isCancelled -> Pair(HttpStatusCode.InternalServerError, "Schema upload cancelled")
    else -> Pair(
        result.get(),
        if (result.get() == HttpStatusCode.OK) "Schema upload successful" else "Schema upload failed"
    )
}

