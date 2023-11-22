package no.nav.paw.arbeidssokerregisteret.api.config

data class Config(
    val authProviders: List<AuthProvider>,
    val database: DatabaseConfig,
    val kafka: KafkaConfig,
    val poaoClientConfig: ServiceClientConfig
)

data class AuthProvider(
    val name: String,
    val discoveryUrl: String,
    val tokenEndpointUrl: String,
    val clientId: String,
    val claims: Claims
)

data class Claims(
    val map: List<String>,
    val combineWithOr: Boolean = false
)

data class KafkaConfig(
    val brokerUrl: String,
    val consumerGroupId: String,
    val consumers: KafkaConsumers,
    val schemaRegistryUrl: String
)

data class KafkaConsumers(
    val arbeidssokerperioder: KafkaConsumer,
    val arbeidssokersituasjon: KafkaConsumer
)

data class KafkaConsumer(
    val topic: String
)

data class ServiceClientConfig(
    val url: String,
    val scope: String
)

data class DatabaseConfig(
    val host: String,
    val port: Int,
    val username: String,
    val password: String,
    val name: String
) {
    val url get() = "jdbc:postgresql://$host:$port/$name?user=$username&password=$password"
}
