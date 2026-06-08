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
                    "-DONNXRUNTIME_INCLUDE_DIR=${layout.buildDirectory.get()}/onnxruntime-headers",
                    "-DONNXRUNTIME_LIB_DIR=${layout.buildDirectory.get()}/onnxruntime-libs/\${ANDROID_ABI}"
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

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
}

// ── Extract ONNX Runtime native libs + headers from the AAR ──────────────────
val onnxruntimeHeadersDir = layout.buildDirectory.dir("onnxruntime-headers").map { it.asFile }
val onnxruntimeLibsDir = layout.buildDirectory.dir("onnxruntime-libs").map { it.asFile }

tasks.register("extractOnnxRuntimeHeaders") {
    description = "Extract ONNX Runtime C++ headers and native libs from the AAR"
    group = "build"

    outputs.dir(onnxruntimeHeadersDir)
    outputs.dir(onnxruntimeLibsDir)

    doLast {
        val headersDir = onnxruntimeHeadersDir.get()
        val libsDir = onnxruntimeLibsDir.get()
        headersDir.mkdirs()
        libsDir.mkdirs()

        val compileClasspath = configurations.getByName("releaseCompileClasspath")
        val aarFiles = compileClasspath.files.filter {
            it.name.contains("onnxruntime") && it.extension == "aar"
        }

        if (aarFiles.isEmpty()) {
            logger.warn("ONNX Runtime AAR not found – header/lib extraction skipped. " +
                    "CMake will build in stub mode.")
            return@doLast
        }

        aarFiles.forEach { aar ->
            logger.lifecycle("Extracting ONNX Runtime from ${aar.name}")

            // Extract headers (headers/** → strip prefix)
            copy {
                from(zipTree(aar)) {
                    include("headers/**")
                    eachFile {
                        relativePath = RelativePath(
                            true,
                            *relativePath.segments.drop(1).toTypedArray()
                        )
                    }
                }
                into(headersDir)
            }

            // Extract native libs (jni/**/libonnxruntime.so → abi/libonnxruntime.so)
            copy {
                from(zipTree(aar)) {
                    include("jni/**/libonnxruntime.so")
                    eachFile {
                        // jni/arm64-v8a/libonnxruntime.so → arm64-v8a/libonnxruntime.so
                        relativePath = RelativePath(
                            true,
                            *relativePath.segments.drop(1).toTypedArray()
                        )
                    }
                }
                into(libsDir)
            }
        }

        // Verify extraction
        val headerCount = headersDir.walkTopDown().filter { it.extension == "h" }.count()
        val libCount = libsDir.walkTopDown().filter { it.name == "libonnxruntime.so" }.count()
        logger.lifecycle("ONNX Runtime: $headerCount headers, $libCount native libs extracted")
    }
}

// Ensure extraction happens before CMake configures / builds
afterEvaluate {
    tasks.filter { it.name.contains("externalNativeBuild") }.forEach { task ->
        task.dependsOn("extractOnnxRuntimeHeaders")
    }
}
