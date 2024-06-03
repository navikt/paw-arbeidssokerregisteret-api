package no.nav.paw.arbeidssoekerregisteret.app

import io.confluent.kafka.schemaregistry.client.SchemaRegistryClientConfig
import no.nav.paw.arbeidssokerregisteret.api.v2.Periode
import no.nav.paw.arbeidssokerregisteret.api.v2.Profilering
import no.nav.paw.arbeidssokerregisteret.api.v5.OpplysningerOmArbeidssoeker
import no.nav.paw.arbeidssokerregisteret.arena.v6.ArenaArbeidssokerregisterTilstand
import org.apache.avro.Schema
import java.lang.System.getenv

private const val SCHEMA_REG_USER = "KAFKA_SCHEMA_REGISTRY_USER"
private const val SCHEMA_REG_PASSWORD = "KAFKA_SCHEMA_REGISTRY_PASSWORD"
private val schemaRegPwd = getenv(SCHEMA_REG_PASSWORD)

private val schemaRegUser = getenv(SCHEMA_REG_USER)
private val schemaRegUserInfo = "$schemaRegUser:$schemaRegPwd"

const val SCHEMA_REG_URL = "KAFKA_SCHEMA_REGISTRY"
val schemaRegistryProperties: Map<String, Any> = mapOf(
    SchemaRegistryClientConfig.BASIC_AUTH_CREDENTIALS_SOURCE to "USER_INFO",
    SchemaRegistryClientConfig.USER_INFO_CONFIG to schemaRegUserInfo,
)

val subjectMap: Map<Schema, String> = mapOf(
    Periode.`SCHEMA$` to "paw.arbeidssokerperioder-v1",
    OpplysningerOmArbeidssoeker.`SCHEMA$` to "paw.opplysninger-om-arbeidssoeker-v1",
    Profilering.`SCHEMA$` to "paw.arbeidssoker-profilering-v1",
    ArenaArbeidssokerregisterTilstand.`SCHEMA$` to "paw.arbeidssoker-arena-v1",
).mapValues { (_, v) -> "$v-value"}

const val COMPATIBILITY_LEVEL_FULL_TRANSITIVE = "FULL_TRANSITIVE"