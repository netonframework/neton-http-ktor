plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    `maven-publish`
}

group = "com.netonstream"
version = providers.gradleProperty("adapterVersion").getOrElse("1.0.0-beta25")
val netonVersion = providers.gradleProperty("netonVersion").getOrElse("1.0.0-beta25")

repositories {
    providers.gradleProperty("neton.repository").orNull?.let { path ->
        exclusiveContent {
            forRepository { maven { url = uri(path) } }
            filter { includeModuleByRegex("com[.]netonstream", "neton-.*") }
        }
    }
}

kotlin {
    @OptIn(org.jetbrains.kotlin.gradle.dsl.abi.ExperimentalAbiValidation::class)
    abiValidation { keepLocallyUnsupportedTargets.set(false) }
    if (org.gradle.internal.os.OperatingSystem.current().isMacOsX) {
        val sdk = providers.exec { commandLine("xcrun", "--sdk", "macosx", "--show-sdk-path") }
            .standardOutput.asText.get().trim()
        targets.withType<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget>().configureEach {
            if (konanTarget.family.isAppleFamily) binaries.all { linkerOpts("-L$sdk/usr/lib/swift") }
        }
    }
}

publishing { repositories { maven { name = "stagingLocal"; url = uri(layout.buildDirectory.dir("staging-repo")) } } }
tasks.withType<org.gradle.api.publish.maven.tasks.AbstractPublishToMaven>().configureEach {
    doFirst { require(providers.gradleProperty("neton.local").orNull != "true") { "Publish only against packaged Neton dependencies" } }
}

repositories {
    mavenCentral()
}

kotlin {
    macosArm64()
    macosX64()
    linuxX64()
    linuxArm64()
    mingwX64()

    sourceSets {
        val nativeMain by creating {
            dependsOn(commonMain.get())
        }
        val posixMain by creating { dependsOn(nativeMain) }
        // CIO on every POSIX target. Darwin failed the client conformance suite
        // (merged Set-Cookie, buffered chunked bodies, ignored cancellation).
        val macosMain by creating { dependsOn(posixMain) }
        val linuxMain by creating { dependsOn(posixMain) }
        posixMain.dependencies { implementation(libs.ktor.client.cio) }
        macosArm64Main.get().dependsOn(macosMain)
        macosX64Main.get().dependsOn(macosMain)
        linuxX64Main.get().dependsOn(linuxMain)
        linuxArm64Main.get().dependsOn(linuxMain)
        mingwX64Main.get().apply { dependsOn(nativeMain); dependencies { implementation(libs.ktor.client.winhttp) } }

        // The client conformance suite needs the contract layer's ScriptedOrigin,
        // which only exists on POSIX targets.
        val posixTest by creating {
            dependsOn(commonTest.get())
        }
        macosArm64Test.get().dependsOn(posixTest)
        macosX64Test.get().dependsOn(posixTest)
        linuxX64Test.get().dependsOn(posixTest)
        linuxArm64Test.get().dependsOn(posixTest)

        commonMain {
            dependencies {
                api("com.netonstream:neton-http:$netonVersion")
                api("com.netonstream:neton-core:$netonVersion")
                implementation("com.netonstream:neton-logging:$netonVersion")
                implementation(libs.ktor.io)
                implementation(libs.ktor.http.cio)
                implementation(libs.ktor.server.core)
                implementation(libs.ktor.server.cio)
                implementation(libs.ktor.server.content.negotiation)
                implementation(libs.ktor.serialization.kotlinx.json)
                implementation(libs.ktor.server.sessions)
                implementation(libs.ktor.server.cors)
                implementation(libs.kotlinx.coroutines.core)
                api(libs.ktor.client.core)
            }
        }
        commonTest {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.kotlinx.coroutines.test)
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.ktor.client.mock)
            }
        }
    }
}
