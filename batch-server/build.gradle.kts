plugins {
	alias(libs.plugins.kotlin.jvm)
	alias(libs.plugins.kotlin.spring)
	alias(libs.plugins.spring.boot)
	alias(libs.plugins.kotlin.noarg)

	jacoco
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

jacoco {
	toolVersion = "0.8.14"
}

tasks.jacocoTestReport {
	reports {
		xml.required = true
		csv.required = false
		html.required = true

		xml.outputLocation.set(layout.buildDirectory.file("build/reports/jacoco/testCoverage/testCoverage.xml"))

		classDirectories.setFrom(
			sourceSets.main.get().output.asFileTree.matching {
				exclude("com/finance/batch/BatchServerApplication*")
			}
		)
	}
}

tasks.jacocoTestCoverageVerification {

	// 애플리케이션 진입점(main)은 테스트 대상에서 제외
	val excludedClasses = listOf("com.finance.batch.BatchServerApplication*")

	violationRules {
		rule {
			element = "BUNDLE"

			limit {
				counter = "LINE"
				value = "COVEREDRATIO"
				minimum = "0.50".toBigDecimal()
			}

			excludes = excludedClasses
		}

		rule {
			element = "BUNDLE"

			limit {
				counter = "BRANCH"
				value = "COVEREDRATIO"
				minimum = "0.40".toBigDecimal()	// 초기 단계 배치 로직 특성상 분기 커버리지 목표는 완화해서 설정
			}

			excludes = excludedClasses
		}

		rule {
			element = "BUNDLE"

			limit {
				counter = "CLASS"
				value = "COVEREDRATIO"
				minimum = "0.50".toBigDecimal() // 팀 정책 임계값(LINE/BRANCH와 통일), 완전 미테스트 클래스 누적 방지
			}

			excludes = excludedClasses
		}
	}

	mustRunAfter(tasks.jacocoTestReport)
}

tasks.test {
	finalizedBy(tasks.jacocoTestReport)
}

tasks.withType<Test>().configureEach {
	useJUnitPlatform()

	jvmArgs("--add-opens", "java.base/java.lang.reflect=ALL-UNNAMED")
}
