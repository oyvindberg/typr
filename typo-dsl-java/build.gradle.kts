plugins {
    `java-library`
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

sourceSets {
    main {
        java {
            srcDirs("src/java", "../.bleep/generated-sources/typo-dsl-java/scripts.GeneratedTuples")
        }
    }
}

dependencies {
    api(project(":typo-runtime-java"))
}

tasks.withType<JavaCompile> {
    options.compilerArgs.addAll(listOf("-proc:none"))
    options.release.set(21)
}
