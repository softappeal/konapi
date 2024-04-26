plugins {
    alias(libs.plugins.multiplatform)
}

repositories {
    mavenCentral()
}

kotlin {
    linuxArm64 {
        binaries {
            executable(listOf(RELEASE)) {
                entryPoint = "sample.main"
                @Suppress("SpellCheckingInspection")
                linkerOpts += "-Lsrc/nativeInterop/cinterop/libs"
            }
        }
    }
    targets.all {
        compilations.all {
            kotlinOptions.allWarningsAsErrors = true
        }
    }
    sourceSets {
        commonMain {
            dependencies {
                implementation("ch.softappeal.konapi:konapi:1.0.0")
            }
        }
    }
}
