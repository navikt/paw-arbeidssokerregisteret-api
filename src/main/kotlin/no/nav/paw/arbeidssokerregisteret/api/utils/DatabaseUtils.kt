package no.nav.paw.arbeidssokerregisteret.api.utils

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.flywaydb.core.Flyway
import java.time.Duration
import javax.sql.DataSource

fun migrateDatabase(dataSource: DataSource) {
    Flyway.configure().baselineOnMigrate(true)
        .dataSource(dataSource)
        .load()
        .migrate()
}

fun generateDatasource(url: String): DataSource = HikariDataSource(
    HikariConfig().apply {
        jdbcUrl = url
        maximumPoolSize = 3
        connectionTimeout = Duration.ofSeconds(30).toMillis()
        maxLifetime = Duration.ofMinutes(30).toMillis()
    }
)
