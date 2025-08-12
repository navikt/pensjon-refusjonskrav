group = "no.nav.pensjon.refusjonskrav"
description = "pensjon-refusjonskrav"


val tokensupportVersion = "5.0.24"

plugins {
    kotlin("jvm") version "2.1.20"
    kotlin("plugin.spring") version "2.1.20"
    id("org.springframework.boot") version "3.5.4"
    id("io.spring.dependency-management") version "1.1.7"
}

repositories {
    mavenCentral()
    maven {
        url = uri("https://maven.pkg.github.com/navikt/maskinporten-validation")
        credentials {
            username = "token"
            password = System.getenv("GITHUB_TOKEN")
        }
    }
}

dependencies {
    implementation(kotlin("reflect"))
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.17.2")
    implementation("com.fasterxml.jackson.module", "jackson-module-kotlin", "2.17.2")
    implementation("io.micrometer", "micrometer-core", "1.13.4")
    implementation("io.micrometer", "micrometer-registry-prometheus", "1.13.4")
    implementation("net.logstash.logback", "logstash-logback-encoder", "8.0")
    implementation("no.nav.common", "token-client", "3.2024.09.16_11.09-578823a87a2f")
    implementation("no.nav.security", "token-validation-spring", tokensupportVersion)
    implementation("org.springframework.boot", "spring-boot-starter-actuator")
    implementation("org.springframework.boot", "spring-boot-starter-validation")
    implementation("org.springframework.boot", "spring-boot-starter-web")

    testImplementation(kotlin("test-junit5"))
    testImplementation("com.ninja-squad", "springmockk", "3.1.0")
    testImplementation("no.nav.security", "mock-oauth2-server", "2.1.10")
    testImplementation("no.nav.security", "token-validation-spring-test", tokensupportVersion)
    testImplementation("org.springframework.boot", "spring-boot-starter-test")

}

kotlin {
    jvmToolchain(21)
}

tasks{
    test {
        useJUnitPlatform()
    }
}
