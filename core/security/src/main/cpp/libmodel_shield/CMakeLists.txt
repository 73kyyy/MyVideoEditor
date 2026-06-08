add_library(model_shield SHARED
    model_shield.cpp
    chunk_decryptor.cpp
    vfs_layer.cpp
    per_layer_weight_crypto.cpp
    inference_entry_guard.cpp
    output_perturbation.cpp
)
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
