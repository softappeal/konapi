import org.jetbrains.kotlin.gradle.tasks.KotlinNativeLink
import kotlin.io.path.Path
import kotlin.io.path.forEachDirectoryEntry
import kotlin.io.path.nameWithoutExtension

val cinteropPath = "$projectDir/cinterop"

kotlin {
    linuxArm64 {
        compilations["main"].cinterops {
            // https://kotlinlang.org/docs/native-c-interop.html
            // https://kotlinlang.org/docs/native-app-with-c-and-libcurl.html
            Path(cinteropPath).forEachDirectoryEntry(glob = "*.def") {
                create(it.nameWithoutExtension) { definitionFile.set(file(it)) }
            }
        }
    }
    explicitApi()
    sourceSets {
        commonTest {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        jvmMain {
            dependencies {
                implementation(kotlin("reflect"))
            }
        }
    }
}

// see https://youtrack.jetbrains.com/issue/KT-43996
tasks.named("linkDebugTestLinuxArm64", type = KotlinNativeLink::class) {
    binary.linkerOpts("-L$cinteropPath/libs")
}

tasks.named("build") {
    dependsOn("linkDebugTestLinuxArm64")
}

mavenPublishing {
    publishToMavenCentral()
    signAllPublications()
    group = "ch.softappeal.konapi"
    pom {
        name.set(project.name)
        description.set("Kotlin Native for Raspberry Pi")
        url.set("https://github.com/softappeal/konapi")
        licenses { license { name.set("BSD-3-Clause") } }
        scm { url.set("https://github.com/softappeal/konapi") }
        organization { name.set("softappeal GmbH Switzerland") }
        developers { developer { name.set("Angelo Salvade") } }
    }
}
