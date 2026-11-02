plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.mirror.app"
    compileSdk = 35
    defaultConfig {
        externalNativeBuild {
            cmake {
                cppFlags += "-std=c11"
                abiFilters += listOf("arm64-v8a", "armeabi-v7a", "x86_64")
                arguments += listOf("-DMIRROR_OPENSSL_ROOT=${project.findProperty("mirrorOpenSslRoot") ?: System.getenv("MIRROR_OPENSSL_ROOT") ?: ""}")
            }
        }
        applicationId = "com.mirror.app"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"
    }
    buildTypes {
        debug { applicationIdSuffix = ".debug" }
        release {
            isMinifyEnabled = false
            // Configure signing via a private keystore; never commit credentials.
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }
    ndkVersion = "27.2.12479018"
}
