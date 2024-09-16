import com.github.davidmc24.gradle.plugin.avro.GenerateAvroProtocolTask

plugins {
    kotlin("jvm")
    id("com.google.cloud.tools.jib") version "3.4.4"
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
    implementation("ch.qos.logback:logback-classic:1.5.16")
    implementation("net.logstash.logback:logstash-logback-encoder:8.0")
    implementation("no.nav.common:log:3.2024.05.23_05.46-2b29fa343e8e")

    implementation("io.ktor:ktor-server-core:3.0.3")
    implementation("io.ktor:ktor-server-netty:3.0.3")
    implementation("io.confluent:kafka-schema-registry-client:7.8.0")
    implementation(project(":main-avro-schema"))
    implementation(project(":arena-avro-schema"))
    implementation(project(":bekreftelsesansvar-schema"))
    implementation(project(":bekreftelsesmelding-schema"))
    schema(project(":arena-avro-schema"))
    schema(project(":main-avro-schema"))
    schema(project(":bekreftelsesansvar-schema"))
    schema(project(":bekreftelsesmelding-schema"))
    api("org.apache.avro:avro:1.12.0")
}
jib {
    from.image = "gcr.io/distroless/java${jvmVersion}-debian12"
    to.image = "${image ?: rootProject.name}:${version}"
}

tasks.named("generateAvroProtocol", GenerateAvroProtocolTask::class.java) {
    schema.forEach {
        source(zipTree(it))
    }
}