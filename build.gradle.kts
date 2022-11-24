import java.nio.charset.Charset

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.20-Beta"
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
}

repositories {
    mavenCentral()
}
val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "18"
}
val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = "18"
}

abstract class SetupTask : DefaultTask() {
    private var isIgnored: Boolean = true
    private var multiModule: Boolean = false

    @Option(option = "multiModule", description = "")
    fun setMulti(multiModule: String) {
        this.multiModule = multiModule.toBoolean()
        isIgnored = false
    }

    @TaskAction
    fun process() {
        if (!this.isIgnored) {
            if (this.multiModule) {
                File("./src").deleteRecursively()
            } else {
                File("./backend").deleteRecursively()
                File("./frontend").deleteRecursively()
                File("./settings.gradle.kts").writeText("", Charset.forName("utf8"))
            }
        }
    }
}

tasks {
    register<SetupTask>("setupWorkspace")
}