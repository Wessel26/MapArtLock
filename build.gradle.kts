import java.text.SimpleDateFormat
import java.util.Date

plugins {
    kotlin("jvm") version libs.versions.kotlin.get()
    alias(libs.plugins.shadow)
    alias(libs.plugins.run.paper)
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly(libs.paper.api)
    compileOnly(libs.kotlin.stdlib)
    compileOnly(libs.boosted.yaml)
    compileOnly(libs.cloud.core)
    compileOnly(libs.cloud.paper)
    compileOnly(libs.cloud.minecraft.extras)
    compileOnly(libs.cloud.kotlin.coroutines)
}

kotlin {
    jvmToolchain(25)
}

tasks {
    build {
        dependsOn(shadowJar)
    }

    runServer {
        minecraftVersion(libs.versions.minecraft.get())
        jvmArgs("-Xms2G", "-Xmx2G", "-Dcom.mojang.eula.agree=true")
    }

    processResources {
        val buildNumber = System.getenv("BUILD_NUMBER") ?: "SNAPSHOT"
        val props = mapOf("version" to version, "buildNumber" to buildNumber, "buildDate" to getDate())
        filesMatching("paper-plugin.yml") {
            expand(props)
        }
    }
}

fun getDate(): String {
    val simpleDateFormat = SimpleDateFormat("dd-MM-yyyy hh:mm:ss")
    val date = Date()
    return simpleDateFormat.format(date)
}