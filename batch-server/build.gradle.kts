plugins {
	java
	id("org.springframework.boot") version "3.5.12"
	id("io.spring.dependency-management") version "1.1.7"
	kotlin("jvm") version "2.1.0"
	kotlin("plugin.spring") version "2.1.0"
	kotlin("plugin.noarg") version "2.1.0"
}

noArg {
	annotation("com.finance.batch.global.annotation.NoArg")
}

group = "com.finance"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-batch")
	implementation("org.projectlombok:lombok")

	runtimeOnly("com.mysql:mysql-connector-j:8.4.0")
	runtimeOnly("com.h2database:h2")

	testImplementation("org.springframework.boot:spring-boot-starter-test") {
		exclude(group = "org.mockito", module = "mockito-core")
	}
	testImplementation("org.springframework.batch:spring-batch-test")

	testImplementation("io.kotest:kotest-runner-junit5:6.1.7")
	testImplementation("io.kotest:kotest-assertions-core:6.1.7")
	testImplementation("io.kotest:kotest-extensions-spring:6.1.7")

	testImplementation("io.mockk:mockk:1.13.10")

	testRuntimeOnly("org.junit.platform:junit-platform-launcher")

	implementation(kotlin("stdlib-jdk8"))
}

tasks.withType<Test>().configureEach {
	useJUnitPlatform()

	jvmArgs("--add-opens", "java.base/java.lang.reflect=ALL-UNNAMED")
}
