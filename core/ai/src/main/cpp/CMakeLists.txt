cmake_minimum_required(VERSION 3.22)
project(core_ai)

add_library(core_ai SHARED
    sam2_inference.cpp
    esrgan_inference.cpp
    rife_inference.cpp
    whisper_inference.cpp
    denoise_rnnoise.cpp
    demucs_inference.cpp
    model_decrypt.cpp
)

target_link_libraries(core_ai log android)
