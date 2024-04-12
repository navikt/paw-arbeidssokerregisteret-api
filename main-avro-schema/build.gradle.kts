import java.time.Instant

plugins {
    kotlin("jvm")
    java
    `maven-publish`
}

val schemaMajorVersion: String by project
val schemaMinorVersion: String by project
val gitSha: String? by project
version = "$schemaMajorVersion.$schemaMinorVersion"

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["kotlin"])
        }
    }
    repositories {
        maven {
            val mavenRepo: String by project
            val githubPassword: String by project
            setUrl("https://maven.pkg.github.com/navikt/$mavenRepo")
            credentials {
                username = "x-access-token"
                password = githubPassword
            }
        }
    }
}

tasks.withType(Jar::class) {
    manifest {
        attributes["Implementation-Version"] = project.version
        attributes["Implementation-Title"] = project.name
        attributes["Group"] = project.group.toString()
        attributes["Arbeidssokerregisteret-Modul"] = "avro-schema"
        gitSha?.also { sha ->
            attributes["Git-SHA"] = sha
        }
        attributes["build-timestamp"] = Instant.now().toString()
    }
}
