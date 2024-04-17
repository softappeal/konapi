// precondition: copy contents of libraryPath to your project

kotlin {
    linuxArm64 {
        binaries {
            executable(listOf(RELEASE)) {
                entryPoint = "ch.softappeal.konapi.app.main"
                linkerOpts += rootProject.extra["libraryPath"] as String
            }
        }
    }
    sourceSets {
        commonMain {
            dependencies {
                implementation(rootProject)
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.bundles.ktor.server)
            }
        }
    }
}
