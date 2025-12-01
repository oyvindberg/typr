plugins {
    kotlin("jvm") version "2.2.21"
}

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain(21)
}

sourceSets {
    main {
        kotlin {
            srcDirs("testapi")
        }
    }
}

dependencies {
    implementation("com.fasterxml.jackson.core:jackson-annotations:2.17.2")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.2")
    implementation("io.swagger.core.v3:swagger-annotations:2.2.15")
    implementation("org.springframework:spring-web:6.1.12")
    implementation("jakarta.validation:jakarta.validation-api:3.0.2")
}
