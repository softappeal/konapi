plugins {
    alias(libs.plugins.multiplatform)
}

kotlin {
    linuxArm64 {
        compilations["main"].apply {
            cinterops {
                create("gpiod") {
                    includeDirs("src/nativeInterop/cinterop/headers/include/")
                }
            }
            compilerOptions
                .options
                .freeCompilerArgs
                .add("-opt-in=kotlinx.cinterop.ExperimentalForeignApi")
        }
        binaries {
            executable(listOf(RELEASE)) {
                entryPoint = "ch.softappeal.kopi.test.main"
            }
        }
    }

    targets.all {
        compilations.all {
            explicitApi()
            kotlinOptions {
                allWarningsAsErrors = true
            }
        }
    }

    sourceSets {
        val linuxArm64Main by getting {
            dependencies {
                implementation(libs.kotlinx.coroutines.core)
                implementation(kotlin("test"))
            }
        }
    }
}

repositories {
    mavenCentral()
}
