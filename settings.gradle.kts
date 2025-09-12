plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
    kotlin("jvm") version "2.2.20" apply false
}

rootProject.name = "paw-arbeidssokerregisteret-api"
include(
    "main-avro-schema",
    "arena-avro-schema",
    "schema-registry",
    "bekreftelse-paavegneav-schema",
    "bekreftelsesmelding-schema"
)

dependencyResolutionManagement {
    val githubPassword: String by settings
    repositories {
        maven {
            setUrl("https://maven.pkg.github.com/navikt/paw-arbeidssokerregisteret-api")
            credentials {
                username = "x-access-token"
                password = githubPassword
            }
        }
        mavenCentral()
        maven {
            url = uri("https://packages.confluent.io/maven/")
        }
    }
}
