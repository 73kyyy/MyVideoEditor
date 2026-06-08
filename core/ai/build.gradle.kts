plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    ndkVersion = "26.1.10909125"
    namespace = "com.myvideo.editor.core.ai"
    compileSdk = 34

    defaultConfig {
        minSdk = 26
        targetSdk = 34
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        externalNativeBuild {
            cmake {
                cppFlags += "-std=c++17"
                arguments += listOf(
                    "-DONNXRUNTIME_INCLUDE_DIR=${layout.buildDirectory.get()}/onnxruntime-headers"
                )
            }
        }
    }

    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation(project(":core:security"))
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // ONNX Runtime
    implementation("com.microsoft.onnxruntime:onnxruntime-android:1.16.3")

    // PyTorch Mobile

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
}

// ── Extract ONNX Runtime C++ headers from the AAR ────────────────────────────
// The onnxruntime-android AAR bundles native headers under "headers/".
// This task unzips them into the build directory so that CMake can find them
// via the ONNXRUNTIME_INCLUDE_DIR argument set above.
val onnxruntimeHeadersDir = layout.buildDirectory.dir("onnxruntime-headers").map { it.asFile }

tasks.register("extractOnnxRuntimeHeaders") {
    description = "Extract ONNX Runtime C++ headers from the AAR into the build directory"
    group = "build"

    outputs.dir(onnxruntimeHeadersDir)

    doLast {
        val outputDir = onnxruntimeHeadersDir.get()
        outputDir.mkdirs()

        val compileClasspath = configurations.getByName("releaseCompileClasspath")
        val aarFiles = compileClasspath.files.filter {
            it.name.contains("onnxruntime") && it.extension == "aar"
        }

        if (aarFiles.isEmpty()) {
            logger.warn("ONNX Runtime AAR not found in releaseCompileClasspath – " +
                    "header extraction skipped. CMake may fail to find headers.")
            return@doLast
        }

        aarFiles.forEach { aar ->
            logger.lifecycle("Extracting ONNX Runtime headers from ${aar.name}")
            copy {
                from(zipTree(aar)) {
                    include("headers/**")
                    eachFile {
                        // Strip the "headers/" prefix so files land directly in outputDir
                        relativePath = RelativePath(
                            true,
                            *relativePath.segments.drop(1).toTypedArray()
                        )
                    }
                }
                into(outputDir)
            }
        }
    }
}

// Ensure headers are extracted before CMake configures / builds
afterEvaluate {
    tasks.filter { it.name.contains("externalNativeBuild") }.forEach { task ->
        task.dependsOn("extractOnnxRuntimeHeaders")
    }
}
