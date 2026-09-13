import com.strumenta.antlrkotlin.gradle.AntlrKotlinTask

plugins {
    kotlin("jvm") version "2.4.10"
    kotlin("plugin.serialization") version "2.4.10"
    id("com.strumenta.antlr-kotlin") version "1.0.12"
    application
}

group = "com.luizlemetech"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.strumenta:antlr-kotlin-runtime-jvm:1.0.12")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(21)
}

val pkgName = "com.luizlemetech.formula.antlr"

val generateKotlinGrammarSource =
    tasks.register<AntlrKotlinTask>("generateKotlinGrammarSource") {
        dependsOn("cleanGenerateKotlinGrammarSource")

        source = fileTree(layout.projectDirectory.dir("antlr")) {
            include("**/*.g4")
        }

        packageName = pkgName
        arguments = listOf("-visitor", "-no-listener")

        outputDirectory =
            layout.buildDirectory
                .dir("generatedAntlr/${pkgName.replace(".", "/")}")
                .get()
                .asFile
    }

sourceSets {
    main {
        kotlin.srcDir(generateKotlinGrammarSource)
    }
}

tasks.compileKotlin {
    dependsOn(generateKotlinGrammarSource)
}

application {
    mainClass.set("com.luizlemetech.formula.MainKt")
}

tasks.test {
    useJUnitPlatform()
}