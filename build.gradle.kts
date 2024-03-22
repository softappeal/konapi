import org.jetbrains.kotlin.gradle.tasks.KotlinNativeLink
import kotlin.io.path.Path
import kotlin.io.path.forEachDirectoryEntry
import kotlin.io.path.nameWithoutExtension

plugins {
    alias(libs.plugins.multiplatform)
}

val cInterop = "src/nativeInterop/cInterop"

@Suppress("SpellCheckingInspection")
kotlin {
    jvm()

    linuxArm64 {
        compilations["main"].apply {
            kotlinOptions.freeCompilerArgs += "-opt-in=kotlinx.cinterop.ExperimentalForeignApi"
            cinterops {
                // https://kotlinlang.org/docs/native-c-interop.html
                // https://kotlinlang.org/docs/native-app-with-c-and-libcurl.html
                Path(cInterop).forEachDirectoryEntry(glob = "*.def") { create(it.nameWithoutExtension) }
            }
        }
    }

    targets.all {
        compilations.all {
            explicitApi()
            kotlinOptions.allWarningsAsErrors = true
        }
    }

    sourceSets {
        val commonTest by getting {
            dependencies {
                implementation(libs.kotlinx.coroutines.core)
                implementation(kotlin("test"))
            }
        }
        val linuxArm64Main by getting {
            dependencies {
                implementation(libs.kotlinx.coroutines.core)
            }
        }
        val linuxArm64Test by getting {
            dependencies {
                implementation(libs.bundles.ktor.server)
            }
        }
    }
}

// see https://youtrack.jetbrains.com/issue/KT-43996
tasks.named("linkDebugTestLinuxArm64", type = KotlinNativeLink::class) {
    binary.linkerOpts("-L$cInterop/libs")
}

tasks.named("build") {
    dependsOn("linkDebugTestLinuxArm64")
}

repositories {
    mavenCentral()
}
