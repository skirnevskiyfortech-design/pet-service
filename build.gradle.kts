plugins {
    java
    checkstyle
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.dependency.management)
    alias(libs.plugins.openapi.generator)
}

group = "com.example"
version = "0.0.1-SNAPSHOT"
description = "pet-service"

openApiGenerate {
    generatorName.set("spring")
    inputSpec.set("$rootDir/src/main/resources/static/pet-service-api.yaml".replace("\\", "/"))
    outputDir.set(layout.buildDirectory.dir("generated").get().asFile.path)
    apiPackage.set("org.openapi.example.api")
    invokerPackage.set("org.openapi.example.invoker")
    modelPackage.set("org.openapi.example.model")
    configOptions.set(
        mapOf(
            "openApiNullable" to "false",
            "dateLibrary" to "java8",
            "useJakartaEe" to "true",
            "interfaceOnly" to "true",
            "skipDefaultInterface" to "true"
        )
    )
}

java {
    toolchain { languageVersion = JavaLanguageVersion.of(17) }
}

configurations {
    compileOnly { extendsFrom(configurations.annotationProcessor.get()) }
}

checkstyle {
    toolVersion = libs.versions.checkstyle.get()
    configFile = file("$rootDir/src/main/java/com/example/petservice/configuration/checkstyle/Checkstyle.xml".replace("\\", "/"))
    isIgnoreFailures = true
}

tasks.withType<Checkstyle> {
    exclude(layout.buildDirectory.dir("generated").get().asFile.path)
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}

repositories { mavenCentral() }

dependencies {
    // Bundles
    implementation(libs.bundles.spring.web)
    implementation(libs.bundles.spring.data)
    implementation(libs.bundles.spring.messaging)
    implementation(libs.bundles.openapi)
    implementation(libs.bundles.infra)
    developmentOnly(libs.bundles.dev)
    testImplementation(libs.bundles.test)

    // Processors
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
    implementation(libs.mapstruct)
    annotationProcessor(libs.mapstruct.processor)
}

tasks.withType<Test> { useJUnitPlatform() }
tasks.named("compileJava") { dependsOn("openApiGenerate") }

sourceSets {
    getByName("main") {
        java { srcDir(layout.buildDirectory.dir("generated/src/main/java").get().asFile.path) }
    }
}