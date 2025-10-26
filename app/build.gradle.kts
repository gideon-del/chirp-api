plugins {
id("chirp.spring-boot-app")
}

group = "com.gideon"
version = "0.0.1-SNAPSHOT"
description = "Chirp backend"


repositories {
	mavenCentral()
	maven { url = uri("https://repo.spring.io/snapshot") }
}

dependencies {
    implementation(projects.chat)
    implementation(projects.user)
    implementation(projects.notification)
    implementation(projects.common)
    implementation(libs.jackson.datatype.jsr310)
    implementation(libs.spring.boot.starter.data.jpa)
    implementation(libs.spring.boot.starter.security)
    implementation(libs.spring.boot.starter.data.redis)
    implementation(libs.kotlin.reflect)
    implementation(libs.spring.boot.starter.amqp)
    implementation(libs.spring.boot.starter.mail)
    runtimeOnly(libs.postgresql)
}




