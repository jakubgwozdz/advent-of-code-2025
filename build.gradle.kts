plugins {
    kotlin("jvm") version "2.2.21"
}

repositories.mavenCentral()

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    implementation("ch.qos.logback:logback-classic:1.5.21")
}

kotlin {
    jvmToolchain(21)
}

