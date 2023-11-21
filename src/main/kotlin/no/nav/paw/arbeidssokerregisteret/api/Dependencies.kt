package no.nav.paw.arbeidssokerregisteret.api

import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry
import no.nav.paw.arbeidssokerregisteret.api.config.Config
import no.nav.paw.arbeidssokerregisteret.api.config.createKafkaConsumerConfig
import no.nav.paw.arbeidssokerregisteret.api.kafka.consumers.PeriodeConsumer
import no.nav.paw.arbeidssokerregisteret.api.kafka.consumers.SituasjonConsumer
import no.nav.paw.arbeidssokerregisteret.api.repositories.PeriodeRepository
import no.nav.paw.arbeidssokerregisteret.api.repositories.SituasjonRepository
import no.nav.paw.arbeidssokerregisteret.api.services.PeriodeService
import no.nav.paw.arbeidssokerregisteret.api.services.SituasjonService
import no.nav.paw.arbeidssokerregisteret.api.utils.generateDatasource
import org.jetbrains.exposed.sql.Database
import javax.sql.DataSource

fun createDependencies(config: Config): Dependencies {
    val registry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)
    val dataSource = generateDatasource(config.database.url)

    val database = Database.connect(dataSource)

    // Arbeidssøkerperiode avhengigheter
    val periodeRepository = PeriodeRepository(database)
    val periodeService = PeriodeService(periodeRepository)
    val periodeConsumer =
        PeriodeConsumer(
            config.kafka.consumers.arbeidssokerperioder.topic,
            createKafkaConsumerConfig(config.kafka),
            periodeService
        )

    // Situasjon avhengigheter
    val situasjonRepository = SituasjonRepository(database)
    val situasjonService = SituasjonService(situasjonRepository)
    val situasjonConsumer =
        SituasjonConsumer(
            config.kafka.consumers.arbeidssokersituasjon.topic,
            createKafkaConsumerConfig(config.kafka),
            situasjonService
        )

    return Dependencies(
        registry,
        dataSource,
        periodeService,
        periodeConsumer,
        situasjonService,
        situasjonConsumer
    )
}

data class Dependencies(
    val registry: PrometheusMeterRegistry,
    val dataSource: DataSource,
    val periodeService: PeriodeService,
    val periodeConsumer: PeriodeConsumer,
    val situasjonService: SituasjonService,
    val situasjonConsumer: SituasjonConsumer
)
