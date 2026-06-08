cmake_minimum_required(VERSION 3.22)
project(core_ai)

# ── ONNX Runtime ──────────────────────────────────────────────────────────────
# The shared library (libonnxruntime.so) is provided by the onnxruntime-android
# AAR.  Gradle extracts native libs from the AAR and packages them into the APK.
# For CMake linking, we pass the library directory via ONNXRUNTIME_LIB_DIR.
# The header directory is passed via ONNXRUNTIME_INCLUDE_DIR.
#
# If the library directory is not set (e.g., during CMake configure before the
# AAR is fully resolved), we fall back to a stub mode where ONNX Runtime calls
# are compiled but the library is not linked — the actual loading happens at
# runtime via dlopen from the APK's lib directory.

set(USE_ONNX_RUNTIME FALSE)

if(DEFINED ONNXRUNTIME_INCLUDE_DIR AND DEFINED ONNXRUNTIME_LIB_DIR)
    if(EXISTS "${ONNXRUNTIME_LIB_DIR}/libonnxruntime.so")
        set(USE_ONNX_RUNTIME TRUE)
        message(STATUS "ONNX Runtime found: ${ONNXRUNTIME_LIB_DIR}")
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
        ${ONNXRUNTIME_LIB_DIR}/libonnxruntime.so
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
