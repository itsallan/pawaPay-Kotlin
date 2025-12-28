import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties


plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinx.serialization)
    id("com.codingfeline.buildkonfig") version "0.17.1"
    id("maven-publish")
}

val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) {
        file.inputStream().use { load(it) }
    }
}

buildkonfig {
    packageName = "io.dala.pawapaykotlin"
    defaultConfigs {
        buildConfigField(
            com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING,
            "API_TOKEN",
            System.getenv("PAWAPAY_API_TOKEN") ?: localProperties.getProperty("PAWAPAY_API_TOKEN") ?: ""
        )
        buildConfigField(
            com.codingfeline.buildkonfig.compiler.FieldSpec.Type.BOOLEAN,
            "IS_SANDBOX",
            (System.getenv("IS_SANDBOX")?.toBoolean()
                ?: localProperties.getProperty("IS_SANDBOX")?.toBoolean()
                ?: true).toString()
        )
    }
}

kotlin {
    androidTarget {
        publishLibraryVariants("release")
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    withSourcesJar()
//
//    listOf(
//        iosArm64(),
//        iosSimulatorArm64()
//    ).forEach { iosTarget ->
//        iosTarget.binaries.framework {
//            baseName = "Shared"
//            isStatic = true
//        }
//    }
    
    jvm()
    
    sourceSets {
        androidMain.dependencies {
            //android engine
            implementation(libs.ktor.client.okhttp)
            //koin
            implementation(libs.koin.androidx.compose)
        }
        commonMain.dependencies {
            //ktor core & serialization
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.kotlinx.serialization.json)
            //dates
            implementation(libs.kotlinx.datetime)
            //koin
            api(libs.koin.core)
            implementation(libs.koin.compose.multiplatform)
            implementation(libs.koin.compose.viewmodel)
            implementation(libs.ktor.client.logging)
        }
        iosMain.dependencies {
            //engine for iOS
            implementation(libs.ktor.client.darwin)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

afterEvaluate {
    publishing {
        publications {
            val kotlinMultiplatform = components.findByName("kotlinMultiplatform")

            create<MavenPublication>("maven") {
                groupId = "com.github.itsallan"
                artifactId = "shared"
                version = "1.0.0-alpha06"

                if (kotlinMultiplatform != null) {
                    from(kotlinMultiplatform)
                }
            }
        }
    }
}

configurations.matching { it.name.contains("ios", ignoreCase = true) }.all {
    attributes {
        attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage::class.java, "java-api"))
    }
}

android {
    namespace = "io.dala.pawapaykotlin.shared"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
}
