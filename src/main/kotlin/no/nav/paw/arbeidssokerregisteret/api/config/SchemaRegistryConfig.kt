package no.nav.paw.arbeidssokerregisteret.api.config

data class SchemaRegistryConfig(
    val url: String,
    val bruker: String?,
    val passord: String?,
    val autoRegistrerSchema: Boolean = true
)
