package no.nav.paw.arbeidssokerregisteret.api.utils

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.flywaydb.core.Flyway
import java.time.Duration
import javax.sql.DataSource

fun migrateDatabase(dataSource: DataSource) {
    val flyway = Flyway.configure().baselineOnMigrate(true).cleanDisabled(false).dataSource(dataSource).load()
    //flyway.clean()
    flyway.migrate()
}

fun generateDatasource(url: String): DataSource = HikariDataSource(
    HikariConfig().apply {
        jdbcUrl = url
        maximumPoolSize = 3
        connectionTimeout = Duration.ofSeconds(30).toMillis()
        maxLifetime = Duration.ofMinutes(30).toMillis()
    }
)
