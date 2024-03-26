import com.github.davidmc24.gradle.plugin.avro.GenerateAvroProtocolTask

plugins {
    kotlin("jvm")
    id("com.github.davidmc24.gradle.plugin.avro") version "1.9.1"
}
val jvmVersion = JavaVersion.VERSION_21
val schema by configurations.creating {
    isTransitive = false
}

dependencies {
    implementation(project(":main-avro-schema"))
    implementation(project(":arena-avro-schema"))
    schema(project(":arena-avro-schema"))
    schema(project(":main-avro-schema"))
    api("org.apache.avro:avro:1.11.0")
}

tasks.named("generateAvroProtocol", GenerateAvroProtocolTask::class.java) {
    schema.forEach {
        source(zipTree(it))
    }
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(jvmVersion.majorVersion)
    }
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}
