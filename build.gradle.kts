import org.jetbrains.kotlin.gradle.tasks.KotlinNativeLink
import kotlin.io.path.Path
import kotlin.io.path.forEachDirectoryEntry
import kotlin.io.path.nameWithoutExtension

plugins {
    alias(libs.plugins.multiplatform)
}

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
val libraryPath by extra("-L$cInterop/libs")

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
        commonMain {
            dependencies {
                implementation(libs.kotlinx.coroutines.core)
            }
        }
        commonTest {
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
