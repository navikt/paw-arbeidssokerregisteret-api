plugins {
    kotlin("jvm")
    `maven-publish`
}

val schemaMajorVersion: String by project
val schemaMinorVersion: String by project
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

