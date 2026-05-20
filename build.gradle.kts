plugins {
    java
    id("org.springframework.boot") version "4.0.6"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "com.aquadev"
version = "0.0.1-SNAPSHOT"
description = "config-server"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

repositories {
    mavenCentral()
}

extra["springCloudVersion"] = "2025.1.1"
val opentelemetryVersion by extra("2.21.0-alpha")

dependencies {
    implementation("org.springframework.cloud:spring-cloud-config-server")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-opentelemetry")
    implementation("io.opentelemetry.instrumentation:opentelemetry-logback-appender-1.0:${opentelemetryVersion}")
    testImplementation("org.springframework.boot:spring-boot-starter-actuator-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.named<org.springframework.boot.gradle.tasks.run.BootRun>("bootRun") {
    jvmArgs("--sun-misc-unsafe-memory-access=allow")
}

tasks.register<JavaExec>("generateCds") {
    group = "optimization"
    description = "Generates a CDS archive for faster startup."
    dependsOn(tasks.bootJar)

    classpath = files(tasks.bootJar.get().archiveFile)
    mainClass.set("org.springframework.boot.loader.launch.JarLauncher")
    jvmArgs(
        "-Dspring.context.exit=onRefresh",
        "-XX:ArchiveClassesAtExit=${project.layout.buildDirectory.get()}/libs/application.jsa"
    )
}
