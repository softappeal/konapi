kotlin {
    linuxArm64 {
        binaries {
            executable(listOf(RELEASE)) {
                entryPoint = "ch.softappeal.kopi.app.main"
                linkerOpts += rootProject.extra["libraryPath"] as String
            }
        }
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(rootProject)
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.bundles.ktor.server)
            }
        }
    }
}
