import com.github.davidmc24.gradle.plugin.avro.GenerateAvroProtocolTask

plugins {
    kotlin("jvm")
    id("com.google.cloud.tools.jib") version "3.5.4"
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
    // Løfter transitive Jackson-versjoner (fra avro, confluent og logstash-encoder) forbi kjente CVE-er
    implementation(platform("com.fasterxml.jackson:jackson-bom:2.22.3"))
    implementation(platform("tools.jackson:jackson-bom:3.2.3"))
    constraints {
        implementation("com.google.guava:guava:33.8.0-jre") {
            because("CVE-2026-102554, transitivt fra kafka-schema-registry-client")
        }
    }

    implementation("ch.qos.logback:logback-classic:1.6.8")
    implementation("net.logstash.logback:logstash-logback-encoder:9.0")
    implementation("no.nav.common:log:4.2026.10.01_11.30-68a5418edc3c")

    implementation("io.ktor:ktor-server-core:3.6.1")
    implementation("io.ktor:ktor-server-netty:3.6.1")
    implementation("io.confluent:kafka-schema-registry-client:8.4.1")
    implementation(project(":main-avro-schema"))
    implementation(project(":arena-avro-schema"))
    implementation(project(":bekreftelse-paavegneav-schema"))
    implementation(project(":bekreftelsesmelding-schema"))
    schema(project(":arena-avro-schema"))
    schema(project(":main-avro-schema"))
    schema(project(":bekreftelse-paavegneav-schema"))
    schema(project(":bekreftelsesmelding-schema"))
    api("org.apache.avro:avro:1.12.2")
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
