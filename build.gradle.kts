import com.github.davidmc24.gradle.plugin.avro.GenerateAvroProtocolTask

plugins {
    kotlin("jvm") version "1.9.10"
    id("io.ktor.plugin") version "2.3.4"
    id("com.github.davidmc24.gradle.plugin.avro") version "1.9.1"
    application
}

val arbeidssokerregisteretSchemaVersion = "23.11.02.28-1"
val logbackVersion = "1.4.5"
val logstashVersion = "7.3"
val navCommonModulesVersion = "2.2023.01.02_13.51-1c6adeb1653b"
val tokenSupportVersion = "3.1.5"
val koTestVersion = "5.7.2"
val hopliteVersion = "2.7.5"
val exposedVersion = "0.44.0"
val ktorVersion = pawObservability.versions.ktor

repositories {
    mavenLocal()
    mavenCentral()
    maven {
        url = uri("https://packages.confluent.io/maven/")
    }
    maven {
        url = uri("https://jitpack.io")
    }
    val githubPassword: String by project
    maven {
        setUrl("https://maven.pkg.github.com/navikt/paw-arbeidssokerregisteret-event-prosessor")
        credentials {
            username = "x-access-token"
            password = githubPassword
        }
    }
}

val schema by configurations.creating {
    isTransitive = false
}

dependencies {
    schema("no.nav.paw.arbeidssokerregisteret.api.schema:eksternt-api:$arbeidssokerregisteretSchemaVersion")
    implementation(pawObservability.bundles.ktorNettyOpentelemetryMicrometerPrometheus)
    implementation("no.nav.security:token-validation-ktor-v2:$tokenSupportVersion")
    implementation("no.nav.security:token-client-core:$tokenSupportVersion")
    implementation("no.nav.common:token-client:$navCommonModulesVersion")
    implementation("no.nav.common:log:$navCommonModulesVersion")
    implementation("ch.qos.logback:logback-classic:$logbackVersion")
    implementation("net.logstash.logback:logstash-logback-encoder:$logstashVersion")
    implementation("com.sksamuel.hoplite:hoplite-core:$hopliteVersion")
    implementation("com.sksamuel.hoplite:hoplite-yaml:$hopliteVersion")

    // Kafka
    implementation("org.apache.kafka:kafka-clients:3.6.0")
    implementation("org.apache.avro:avro:1.11.1")
    implementation("io.confluent:kafka-avro-serializer:7.4.0")

    // Ktor
    implementation("io.ktor:ktor-server-cors:$ktorVersion")
    implementation("io.ktor:ktor-server-swagger:$ktorVersion")
    implementation("io.ktor:ktor-server-call-id:$ktorVersion")
    implementation("io.ktor:ktor-server-status-pages:$ktorVersion")
    implementation("io.ktor:ktor-server-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-jackson:$ktorVersion")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.14.2")

    // Database
    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-crypt:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-java-time:$exposedVersion")
    implementation("com.zaxxer:HikariCP:5.0.1")
    implementation("org.postgresql:postgresql:42.6.0")
    implementation("org.flywaydb:flyway-core:9.21.2")

    // Test
    testImplementation("io.ktor:ktor-server-tests-jvm:$ktorVersion")
    testImplementation("io.kotest:kotest-runner-junit5:$koTestVersion")
    testImplementation("io.kotest:kotest-assertions-core:$koTestVersion")
    testImplementation("io.mockk:mockk:1.13.8")
    testImplementation("org.testcontainers:testcontainers:1.19.1")
    testImplementation("org.testcontainers:postgresql:1.19.1")
    testImplementation("no.nav.security:mock-oauth2-server:2.0.0")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

application {
    mainClass.set("no.nav.paw.arbeidssokerregisteret.api.ApplicationKt")
}

tasks.withType(GenerateAvroProtocolTask::class) {
    source(zipTree(schema.singleFile))
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

ktor {
    fatJar {
        archiveFileName.set("fat.jar")
    }
}
