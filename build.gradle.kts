import org.jetbrains.dokka.gradle.engine.parameters.VisibilityModifier
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import java.util.regex.Pattern

plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.dokka)
    alias(libs.plugins.publish)
}

fun KotlinMultiplatformExtension.configureSourceSets() {
    sourceSets {
        commonMain {
            kotlin.srcDir("src")
        }
        commonTest {
            kotlin.srcDir("test")
        }
        jvmMain {
            kotlin.srcDir("src@jvm")
        }
        jvmTest {
            kotlin.srcDir("test@jvm")
        }
        linuxArm64Main {
            kotlin.srcDir("src@linuxArm64")
        }
        linuxArm64Test {
            kotlin.srcDir("test@linuxArm64")
        }
    }
}

allprojects {
    apply(plugin = "org.jetbrains.kotlin.multiplatform")
    apply(plugin = "org.jetbrains.dokka")
    apply(plugin = "com.vanniktech.maven.publish")
    repositories {
        mavenCentral()
    }
    kotlin {
        jvm()
        linuxArm64()
        compilerOptions {
            extraWarnings.set(true)
            freeCompilerArgs.add("-Xname-based-destructuring=complete")
            allWarningsAsErrors.set(true)
        }
        configureSourceSets()
    }
    dokka {
        dokkaPublications.html {
            failOnWarning.set(true)
        }
        dokkaSourceSets {
            configureEach {
                documentedVisibilities(VisibilityModifier.Public, VisibilityModifier.Protected)
            }
        }
    }
}

dependencies {
    dokka(project(":konapi"))
}

tasks.register("markers") {
    doLast {
        fun divider(type: Char) = println(type.toString().repeat(132))
        val fileTree = fileTree(".")
        fileTree
            .exclude("/.git/")
            .exclude(".DS_Store")
            .exclude("/.idea/")
            .exclude("/.gradle/")
            .exclude("/.kotlin/")
            .exclude("**/build/")
            .exclude("/konapi/test-files/")
            .exclude("/konapi/cinterop/headers/")
            .exclude("/konapi/cinterop/libs/")
        fun search(marker: String, help: String, abort: Boolean = false) {
            divider('=')
            println("= $marker - $help")
            val pattern = Pattern.compile("\\b$marker\\b", Pattern.CASE_INSENSITIVE)
            fileTree.visit {
                if (!isDirectory) {
                    var found = false
                    var number = 0
                    file.forEachLine { line ->
                        number++
                        if (pattern.matcher(line).find()) {
                            if (!found) {
                                divider('-')
                                println("+ $relativePath")
                            }
                            found = true
                            println("- $number: $line")
                            if (abort) throw Exception("abort marker $marker found")
                        }
                    }
                }
            }
        }
        search("FIXM" + "E", "not allowed for building a release", true)
        search("TOD" + "O", "under construction, yet a release can still be built")
        search("NOT" + "E", "important comment")
        divider('=')
    }
}
