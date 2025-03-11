package no.nav.paw.arbeidssoekerregisteret.app

import io.confluent.kafka.schemaregistry.avro.AvroSchema
import io.confluent.kafka.schemaregistry.avro.AvroSchemaProvider
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClientFactory
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.apache.avro.Schema
import org.slf4j.LoggerFactory
import java.time.Duration
import java.util.concurrent.CompletableFuture
import kotlin.system.exitProcess


val logger = LoggerFactory.getLogger("app")
fun main() {
    logger.info("Starter")
    val client = SchemaRegistryClientFactory.newClient(
        listOf(System.getenv(SCHEMA_REG_URL)),
        10,
        listOf(AvroSchemaProvider().also { it.configure(schemaRegistryProperties) }),
        schemaRegistryProperties,
        emptyMap()
    )
    val result = client.uploadSchemas()
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
    logger.info("Opplasting ferdig: http kode ${result.get()}")
    if (result.get() == HttpStatusCode.OK) {
        exitProcess(0)
    } else {
        //Må vente lenge nok til at deploy steget gir opp og feiler
        Thread.sleep(Duration.ofMinutes(11).toMillis())
        exitProcess(1)
    }
}

fun SchemaRegistryClient.uploadSchemas(): CompletableFuture<HttpStatusCode> =
    CompletableFuture.supplyAsync {
        subjectMap
            .onEach { (subject, _) ->
                runCatching { setFullTransitiveCompatibility(subject) }
                    .onFailure { logger.error("Failed to set compatibility for $subject", it) }
                    .getOrThrow()
            }
            .map{(subject, schema) -> uploadSchema(schema, subject)}
            .firstOrNull { it != HttpStatusCode.OK }
            ?: HttpStatusCode.OK
    }

fun SchemaRegistryClient.uploadSchema(schema: Schema, subject: String): HttpStatusCode =
    runCatching {
        val avroSchema = AvroSchema(schema)
        registerWithResponse(subject, avroSchema, true)
    }
        .onFailure { logger.error("Failed to upload schema for $subject", it) }
        .onSuccess { logger.info("Schema uploaded for $subject") }
        .map { HttpStatusCode.OK }
        .getOrElse { HttpStatusCode.InternalServerError }

fun SchemaRegistryClient.setFullTransitiveCompatibility(subject: String): String =
    updateCompatibility(subject, COMPATIBILITY_LEVEL_FULL_TRANSITIVE)

fun getCodeAndMsg(result: CompletableFuture<HttpStatusCode>): Pair<HttpStatusCode, String> = when {
    !result.isDone -> Pair(HttpStatusCode.ServiceUnavailable, "not done yet")
    result.isCancelled -> Pair(HttpStatusCode.InternalServerError, "Schema upload cancelled")
    else -> Pair(
        result.get(),
        if (result.get() == HttpStatusCode.OK) "Schema upload successful" else "Schema upload failed"
    )
}

