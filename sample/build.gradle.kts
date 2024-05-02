import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

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
                linkerOpts += "-L$rootDir/src/nativeInterop/cinterop/libs" // specifies dir for linking with needed libs
            }
        }
    }
    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    compilerOptions {
        allWarningsAsErrors = true
    }
    sourceSets {
        commonMain {
            dependencies {
                implementation("ch.softappeal.konapi:konapi:2.0.0")
                // implementation(rootProject)
            }
        }
    }
}
