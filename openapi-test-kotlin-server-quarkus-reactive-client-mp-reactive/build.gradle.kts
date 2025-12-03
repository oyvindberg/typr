plugins {
    kotlin("jvm") version "2.1.0"
}

group = "testapi"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain(21)
}

sourceSets {
    main {
        kotlin.srcDirs("testapi")
    }
    test {
        kotlin.srcDirs("test")
    }
}

dependencies {
    // Jackson
    implementation("com.fasterxml.jackson.core:jackson-annotations:2.17.2")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.2")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.17.2")

    // OpenAPI/Swagger annotations
    implementation("io.swagger.core.v3:swagger-annotations:2.2.25")

    // JAX-RS
    implementation("jakarta.ws.rs:jakarta.ws.rs-api:3.1.0")

    // Validation
    implementation("jakarta.validation:jakarta.validation-api:3.0.2")

    // Mutiny for reactive
    implementation("io.smallrye.reactive:mutiny:2.6.2")

    // MicroProfile REST Client
    implementation("org.eclipse.microprofile.rest.client:microprofile-rest-client-api:3.0.1")

    // Jersey multipart for file uploads
    implementation("org.glassfish.jersey.media:jersey-media-multipart:3.1.9")

    // Testing
    testImplementation("junit:junit:4.13.2")
}
