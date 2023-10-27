package no.nav.paw.arbeidssokerregisteret.api.config

import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.addResourceSource
import java.lang.System.getenv

inline fun <reified T : Any> loadConfiguration(): T = ConfigLoaderBuilder.default()
    .apply {
        when (getenv("NAIS_CLUSTER_NAME")) {
            "prod-gcp" -> {
                addResourceSource("/application-prod.yaml", optional = true)
            }

            "dev-gcp" -> {
                addResourceSource("/application-local.yaml", optional = true)
            }

            else -> {
                addResourceSource("/application-local.yaml", optional = true)
            }
        }
    }
    .strict()
    .build()
    .loadConfigOrThrow()
