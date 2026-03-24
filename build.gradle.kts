plugins {
    kotlin("jvm") version "2.3.10" apply false
}

allprojects {
    group = "com.finance"
    version = "1.0-SNAPSHOT"

    repositories {
        mavenCentral()
    }
}