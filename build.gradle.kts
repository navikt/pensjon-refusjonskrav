group = "no.nav.pensjon.refusjonskrav"
description = "pensjon-refusjonskrav"


val tokensupportVersion = "7.0.1"

plugins {
    kotlin("jvm") version "2.4.21"
    kotlin("kapt") version "2.4.21"
    kotlin("plugin.spring") version "2.4.21"
    id("org.springframework.boot") version "4.1.2"
    id("io.spring.dependency-management") version "1.1.7"
}

extra["jackson-bom.version"] = "3.2.4"
extra["jackson-2-bom.version"] = "2.22.4"

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
    // Shared annotations must match Jackson 3 rather than the legacy logging BOM.
    implementation("com.fasterxml.jackson.core:jackson-annotations:2.22")
    implementation("tools.jackson.module:jackson-module-kotlin")
    implementation("io.micrometer:micrometer-registry-prometheus")
    implementation("net.logstash.logback:logstash-logback-encoder:8.0")
    implementation("no.nav.security:token-client-spring:$tokensupportVersion")
    implementation("no.nav.security:token-validation-spring:$tokensupportVersion")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springframework.boot:spring-boot-starter-restclient")

    testImplementation(kotlin("test-junit5"))
    testImplementation("com.ninja-squad:springmockk:5.0.1")
    testImplementation("no.nav.security:mock-oauth2-server:6.1.1")
    testImplementation("no.nav.security:token-validation-spring-test:$tokensupportVersion")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testImplementation(kotlin("test"))
    testImplementation("org.wiremock.integrations:wiremock-spring-boot:4.4.3")

}

kotlin {
    jvmToolchain(25)
}

tasks {
    test {
        useJUnitPlatform()
    }
}
