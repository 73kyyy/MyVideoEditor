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

# ===== 零风险编译加固 =====
target_compile_options(secure_inference PRIVATE
    -Wall -Wextra -Wformat=2 -Werror=format-security
    -fvisibility=hidden -fvisibility-inlines-hidden
    -ffunction-sections -fdata-sections
    -flto
    $<$<CONFIG:Release>:-O3>
)

# ARM64 PAC+BTI（不支持硬件自动为NOP，零风险）
if(ANDROID_ABI STREQUAL "arm64-v8a")
    target_compile_options(secure_inference PRIVATE -mbranch-protection=standard)
endif()

# ===== 链接器加固 =====
target_link_options(secure_inference PRIVATE
    -Wl,--gc-sections
    -Wl,--strip-all
    -Wl,--exclude-libs,ALL
    -Wl,--version-script,${CMAKE_CURRENT_SOURCE_DIR}/export_symbols.map
    -flto
    $<$<CONFIG:Release>:-O3>
)
set_target_properties(secure_inference PROPERTIES LINK_DEPENDS ${CMAKE_CURRENT_SOURCE_DIR}/export_symbols.map)
