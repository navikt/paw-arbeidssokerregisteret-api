package no.nav.paw.arbeidssokerregisteret.api

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

    val database = Database.connect(dataSource)

    // Arbeidssøkerperiode avhengigheter
    val arbeidssokerperiodeRepository = ArbeidssokerperiodeRepository(database)
    val arbeidssokerperiodeService = ArbeidssokerperiodeService(arbeidssokerperiodeRepository)
    val arbeidssokerperiodeConsumer = ArbeidssokerperiodeConsumer(
        config.kafka.consumers.arbeidssokerperioder.topic,
        createKafkaConsumerConfig(config.kafka),
        arbeidssokerperiodeService
    )

    return Dependencies(
        registry,
        dataSource,
        arbeidssokerperiodeService,
        arbeidssokerperiodeConsumer
    )
}

data class Dependencies(
    val registry: PrometheusMeterRegistry,
    val dataSource: DataSource,
    val arbeidssokerperiodeService: ArbeidssokerperiodeService,
    val arbeidssokerperiodeConsumer: ArbeidssokerperiodeConsumer
)
