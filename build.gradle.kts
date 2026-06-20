plugins {
    java
    alias(libs.plugins.sonarqube)
}

group = "nl.chimpgamer"
version = "1.1.9"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:26.1.2.build.+")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.release.set(25)
}

tasks.processResources {
    filesMatching("paper-plugin.yml") {
        expand("version" to project.version)
    }
}

sonar {
    properties {
        property("sonar.projectKey", "map-art-lock")
        property("sonar.projectVersion", project.version.toString())
        property("sonar.sourceEncoding", "UTF-8")
    }
}
