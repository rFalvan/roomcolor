plugins {
    kotlin("jvm") version "1.9.23"
}

group = "me.falvan"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {}

kotlin {
    jvmToolchain(17)
}