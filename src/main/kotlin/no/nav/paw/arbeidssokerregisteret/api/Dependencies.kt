package no.nav.paw.arbeidssokerregisteret.api

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry
import no.nav.paw.arbeidssokerregisteret.api.config.Config
import no.nav.paw.arbeidssokerregisteret.api.config.createKafkaConsumerConfig
import no.nav.paw.arbeidssokerregisteret.api.kafka.consumers.ArbeidssokerperiodeConsumer
import no.nav.paw.arbeidssokerregisteret.api.repositories.ArbeidssokerperiodeRepository
import no.nav.paw.arbeidssokerregisteret.api.services.ArbeidssokerperiodeService
import no.nav.paw.arbeidssokerregisteret.api.utils.generateDatasource
import org.jetbrains.exposed.sql.Database
import javax.sql.DataSource

fun createDependencies(config: Config): Dependencies {
    val registry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)
    val dataSource = generateDatasource(config.database.url)
    val objectMapper = jacksonObjectMapper().apply {
        registerKotlinModule()
        disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        registerModule(JavaTimeModule())
    }

    val database = Database.connect(dataSource)

    // Arbeidssøkerperiode avhengigheter
    val arbeidssokerperiodeRepository = ArbeidssokerperiodeRepository(database)
    val arbeidssokerperiodeService = ArbeidssokerperiodeService(arbeidssokerperiodeRepository)
    val arbeidssokerperiodeConsumer = ArbeidssokerperiodeConsumer(
        config.kafka.consumers.arbeidssokerperioder.topic,
        createKafkaConsumerConfig(config.kafka),
        arbeidssokerperiodeService,
        objectMapper
    )

    return Dependencies(
        registry,
        dataSource,
        arbeidssokerperiodeService,
        objectMapper,
        arbeidssokerperiodeConsumer
    )
}

data class Dependencies(
    val registry: PrometheusMeterRegistry,
    val dataSource: DataSource,
    val arbeidssokerperiodeService: ArbeidssokerperiodeService,
    val objectMapper: ObjectMapper,
    val arbeidssokerperiodeConsumer: ArbeidssokerperiodeConsumer
)
