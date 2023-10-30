import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "com.sap.pi"
version = "0.0.1"

val ktorVersion = "2.2.4"
val logbackVersion = "1.4.11"
val prometheusVersion = "1.11.3"
val postgresVersion = "42.5.4"
val nettyVersion = "4.1.94.Final"

plugins {
    application
    jacoco
    kotlin("jvm") version "1.9.10"
    id("io.ktor.plugin") version "2.1.3"
    id("org.owasp.dependencycheck") version "7.4.4"
    id("com.github.kt3k.coveralls") version "2.12.2"
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

application {
    applicationName = "analysis"
    group = "com.sap.pi"
    mainClass.set("com.sap.pi.analysis.ApplicationKt")
}

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    freeCompilerArgs = listOf("-Xcontext-receivers")
}
val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    freeCompilerArgs = listOf("-Xcontext-receivers")
}

dependencies {
    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-server-netty:$ktorVersion")

    implementation("io.ktor:ktor-server-resources:$ktorVersion")

    implementation("io.ktor:ktor-server-auth:$ktorVersion")
    implementation("io.ktor:ktor-server-auth-jwt:$ktorVersion")

    implementation("ch.qos.logback:logback-classic:$logbackVersion")
    implementation("net.logstash.logback:logstash-logback-encoder:7.4")
    implementation("io.ktor:ktor-server-call-logging:$ktorVersion")
    implementation("io.sentry:sentry-logback:6.28.0")
    implementation("io.github.oshai:kotlin-logging-jvm:5.1.0")

    implementation("io.ktor:ktor-server-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-jackson:2.3.4")
    implementation("io.ktor:ktor-serialization:$ktorVersion")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.2")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.15.2")

    implementation("io.ktor:ktor-server-metrics-micrometer:$ktorVersion")
    implementation("io.micrometer:micrometer-registry-prometheus:$prometheusVersion")
    implementation("com.codahale.metrics:metrics-healthchecks:3.0.2")

    implementation(platform("com.amazonaws:aws-java-sdk-bom:1.12.542"))
    implementation("com.amazonaws:aws-java-sdk-rds")
    implementation("com.amazonaws:aws-java-sdk-sts")

    implementation("com.zaxxer:HikariCP:5.0.1")
    implementation("org.postgresql:postgresql:$postgresVersion")

    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.8.0")
    implementation("io.ktor:ktor-server-status-pages:$ktorVersion")

    implementation("io.ktor:ktor-client-cio:$ktorVersion")
    implementation("io.ktor:ktor-client-logging:$ktorVersion")
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-client-auth:$ktorVersion")
    implementation("io.ktor:ktor-client-auth:$ktorVersion")

    implementation("org.apache.qpid:qpid-jms-client:2.4.0")
    implementation("org.yaml:snakeyaml:2.2")

    testImplementation("org.junit.jupiter:junit-jupiter:5.9.2")
    testImplementation("io.ktor:ktor-server-test-host:$ktorVersion")
    testImplementation("io.kotest:kotest-runner-junit5-jvm:5.7.1")
    testImplementation("io.mockk:mockk:1.13.7")
    testImplementation("com.github.tomakehurst:wiremock-jre8:2.35.0")
    testImplementation("org.apache.qpid:qpid-broker-core:9.0.0")
    testImplementation("org.apache.qpid:qpid-broker-plugins-amqp-1-0-protocol:9.0.0")
    testImplementation("org.apache.qpid:qpid-broker-plugins-memory-store:9.0.0")

    constraints {
        // Fix https://sap.blackducksoftware.com/api/vulnerabilities/CVE-2022-41881/overview
        implementation("io.netty:netty-codec-http2") {
            version {
                require(nettyVersion)
            }
        }
        implementation("io.netty:netty-transport-native-kqueue") {
            version {
                require(nettyVersion)
            }
        }
        implementation("io.netty:netty-transport-native-epoll") {
            version {
                require(nettyVersion)
            }
        }
        implementation("io.netty:netty-transport") {
            version {
                require(nettyVersion)
            }
        }
        implementation("io.netty:netty-transport-native-unix-common") {
            version {
                require(nettyVersion)
            }
        }
        implementation("io.netty:netty-resolver") {
            version {
                require(nettyVersion)
            }
        }
        implementation("io.netty:netty-codec") {
            version {
                require(nettyVersion)
            }
        }
        implementation("io.netty:netty-codec-http") {
            version {
                require(nettyVersion)
            }
        }
        implementation("io.netty:netty-common") {
            version {
                require(nettyVersion)
            }
        }
        implementation("io.netty:netty-buffer") {
            version {
                require(nettyVersion)
            }
        }
        implementation("io.netty:netty-handler") {
            version {
                require(nettyVersion)
            }
        }
        // Fix: https://sap.blackducksoftware.com/api/vulnerabilities/CVE-2023-2976/overview
        implementation("com.google.guava:guava") {
            version {
                require("32.1.2-jre")
            }
        }
    }
}

tasks.test {
    useJUnitPlatform()
}

tasks.jacocoTestReport {
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
    classDirectories.setFrom(
        files(
            classDirectories.files.map {
                fileTree(it) {
                    exclude(
                        "**/generated/**/*",
                    )
                }
            },
        ),
    )
}

tasks.test {
    finalizedBy("jacocoTestReport")
}

tasks.check {
    dependsOn("jacocoTestReport")
}

coveralls {
    jacocoReportPath = "${project.buildDir}/reports/jacoco/test/jacocoTestReport.xml"
    sourceDirs = listOf("${project.projectDir}/src/main/kotlin")
}
