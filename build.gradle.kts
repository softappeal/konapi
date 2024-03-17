plugins {
    alias(libs.plugins.multiplatform)
}

@Suppress("SpellCheckingInspection")
kotlin {
    linuxArm64 {
        compilations["main"].apply {
            explicitApi()
            kotlinOptions {
                allWarningsAsErrors = true
                freeCompilerArgs += "-opt-in=kotlinx.cinterop.ExperimentalForeignApi"
            }
            cinterops {
                // https://kotlinlang.org/docs/native-c-interop.html
                // https://kotlinlang.org/docs/native-app-with-c-and-libcurl.html
                val gpiod by creating
                val i2c by creating
            }
            defaultSourceSet.dependencies {
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.bundles.ktor.server)
                implementation(kotlin("test"))
            }
        }
        binaries.executable(listOf(RELEASE)) {
            entryPoint = "ch.softappeal.kopi.test.main"
            linkerOpts.add("-Lsrc/nativeInterop/cInterop/libs")
        }
    }
}

repositories {
    mavenCentral()
}
