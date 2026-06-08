cmake_minimum_required(VERSION 3.22)
project(core_ai)

# ── ONNX Runtime ──────────────────────────────────────────────────────────────
# The shared library (libonnxruntime.so) is provided by the onnxruntime-android
# AAR.  Gradle extracts native libs from the AAR into ONNXRUNTIME_LIB_DIR
# with ABI subdirectories (e.g., onnxruntime-libs/arm64-v8a/libonnxruntime.so).
# The header directory is passed via ONNXRUNTIME_INCLUDE_DIR.

set(USE_ONNX_RUNTIME FALSE)

if(DEFINED ONNXRUNTIME_INCLUDE_DIR AND DEFINED ONNXRUNTIME_LIB_DIR)
    # ANDROID_ABI is set by the Android CMake toolchain
    set(_ORT_LIB_PATH "${ONNXRUNTIME_LIB_DIR}/${ANDROID_ABI}/libonnxruntime.so")
    if(EXISTS "${_ORT_LIB_PATH}")
        set(USE_ONNX_RUNTIME TRUE)
        message(STATUS "ONNX Runtime found: ${_ORT_LIB_PATH}")
    else()
        message(WARNING "ONNX Runtime lib not found at ${_ORT_LIB_PATH} - building in stub mode")
    endif()
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
)

if(USE_ONNX_RUNTIME)
    target_include_directories(core_ai PRIVATE ${ONNXRUNTIME_INCLUDE_DIR})
    target_link_libraries(core_ai
        log
        android
        ${_ORT_LIB_PATH}
    )
    target_compile_definitions(core_ai PRIVATE USE_ONNX_RUNTIME=1)
    message(STATUS "Building with ONNX Runtime support")
else()
    # Stub mode: ONNX Runtime not available at CMake time.
    # The library will still compile but inference calls will return
    # error/null results. The Kotlin layer (InferenceSessionManager)
    # provides the actual ONNX Runtime functionality via Java API.
    target_link_libraries(core_ai log android)
    target_compile_definitions(core_ai PRIVATE USE_ONNX_RUNTIME=0)
    message(WARNING "ONNX Runtime library not found - building in stub mode. "
        "Kotlin InferenceSessionManager will handle ONNX inference.")
endif()
