package no.nav.paw.arbeidssoekerregisteret.app

import io.confluent.kafka.schemaregistry.client.SchemaRegistryClientConfig
import no.nav.paw.arbeidssokerregisteret.api.v1.Egenvurdering
import no.nav.paw.arbeidssokerregisteret.api.v1.Periode
import no.nav.paw.arbeidssokerregisteret.api.v1.Profilering
import no.nav.paw.arbeidssokerregisteret.api.v4.OpplysningerOmArbeidssoeker
import no.nav.paw.arbeidssokerregisteret.arena.v8.ArenaArbeidssokerregisterTilstand
import no.nav.paw.bekreftelse.melding.v1.Bekreftelse
import no.nav.paw.bekreftelse.paavegneav.v1.PaaVegneAv
import org.apache.avro.Schema
import java.lang.System.getenv

private val schemaRegPwd = getenv("KAFKA_SCHEMA_REGISTRY_PASSWORD")
private val schemaRegUser = getenv("KAFKA_SCHEMA_REGISTRY_USER")
private val schemaRegUserInfo = "$schemaRegUser:$schemaRegPwd"

const val SCHEMA_REG_URL = "KAFKA_SCHEMA_REGISTRY"
val schemaRegistryProperties: Map<String, Any> = mapOf(
    SchemaRegistryClientConfig.BASIC_AUTH_CREDENTIALS_SOURCE to "USER_INFO",
    SchemaRegistryClientConfig.USER_INFO_CONFIG to schemaRegUserInfo,
)

val subjectMap: Map<String, Schema> get() {
    val domain = requireNotNull(getenv("SCHEMA_DOMAIN"), {"SCHEMA_DOMAIN must be defined"}).lowercase()
    logger.info("Using schema domain $domain")
    return when (domain) {
        "no-schema" -> emptyMap()
        "main" -> mapOf(
            "paw.arbeidssokerperioder-v1" to Periode.`SCHEMA$`,
            "paw.opplysninger-om-arbeidssoeker-v1" to OpplysningerOmArbeidssoeker.`SCHEMA$`,
            "paw.arbeidssoker-profilering-v1" to Profilering.`SCHEMA$`,
            "paw.arbeidssoker-arena-v1" to ArenaArbeidssokerregisterTilstand.`SCHEMA$`,
            "paw.arbeidssoker-egenvurdering-v1" to Egenvurdering.`SCHEMA$`
        ).mapKeys { (v, _) -> "$v-value"}
        "bekreftelse" -> mapOf(
            "paw.arbeidssoker-bekreftelse-v1" to Bekreftelse.`SCHEMA$`,
            "paw.arbeidssoker-bekreftelse-dagpenger-v1" to Bekreftelse.`SCHEMA$`,
            "paw.arbeidssoker-bekreftelse-friskmeldt-til-arbeidsformidling-v1" to Bekreftelse.`SCHEMA$`,
            "paw.arbeidssoker-bekreftelse-paavegneav-v1" to PaaVegneAv.`SCHEMA$`,
            "paw.arbeidssoker-bekreftelse-paavegneav-dagpenger-v1" to PaaVegneAv.`SCHEMA$`,
            "paw.arbeidssoker-bekreftelse-paavegneav-friskmeldt-til-arbeidsformidling-v1" to PaaVegneAv.`SCHEMA$`,
        ).mapKeys { (v, _) -> "$v-value" }
        else -> throw IllegalArgumentException("Unsupported domain")
    }
}

const val COMPATIBILITY_LEVEL_FULL_TRANSITIVE = "FULL_TRANSITIVE"