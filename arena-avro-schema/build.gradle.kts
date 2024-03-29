plugins {
    kotlin("jvm")
    `maven-publish`
}

val schemaMajorVersion: String by project
val schemaMinorVersion: String by project
val gitSha: String? by project
version = "$schemaMajorVersion.$schemaMinorVersion"

val schemaDeps by configurations.creating {
    isTransitive = false
}

dependencies {
    schemaDeps(project(":main-avro-schema"))
}

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

tasks.withType(Copy::class).configureEach {
    from("$rootDir/main-avro-schema/src/main/resources/") {
        include("*.avdl", "vo/*.avdl", "helpers/*.avdl")
        filter { line ->
            line.replace(
                "no.nav.paw.arbeidssokerregisteret.api",
                "no.nav.paw.arbeidssokerregisteret.arena"
            )
        }
    }
    into("$buildTreePath/resources/main/")
}

tasks.withType(Jar::class) {
    manifest {
        attributes["Implementation-Version"] = project.version
        attributes["Implementation-Title"] = project.name
        attributes["Arbeidssokerregisteret-Modul"] = "avro-schema"
        gitSha?.also { sha ->
            attributes["Git-SHA"] = sha
        }
    }
}
