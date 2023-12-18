package no.nav.paw.arbeidssokerregisteret.api

import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry
import no.nav.paw.arbeidssokerregisteret.api.config.Config
import no.nav.paw.arbeidssokerregisteret.api.config.createKafkaConsumer
import no.nav.paw.arbeidssokerregisteret.api.kafka.consumers.ArbeidssoekerperiodeConsumer
import no.nav.paw.arbeidssokerregisteret.api.kafka.consumers.OpplysningerOmArbeidssoekerConsumer
import no.nav.paw.arbeidssokerregisteret.api.kafka.consumers.ProfileringConsumer
import no.nav.paw.arbeidssokerregisteret.api.repositories.ArbeidssoekerperiodeRepository
import no.nav.paw.arbeidssokerregisteret.api.repositories.OpplysningerOmArbeidssoekerRepository
import no.nav.paw.arbeidssokerregisteret.api.repositories.ProfileringRepository
import no.nav.paw.arbeidssokerregisteret.api.services.ArbeidssoekerperiodeService
import no.nav.paw.arbeidssokerregisteret.api.services.AutorisasjonService
import no.nav.paw.arbeidssokerregisteret.api.services.OpplysningerOmArbeidssoekerService
import no.nav.paw.arbeidssokerregisteret.api.services.ProfileringService
import no.nav.paw.arbeidssokerregisteret.api.services.TokenService
import no.nav.paw.arbeidssokerregisteret.api.utils.generateDatasource
import no.nav.poao_tilgang.client.PoaoTilgangCachedClient
import no.nav.poao_tilgang.client.PoaoTilgangHttpClient
import org.jetbrains.exposed.sql.Database
import javax.sql.DataSource

fun createDependencies(config: Config): Dependencies {
    val registry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)
    val dataSource = generateDatasource(config.database.url)

    val database = Database.connect(dataSource)

    val tokenService = config.authProviders.find { it.name == "azure" }?.run(::TokenService) ?: throw RuntimeException("Azure provider ikke funnet")

    val poaoTilgangHttpClient =
        PoaoTilgangCachedClient(
            PoaoTilgangHttpClient(
                config.poaoClientConfig.url,
                { tokenService.createMachineToMachineToken(config.poaoClientConfig.scope) }
            )
        )
    // OBO vs StS token

    val autorisasjonService = AutorisasjonService(poaoTilgangHttpClient)

    // Arbeidssøkerperiode avhengigheter
    val arbeidssoekerperiodeRepository = ArbeidssoekerperiodeRepository(database)
    val arbeidssoekerperiodeService = ArbeidssoekerperiodeService(arbeidssoekerperiodeRepository)
    val arbeidssoekerperiodeConsumer =
        ArbeidssoekerperiodeConsumer(
            config.kafka.periodeTopic,
            config.kafka.createKafkaConsumer(),
            arbeidssoekerperiodeService
        )

    // Situasjon avhengigheter
    val opplysningerOmArbeidssoekerRepository = OpplysningerOmArbeidssoekerRepository(database)
    val opplysningerOmArbeidssoekerService = OpplysningerOmArbeidssoekerService(opplysningerOmArbeidssoekerRepository)
    val opplysningerOmArbeidssoekerConsumer =
        OpplysningerOmArbeidssoekerConsumer(
            config.kafka.opplysningerOmArbeidssoekerTopic,
            config.kafka.createKafkaConsumer(),
            opplysningerOmArbeidssoekerService
        )

    // Profilering avhengigheter
    val profileringRepository = ProfileringRepository(database)
    val profileringService = ProfileringService(profileringRepository)
    val profileringConsumer =
        ProfileringConsumer(
            config.kafka.profileringTopic,
            config.kafka.createKafkaConsumer(),
            profileringService
        )

    return Dependencies(
        registry,
        dataSource,
        arbeidssoekerperiodeService,
        arbeidssoekerperiodeConsumer,
        opplysningerOmArbeidssoekerService,
        opplysningerOmArbeidssoekerConsumer,
        profileringService,
        profileringConsumer,
        autorisasjonService
    )
}

data class Dependencies(
    val registry: PrometheusMeterRegistry,
    val dataSource: DataSource,
    val arbeidssoekerperiodeService: ArbeidssoekerperiodeService,
    val arbeidssoekerperiodeConsumer: ArbeidssoekerperiodeConsumer,
    val opplysningerOmArbeidssoekerService: OpplysningerOmArbeidssoekerService,
    val opplysningerOmArbeidssoekerConsumer: OpplysningerOmArbeidssoekerConsumer,
    val profileringService: ProfileringService,
    val profileringConsumer: ProfileringConsumer,
    val autorisasjonService: AutorisasjonService
)
