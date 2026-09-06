plugins {
	java
	id("org.springframework.boot") version "4.1.1"
	id("io.spring.dependency-management") version "1.1.7"
	id("jacoco")
}

group = "com.finora-app"
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
	implementation("org.springframework.boot:spring-boot-starter-webmvc")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.security:spring-security-oauth2-jose")
	implementation("org.springframework.boot:spring-boot-starter-jdbc")
	implementation("org.springframework.boot:spring-boot-starter-flyway")
	implementation("org.flywaydb:flyway-database-postgresql")
	runtimeOnly("org.postgresql:postgresql")
	testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
	useJUnitPlatform()
}

jacoco {
	toolVersion = "0.8.13"
}

val coverageExclusions = listOf("**/FinoraApplication.class")

tasks.jacocoTestReport {
	dependsOn(tasks.test)
	classDirectories.setFrom(
		files(classDirectories.files.map { fileTree(it) { exclude(coverageExclusions) } })
	)
	reports {
		xml.required = true
		html.required = true
	}
}

tasks.jacocoTestCoverageVerification {
	classDirectories.setFrom(
		files(classDirectories.files.map { fileTree(it) { exclude(coverageExclusions) } })
	)
	violationRules {
		rule {
			limit {
				counter = "LINE"
				value = "COVEREDRATIO"
				minimum = "0.90".toBigDecimal()
			}
			limit {
				counter = "BRANCH"
				value = "COVEREDRATIO"
				minimum = "0.90".toBigDecimal()
			}
		}
	}
}

tasks.check {
	dependsOn(tasks.jacocoTestCoverageVerification)
}
