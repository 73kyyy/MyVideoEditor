// NexClip - app/build.gradle.kts
// 编号1：构建期防护 + 编号60：供应链安全

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.myvideo.editor"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.myvideo.editor"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"

        // 编号1：Native构建
        externalNativeBuild {
            cmake {
                cppFlags += "-std=c++17 -O2 -fvisibility=hidden -fno-exceptions -fno-rtti"
                arguments += listOf(
                    "-DANDROID_STL=c++_static",
                    "-DCMAKE_BUILD_TYPE=Release"
                )
            }
        }

        // 编号1：ABI过滤
        ndk {
            abiFilters += listOf("arm64-v8a", "armeabi-v7a")
        }

        // 编号1：版本信息
        buildConfigField("String", "BUILD_TIME", "\"${System.currentTimeMillis()}\"")
        buildConfigField("String", "GIT_HASH", "\"local_build\"")
    }

    // 编号1：签名配置
    signingConfigs {
        create("release") {
            storeFile = file("../keystore/nexclip.jks")
            storePassword = System.getenv("KEYSTORE_PASSWORD") ?: ""
            keyAlias = "nexclip"
            keyPassword = System.getenv("KEY_PASSWORD") ?: ""
        }
        create("debug") {
            storeFile = file("../keystore/debug.jks")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
    }

    buildTypes {
        debug {
            isDebuggable = true
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("debug")
        }
        release {
            isDebuggable = false
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }

    // 编号1：CMake Native构建
    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    // 编号1：构建变体
    flavorDimensions += "tier"
    productFlavors {
        create("free") {
            dimension = "tier"
            applicationIdSuffix = ".free"
            versionNameSuffix = "-free"
        }
        create("pro") {
            dimension = "tier"
            applicationIdSuffix = ".pro"
            versionNameSuffix = "-pro"
        }
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
        jniLibs {
            useLegacyPackaging = false
        }
    }
}

// ===== 编号1+60：依赖版本锁定 =====
dependencies {
    // AndroidX - 固定版本
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation("androidx.navigation:navigation-compose:2.7.6")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.security:security-crypto:1.1.0-alpha06")

    // Compose - 固定版本
    implementation(platform("androidx.compose:compose-bom:2024.01.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.animation:animation")

    // 网络 - 固定版本
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // JSON - 固定版本
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")

    // 图片加载 - 固定版本
    implementation("io.coil-kt:coil-compose:2.5.0")

    // 视频处理 - 固定版本
    implementation("com.google.android.exoplayer:exoplayer-core:2.19.1")
    implementation("androidx.media3:media3-exoplayer:1.2.1")

    // 测试
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    debugImplementation("androidx.compose.ui:ui-tooling")
}
