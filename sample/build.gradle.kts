plugins {
    alias(libs.plugins.multiplatform)
}

repositories {
    mavenCentral()
}

kotlin {
    linuxArm64 {
        binaries {
            executable(listOf(RELEASE)) { // creates "./build/bin/linuxArm64/releaseExecutable/sample.kexe"
                entryPoint = "sample.main"
                @Suppress("SpellCheckingInspection")
                linkerOpts += "-Lsrc/nativeInterop/cinterop/libs" // specifies dir for linking with needed libs
            }
        }
    }
    sourceSets {
        commonMain {
            dependencies {
                implementation("ch.softappeal.konapi:konapi:2.0.0")
            }
        }
    }
}
