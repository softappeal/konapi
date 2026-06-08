kotlin {
    linuxArm64 {
        binaries {
            executable(listOf(RELEASE)) { // creates "./build/bin/linuxArm64/releaseExecutable/sample.kexe"
                entryPoint = "sample.main"
                linkerOpts += "-L$rootDir/konapi/cinterop/libs" // specifies dir for linking with needed libs
            }
        }
    }
    sourceSets {
        commonMain {
            dependencies {
                // implementation("ch.softappeal.konapi:konapi:<VERSION>")
                implementation(project(":konapi"))
            }
        }
    }
}
