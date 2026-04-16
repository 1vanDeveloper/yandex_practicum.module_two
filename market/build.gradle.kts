plugins {
    id("java")
    id("org.springframework.boot") version "3.4.4"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.openapi.generator") version "7.12.0"
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

group = "ru.yandex.practicum"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.apache.commons:commons-lang3:3.14.0")
    implementation("org.openapitools:jackson-databind-nullable:0.2.6")
    runtimeOnly("org.postgresql:r2dbc-postgresql")
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.projectreactor:reactor-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}

openApiGenerate {
    generatorName.set("java")
    inputSpec.set("${project.rootDir}/payments/build/openapi.json")
    outputDir.set("${project.layout.buildDirectory.get().asFile.absolutePath}/generated")
    apiPackage.set("ru.yandex.practicum.client.api")
    modelPackage.set("ru.yandex.practicum.client.model")
    invokerPackage.set("ru.yandex.practicum.client")
    groupId.set("ru.yandex.practicum")
    library.set("webclient")
    configOptions.set(
        mapOf(
            "reactive" to "true",
            "useBeanValidation" to "true",
            "useJakartaEe" to "true",
            "dateLibrary" to "java8"
        )
    )
    generateApiTests.set(false)
    generateModelTests.set(false)
}

tasks.openApiGenerate {
    dependsOn(":payments:generateOpenApiDocs")
}

tasks.compileJava {
    dependsOn(tasks.openApiGenerate)
}

sourceSets["main"].java.srcDir("${layout.buildDirectory.get().asFile.absolutePath}/generated/src/main/java")
