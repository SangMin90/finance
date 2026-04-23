plugins {
    kotlin("jvm") version "2.3.10" apply false
    kotlin("plugin.spring") version "2.3.10" apply false
    id("org.springframework.boot") version "3.5.12" apply false
	id("io.spring.dependency-management") version "1.1.7" apply false
}

allprojects {
    group = "com.finance"
    version = "1.0-SNAPSHOT"
}