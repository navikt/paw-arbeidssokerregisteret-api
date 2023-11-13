package no.nav.paw.arbeidssokerregisteret.api.repositories

import no.nav.paw.arbeidssokerregisteret.api.config.DatabaseConfig
import no.nav.paw.arbeidssokerregisteret.api.utils.dataSource
import no.nav.paw.arbeidssokerregisteret.api.utils.migrateDatabase
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.wait.strategy.Wait
import javax.sql.DataSource

fun initTestDatabase(): DataSource {
    val postgres = postgreSQLContainer()
    val dataSource =
        DatabaseConfig(
            host = postgres.host,
            port = postgres.firstMappedPort,
            username = postgres.username,
            password = postgres.password,
            name = postgres.databaseName
        ).dataSource()
    migrateDatabase(dataSource)
    return dataSource
}

fun postgreSQLContainer(): PostgreSQLContainer<out PostgreSQLContainer<*>> {
    val postgres =
        PostgreSQLContainer(
            "postgres:14"
        ).apply {
            addEnv("POSTGRES_PASSWORD", "admin")
            addEnv("POSTGRES_USER", "admin")
            addEnv("POSTGRES_DB", "arbeidssokerregisteret")
            addExposedPorts(5432)
        }
    postgres.start()
    postgres.waitingFor(Wait.forHealthcheck())
    return postgres
}
