pluginManagement {
    val flywayVersion: String by settings

    plugins {
        id("org.flywaydb.flyway") version flywayVersion
    }
}

buildscript {
    val flywayVersion: String by settings
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("org.flywaydb:flyway-database-postgresql:$flywayVersion")
        classpath("com.fasterxml.jackson.core:jackson-databind:2.16.1")
    }
}

rootProject.name = "analysis"
