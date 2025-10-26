plugins {
    id("java-library")
    id("chirp.kotlin-common")

}

group = "org.example"
version = "unspecified"

repositories {
    mavenCentral()
    maven { url = uri("https://repo.spring.io/snapshot") }
}

dependencies {
    api(libs.kotlin.reflect)
    api(libs.jackson.module.kotlin)
   implementation(libs.spring.boot.starter.amqp)
   implementation(libs.jackson.datatype.jsr310)
    implementation(libs.spring.boot.starter.security)
    implementation(libs.jwt.api)
    runtimeOnly(libs.jwt.impl)
    runtimeOnly(libs.jwt.jackson)
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}