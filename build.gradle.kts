@file:Suppress("SpellCheckingInspection")

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeLink
import java.util.regex.Pattern
import kotlin.io.path.Path
import kotlin.io.path.forEachDirectoryEntry
import kotlin.io.path.nameWithoutExtension

plugins {
    alias(libs.plugins.multiplatform)
    `maven-publish`
    signing
}

repositories {
    mavenCentral()
}

val cinteropPath = "$projectDir/src/nativeInterop/cinterop"

tasks.register<Jar>("javadocJar") {
    archiveClassifier = "javadoc"
}

kotlin {
    jvm {
        mavenPublication {
            artifact(tasks["javadocJar"])
        }
    }
    linuxArm64 {
        compilations["main"].cinterops {
            // https://kotlinlang.org/docs/native-c-interop.html
            // https://kotlinlang.org/docs/native-app-with-c-and-libcurl.html
            Path(cinteropPath).forEachDirectoryEntry(glob = "*.def") { create(it.nameWithoutExtension) }
        }
    }
    explicitApi()
    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    compilerOptions {
        allWarningsAsErrors = true
    }
    sourceSets {
        commonTest {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        jvmMain {
            dependencies {
                implementation(kotlin("reflect"))
            }
        }
    }
}

// see https://youtrack.jetbrains.com/issue/KT-43996
tasks.named("linkDebugTestLinuxArm64", type = KotlinNativeLink::class) {
    binary.linkerOpts("-L$cinteropPath/libs")
}

tasks.named("build") {
    dependsOn("linkDebugTestLinuxArm64")
}

tasks.named("compileTestKotlinLinuxArm64") {
    dependsOn("signLinuxArm64Publication")
}

group = "ch.softappeal.konapi"

publishing {
    publications.withType<MavenPublication>().onEach { publication ->
        publication.pom {
            name = project.name
            description = "Kotlin Native for Raspberry Pi"
            url = "https://github.com/softappeal/konapi"
            licenses { license { name = "BSD-3-Clause" } }
            scm { url = "https://github.com/softappeal/konapi" }
            organization { name = "softappeal GmbH Switzerland" }
            developers { developer { name = "Angelo Salvade" } }
        }
    }
    repositories {
        maven {
            name = "ossrh"
            credentials(PasswordCredentials::class)
            url = uri("https://oss.sonatype.org/service/local/staging/deploy/maven2")
        }
    }
}

signing {
    sign(publishing.publications)
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
            .exclude("/test-files/")
            .exclude("src/nativeInterop/cinterop/headers/")
            .exclude("src/nativeInterop/cinterop/libs/")
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
