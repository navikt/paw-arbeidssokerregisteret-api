import com.github.davidmc24.gradle.plugin.avro.GenerateAvroProtocolTask

plugins {
    kotlin("jvm")
    id("com.google.cloud.tools.jib") version "3.5.2"
    id("com.github.davidmc24.gradle.plugin.avro") version "1.9.1"
    application
}

val schemaMinorVersion: String by project
version = schemaMinorVersion
val jvmVersion = JavaVersion.VERSION_21
val image: String? by project
val schema by configurations.creating {
    isTransitive = false
}

dependencies {
    implementation("ch.qos.logback:logback-classic:1.5.24")
    implementation("net.logstash.logback:logstash-logback-encoder:8.1")
    implementation("no.nav.common:log:3.2025.11.10_14.07-a9f44944d7bc")

    implementation("io.ktor:ktor-server-core:3.3.3")
    implementation("io.ktor:ktor-server-netty:3.3.3")
    implementation("io.confluent:kafka-schema-registry-client:8.1.1")
    implementation(project(":main-avro-schema"))
    implementation(project(":arena-avro-schema"))
    implementation(project(":bekreftelse-paavegneav-schema"))
    implementation(project(":bekreftelsesmelding-schema"))
    schema(project(":arena-avro-schema"))
    schema(project(":main-avro-schema"))
    schema(project(":bekreftelse-paavegneav-schema"))
    schema(project(":bekreftelsesmelding-schema"))
    api("org.apache.avro:avro:1.12.1")
}
jib {
    from.image = "europe-north1-docker.pkg.dev/cgr-nav/pull-through/nav.no/jre:openjdk-${jvmVersion}"
    to.image = "${image ?: rootProject.name}:${version}"
}

tasks.named("generateAvroProtocol", GenerateAvroProtocolTask::class.java) {
    schema.forEach {
        source(zipTree(it))
    }
}