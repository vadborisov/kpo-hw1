plugins {
    application
    java
    checkstyle
    jacoco
}

group = "ru.hse"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

application {
    mainClass = "ru.hse.vyshkat.Main"
}

tasks.named<JavaExec>("run") {
    standardInput = System.`in`
}

dependencies {
    implementation("org.springframework:spring-context:7.0.9")
    testImplementation(platform("org.junit:junit-bom:5.13.4"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.mockito:mockito-junit-jupiter:5.20.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}

tasks.test {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport)
}

jacoco {
    toolVersion = "0.8.14"
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        html.required = true
        xml.required = true
        csv.required = false
    }
}

// Use the instructor's Sun rules, with the same documentation exceptions.
val sunChecks = configurations.detachedConfiguration(
    dependencies.create("com.puppycrawl.tools:checkstyle:14.1.0")
).apply {
    isTransitive = false
}

val checkstyleConfig = layout.buildDirectory.file("checkstyle/sun_checks.xml")
val prepareCheckstyleConfig = tasks.register("prepareCheckstyleConfig") {
    inputs.files(sunChecks)
    outputs.file(checkstyleConfig)
    doLast {
        val rules = resources.text.fromArchiveEntry(sunChecks, "sun_checks.xml")
            .asString()
            .replace("<module name=\"JavadocPackage\"/>", "")
            .replace("<module name=\"JavadocVariable\"/>", "")
            // Boundary values in tests describe the scenario directly.
            .replace(
                "<module name=\"Checker\">",
                """<module name="Checker">
                    <module name="SuppressionSingleFilter">
                        <property name="checks" value="MagicNumber"/>
                        <property name="files" value="[/\\]src[/\\]test[/\\]"/>
                    </module>"""
            )
        checkstyleConfig.get().asFile.apply {
            parentFile.mkdirs()
            writeText(rules)
        }
    }
}

checkstyle {
    toolVersion = "14.1.0"
    config = resources.text.fromFile(checkstyleConfig.get().asFile)
    isIgnoreFailures = false
    maxWarnings = 0
}

tasks.withType<Checkstyle>().configureEach {
    dependsOn(prepareCheckstyleConfig)
    reports {
        html.required = true
        xml.required = false
    }
}
