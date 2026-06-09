add_library(model_shield SHARED
    model_shield.cpp
    chunk_decryptor.cpp
    vfs_layer.cpp
    per_layer_weight_crypto.cpp
    inference_entry_guard.cpp
    output_perturbation.cpp
)

# ===== 零风险编译加固 =====
target_compile_options(model_shield PRIVATE
    -Wall -Wextra -Wformat=2 -Werror=format-security
    -fvisibility=hidden -fvisibility-inlines-hidden
    -ffunction-sections -fdata-sections
    -flto
    $<$<CONFIG:Release>:-O3>
)
if(ANDROID_ABI STREQUAL "arm64-v8a")
    target_compile_options(model_shield PRIVATE -mbranch-protection=standard)
endif()
target_link_options(model_shield PRIVATE
    -Wl,--gc-sections -Wl,--strip-all -Wl,--exclude-libs,ALL
    -Wl,--version-script,${CMAKE_CURRENT_SOURCE_DIR}/export_symbols.map
    -flto $<$<CONFIG:Release>:-O3>
)
set_target_properties(model_shield PROPERTIES LINK_DEPENDS ${CMAKE_CURRENT_SOURCE_DIR}/export_symbols.map)

# OpenSSL headers and libraries
set(OPENSSL_INC "${CMAKE_CURRENT_SOURCE_DIR}/../../../../../../../app/src/main/cpp/openssl/include")
set(OPENSSL_LIB "${CMAKE_CURRENT_SOURCE_DIR}/../../../../../../../app/src/main/cpp/openssl/${ANDROID_ABI}")

if(EXISTS "${OPENSSL_INC}/openssl/ssl.h")
    include_directories("${OPENSSL_INC}")
    if(EXISTS "${OPENSSL_LIB}/libcrypto.a")
        target_link_libraries(model_shield log android "${OPENSSL_LIB}/libcrypto.a" "${OPENSSL_LIB}/libssl.a")
    else()
        target_link_libraries(model_shield log android)
    endif()
else()
    target_link_libraries(model_shield log android)
endif()
add_definitions(-DOSSL_SUPPRESS_DEPRECATED=1)
