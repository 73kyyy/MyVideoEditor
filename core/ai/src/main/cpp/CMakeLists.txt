cmake_minimum_required(VERSION 3.22)
project(core_ai)

# ── ONNX Runtime ──────────────────────────────────────────────────────────────
# The shared library (libonnxruntime.so) is provided by the onnxruntime-android
# AAR.  AGP extracts it and adds it to the linker search path automatically.
find_library(ONNXRUNTIME_LIBRARY onnxruntime)

if(NOT ONNXRUNTIME_LIBRARY)
    message(FATAL_ERROR
        "Could not find libonnxruntime.so.  Make sure the onnxruntime-android "
        "AAR dependency is declared in build.gradle.kts and Gradle has synced.")
endif()

# Header directory – set from build.gradle.kts via -DONNXRUNTIME_INCLUDE_DIR=...
# The AAR ships headers under its "headers/" prefix; a Gradle task extracts
# them into the build directory before CMake configures.
if(NOT DEFINED ONNXRUNTIME_INCLUDE_DIR)
    message(WARNING
        "ONNXRUNTIME_INCLUDE_DIR is not set.  ONNX Runtime headers will not "
        "be found.  Add -DONNXRUNTIME_INCLUDE_DIR=<path> in build.gradle.kts "
        "externalNativeBuild.cmake.arguments.")
endif()

# ── Shared library ────────────────────────────────────────────────────────────
add_library(core_ai SHARED
    sam2_inference.cpp
    esrgan_inference.cpp
    rife_inference.cpp
    whisper_inference.cpp
    denoise_rnnoise.cpp
    demucs_inference.cpp
    model_decrypt.cpp
)

target_include_directories(core_ai PRIVATE
    ${CMAKE_CURRENT_SOURCE_DIR}
    ${ONNXRUNTIME_INCLUDE_DIR}
)

target_link_libraries(core_ai
    log
    android
    ${ONNXRUNTIME_LIBRARY}
)
