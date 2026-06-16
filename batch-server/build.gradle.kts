plugins {
	alias(libs.plugins.kotlin.jvm)
	alias(libs.plugins.kotlin.spring)
	alias(libs.plugins.spring.boot)
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

dependencies {
	implementation(platform(libs.spring.dependency))
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
}

tasks.withType<Test>().configureEach {
	useJUnitPlatform()

	jvmArgs("--add-opens", "java.base/java.lang.reflect=ALL-UNNAMED")
}
