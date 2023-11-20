package no.nav.paw.arbeidssokerregisteret.api

import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry
import no.nav.paw.arbeidssokerregisteret.api.config.Config
import no.nav.paw.arbeidssokerregisteret.api.config.createKafkaConsumerConfig
import no.nav.paw.arbeidssokerregisteret.api.kafka.consumers.ArbeidssoekerperiodeConsumer
import no.nav.paw.arbeidssokerregisteret.api.repositories.ArbeidssoekerperiodeRepository
import no.nav.paw.arbeidssokerregisteret.api.services.ArbeidssoekerperiodeService
import no.nav.paw.arbeidssokerregisteret.api.utils.generateDatasource
import org.jetbrains.exposed.sql.Database
import javax.sql.DataSource

fun createDependencies(config: Config): Dependencies {
    val registry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)
    val dataSource = generateDatasource(config.database.url)

    val database = Database.connect(dataSource)

    // Arbeidssøkerperiode avhengigheter
    val arbeidssoekerperiodeRepository = ArbeidssoekerperiodeRepository(database)
    val arbeidssoekerperiodeService = ArbeidssoekerperiodeService(arbeidssoekerperiodeRepository)
    val arbeidssoekerperiodeConsumer =
        ArbeidssoekerperiodeConsumer(
            config.kafka.consumers.arbeidssokerperioder.topic,
            createKafkaConsumerConfig(config.kafka),
            arbeidssoekerperiodeService
        )

    return Dependencies(
        registry,
        dataSource,
        arbeidssoekerperiodeService,
        arbeidssoekerperiodeConsumer
    )
}

data class Dependencies(
    val registry: PrometheusMeterRegistry,
    val dataSource: DataSource,
    val arbeidssoekerperiodeService: ArbeidssoekerperiodeService,
    val arbeidssoekerperiodeConsumer: ArbeidssoekerperiodeConsumer
)
