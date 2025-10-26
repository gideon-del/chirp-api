plugins {
    id("java-library")
    id("chirp.spring-boot-service")

    kotlin("plugin.jpa")
}

group = "org.example"
version = "unspecified"

repositories {
    mavenCentral()
    maven { url = uri("https://repo.spring.io/snapshot") }
}

dependencies {
    implementation(projects.common)
    implementation(libs.spring.boot.starter.data.jpa)
    implementation(libs.spring.boot.starter.data.redis)
    implementation(libs.spring.boot.starter.security)
    implementation(libs.spring.boot.starter.validation)
    implementation(libs.jackson.module.kotlin)
  implementation(libs.jwt.api)
    testImplementation(kotlin("test"))
    runtimeOnly(libs.jwt.impl)
    runtimeOnly(libs.jwt.jackson)
}


tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}