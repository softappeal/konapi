import kotlin.io.path.Path
import kotlin.io.path.forEachDirectoryEntry
import kotlin.io.path.nameWithoutExtension

plugins {
    alias(libs.plugins.multiplatform)
}

@Suppress("SpellCheckingInspection")
kotlin {
    linuxArm64 {
        val cInterop = "src/nativeInterop/cInterop"
        compilations["main"].apply {
            explicitApi()
            kotlinOptions {
                allWarningsAsErrors = true
                freeCompilerArgs += "-opt-in=kotlinx.cinterop.ExperimentalForeignApi"
            }
            cinterops {
                // https://kotlinlang.org/docs/native-c-interop.html
                // https://kotlinlang.org/docs/native-app-with-c-and-libcurl.html
                Path(cInterop).forEachDirectoryEntry(glob = "*.def") { create(it.nameWithoutExtension) }
            }
            defaultSourceSet.dependencies {
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.bundles.ktor.server)
                implementation(kotlin("test"))
            }
        }
        binaries.executable(listOf(RELEASE)) {
            entryPoint = "ch.softappeal.kopi.test.main"
            // entryPoint = "ch.softappeal.kopi.app.main"
            linkerOpts.add("-L$cInterop/libs")
        }
    }
}

repositories {
    mavenCentral()
}
