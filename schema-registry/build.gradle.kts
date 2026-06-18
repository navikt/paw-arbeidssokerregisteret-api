import com.github.davidmc24.gradle.plugin.avro.GenerateAvroProtocolTask

plugins {
    kotlin("jvm")
    id("com.google.cloud.tools.jib") version "3.5.3"
    id("com.github.davidmc24.gradle.plugin.avro") version "1.9.1"
    application
}

val schemaMinorVersion: String by project
version = schemaMinorVersion
val jvmVersion = JavaVersion.VERSION_25
val image: String? by project
val schema by configurations.creating {
    isTransitive = false
}

dependencies {
    implementation("ch.qos.logback:logback-classic:1.5.32")
    implementation("net.logstash.logback:logstash-logback-encoder:8.1")
    implementation("no.nav.common:log:3.2025.11.10_14.07-a9f44944d7bc")

    implementation("io.ktor:ktor-server-core:3.4.3")
    implementation("io.ktor:ktor-server-netty:3.4.3")
    implementation("io.confluent:kafka-schema-registry-client:8.2.0")
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

tasks.named("generateAvroProtocol", GenerateAvroProtocolTask::class.java) {
    schema.forEach {
        source(zipTree(it))
    }
}

val chainguardJavaImage : String = "europe-north1-docker.pkg.dev/cgr-nav/pull-through/nav.no/jre:openjdk-"
val targetImage: String = "${image ?: project.name}:${project.version}"
val baseImage = "$chainguardJavaImage$jvmVersion"

// Workaround: Jib cannot parse OCI Image Index v1.1 manifests with the `artifactType`
// field (unresolved upstream bug). Pre-pull via Docker, which handles OCI v1.1 natively,
// then point Jib at the local daemon image to bypass the registry manifest parsing.
val pullBaseImage by tasks.registering(Exec::class) {
    group = "jib"
    description = "Pre-pull base image to local Docker daemon"
    commandLine("docker", "pull", baseImage)
}

jib {
    from.image = "docker://$baseImage"
    to.image = targetImage
    container {
        jvmFlags = listOf("-XX:ActiveProcessorCount=8", "-XX:+UseZGC", "-XX:+ZGenerational")
        environment = mapOf(
            "IMAGE_WITH_VERSION" to targetImage
        )
    }
}

tasks.named("jib") { dependsOn(pullBaseImage) }
tasks.named("jibDockerBuild") { dependsOn(pullBaseImage) }