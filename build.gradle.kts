plugins {
    kotlin("jvm") version "1.9.23"
    application
}

group = "com.resvara"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    // Vert.x Core and Vert.x Web dependencies
    implementation("io.vertx:vertx-core:4.5.7")
    implementation("io.vertx:vertx-web:4.5.7")
    implementation("io.vertx:vertx-lang-kotlin-coroutines:4.5.7")
    implementation("io.vertx:vertx-auth-jwt:4.5.7")

    // JSON serialization for Vert.x with Jackson
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.17.1")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jdk8:2.17.1")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.17.1")

    // Testing dependencies
    testImplementation("org.jetbrains.kotlin:kotlin-test:1.9.23")
    testImplementation("io.vertx:vertx-junit5:4.5.7")
}

application {
    mainClass.set("com.resvera.ApplicationKt")
}


