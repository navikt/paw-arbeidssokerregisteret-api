package no.nav.paw.arbeidssokerregisteret.api.config

data class KafkaServerConfig(
    val autentisering: String,
    val kafkaBrokers: String,
    val keystorePath: String?,
    val credstorePassword: String?,
    val truststorePath: String?
)
