import org.jetbrains.kotlin.gradle.tasks.KotlinNativeLink
import kotlin.io.path.Path
import kotlin.io.path.forEachDirectoryEntry
import kotlin.io.path.nameWithoutExtension

plugins {
    alias(libs.plugins.multiplatform)
}

val libraries = libs

allprojects {
    apply(plugin = "org.jetbrains.kotlin.multiplatform")
    kotlin {
        jvm()
        targets.all {
            compilations.all {
                kotlinOptions.allWarningsAsErrors = true
            }
        }
    }
    repositories {
        mavenCentral()
    }
}

val cInterop = "src/nativeInterop/cInterop"
val libraryPath = "-L$cInterop/libs"

kotlin {
    linuxArm64 {
        compilations["main"].cinterops {
            // https://kotlinlang.org/docs/native-c-interop.html
            // https://kotlinlang.org/docs/native-app-with-c-and-libcurl.html
            Path(cInterop).forEachDirectoryEntry(glob = "*.def") { create(it.nameWithoutExtension) }
        }
    }
    targets.all {
        compilations.all {
            explicitApi()
        }
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libraries.kotlinx.coroutines.core)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}

// see https://youtrack.jetbrains.com/issue/KT-43996
tasks.named("linkDebugTestLinuxArm64", type = KotlinNativeLink::class) {
    binary.linkerOpts(libraryPath)
}

tasks.named("build") {
    dependsOn("linkDebugTestLinuxArm64")
}

project("app") {
    kotlin {
        linuxArm64 {
            binaries {
                executable(listOf(RELEASE)) {
                    entryPoint = "ch.softappeal.kopi.app.main"
                    linkerOpts += libraryPath
                }
            }
        }
        sourceSets {
            val commonMain by getting {
                dependencies {
                    implementation(project(":"))
                    implementation(libraries.kotlinx.coroutines.core)
                    implementation(libraries.bundles.ktor.server)
                }
            }
        }
    }
}
