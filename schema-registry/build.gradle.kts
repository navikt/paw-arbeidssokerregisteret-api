plugins {
    kotlin("jvm")
    id("com.google.cloud.tools.jib") version "3.4.1"
    application
}

val schemaMinorVersion: String by project
version = schemaMinorVersion
val jvmVersion = JavaVersion.VERSION_21
val image: String? by project

dependencies {
    implementation("ch.qos.logback:logback-classic:1.4.14")
    implementation("net.logstash.logback:logstash-logback-encoder:7.3")
    implementation("no.nav.common:log:3.2024.02.21_11.18-8f9b43befae1")

    implementation("io.ktor:ktor-server-core:2.3.9")
    implementation("io.ktor:ktor-server-netty:2.3.9")
    implementation("io.confluent:kafka-schema-registry-client:7.6.0")
}
jib {
    from.image = "ghcr.io/navikt/baseimages/temurin:${jvmVersion.majorVersion}"
    to.image = "${image ?: rootProject.name}:${version}"
}