cmake_minimum_required(VERSION 3.22.1)
project("nexclip_native")

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# 项目源码
file(GLOB_RECURSE SOURCES
    "*.c"
    "*.cpp"
)

# 安全模块源码（从security目录）
set(SECURITY_DIR "${CMAKE_SOURCE_DIR}/../../../../security")
file(GLOB SECURITY_SOURCES "${SECURITY_DIR}/*.c")

add_library(nexclip_native SHARED
    ${SOURCES}
    ${SECURITY_SOURCES}
)

target_include_directories(nexclip_native PRIVATE
    ${CMAKE_SOURCE_DIR}
    ${SECURITY_DIR}
)

# ===== opencv-mobile =====
set(OpenCV_DIR "${CMAKE_SOURCE_DIR}/third_party/opencv-mobile")
if(EXISTS "${OpenCV_DIR}/sdk/native/jni")
    include_directories("${OpenCV_DIR}/sdk/native/jni/include")
    link_directories("${OpenCV_DIR}/sdk/native/jni/libs/${ANDROID_ABI}")
    target_link_libraries(nexclip_native opencv_java4)
    target_compile_definitions(nexclip_native PRIVATE HAS_OPENCV=1)
endif()

# ===== ncnn =====
set(ncnn_DIR "${CMAKE_SOURCE_DIR}/third_party/ncnn")
if(EXISTS "${ncnn_DIR}/${ANDROID_ABI}/lib/libncnn.a")
    include_directories("${ncnn_DIR}/${ANDROID_ABI}/include")
    target_link_libraries(nexclip_native
        "${ncnn_DIR}/${ANDROID_ABI}/lib/libncnn.a")
    target_compile_definitions(nexclip_native PRIVATE HAS_NCNN=1)
endif()

# ===== MNN =====
set(MNN_DIR "${CMAKE_SOURCE_DIR}/third_party/mnn")
if(EXISTS "${MNN_DIR}/${ANDROID_ABI}/lib/libMNN.so")
    include_directories("${MNN_DIR}/include")
    link_directories("${MNN_DIR}/${ANDROID_ABI}/lib")
    target_link_libraries(nexclip_native MNN MNN_Express)
    target_compile_definitions(nexclip_native PRIVATE HAS_MNN=1)
endif()

# ===== OpenSSL =====
set(OPENSSL_DIR "${CMAKE_SOURCE_DIR}/openssl")
include_directories("${OPENSSL_DIR}/include")
if(EXISTS "${OPENSSL_DIR}/${ANDROID_ABI}/libcrypto.a")
    target_link_libraries(nexclip_native
        "${OPENSSL_DIR}/${ANDROID_ABI}/libcrypto.a"
        "${OPENSSL_DIR}/${ANDROID_ABI}/libssl.a")
endif()

# ===== RNNoise =====
set(RNNOISE_DIR "${CMAKE_SOURCE_DIR}/third_party/rnnoise")
if(EXISTS "${RNNOISE_DIR}/${ANDROID_ABI}/lib/librnnoise.a")
    include_directories("${RNNOISE_DIR}/include")
    target_link_libraries(nexclip_native
        "${RNNOISE_DIR}/${ANDROID_ABI}/lib/librnnoise.a")
    target_compile_definitions(nexclip_native PRIVATE HAS_RNNOISE=1)
endif()

# 系统库
find_library(log-lib log)
find_library(android-lib android)
find_library(jnigraphics-lib jnigraphics)
find_library(gles-lib GLESv3)
find_library(egl-lib EGL)

target_link_libraries(nexclip_native
    ${log-lib}
    ${android-lib}
    ${jnigraphics-lib}
    ${gles-lib}
    ${egl-lib}
)
    # 抑制OpenSSL弃用API警告
    target_compile_definitions(${CMAKE_PROJECT_NAME} PRIVATE OSSL_SUPPRESS_DEPRECATED=1)
