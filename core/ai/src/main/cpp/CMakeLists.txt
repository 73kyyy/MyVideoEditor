cmake_minimum_required(VERSION 3.22)
project(secure_inference)

# OpenSSL路径（与security模块相同的模式）
set(OPENSSL_INC "${CMAKE_CURRENT_SOURCE_DIR}/../../../../../../../app/src/main/cpp/openssl/include")
set(OPENSSL_LIB "${CMAKE_CURRENT_SOURCE_DIR}/../../../../../../../app/src/main/cpp/openssl/${ANDROID_ABI}")

add_library(secure_inference SHARED
    secure_inference_jni.cpp
)

# 条件链接OpenSSL
if(EXISTS "${OPENSSL_INC}/openssl/ssl.h")
    include_directories("${OPENSSL_INC}")
    if(EXISTS "${OPENSSL_LIB}/libcrypto.a")
        target_link_libraries(secure_inference log android "${OPENSSL_LIB}/libcrypto.a")
        target_compile_definitions(secure_inference PRIVATE HAS_OPENSSL=1)
        message(STATUS "OpenSSL found, linking libcrypto.a - full crypto enabled")
    else()
        target_link_libraries(secure_inference log android)
        message(WARNING "OpenSSL lib not found, building with stub crypto - models will load unencrypted")
    endif()
else()
    target_link_libraries(secure_inference log android)
    message(WARNING "OpenSSL headers not found, building with stub crypto - models will load unencrypted")
endif()

# C++17标准
target_compile_features(secure_inference PRIVATE cxx_std_17)

# 优化选项
target_compile_options(secure_inference PRIVATE
    -fvisibility=hidden
    -ffunction-sections
    -fdata-sections
)

# 链接器优化：移除未使用的函数
target_link_options(secure_inference PRIVATE
    -Wl,--gc-sections
    -Wl,--strip-all
)
