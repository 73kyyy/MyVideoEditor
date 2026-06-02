add_library(model_shield SHARED
    model_shield.cpp
    chunk_decryptor.cpp
    vfs_layer.cpp
    per_layer_weight_crypto.cpp
    inference_entry_guard.cpp
    output_perturbation.cpp
)
target_link_libraries(model_shield log android)

# OpenSSL headers
set(OPENSSL_INC "${CMAKE_CURRENT_SOURCE_DIR}/../../../../../../../app/src/main/cpp/openssl/include")
if(EXISTS "${OPENSSL_INC}/openssl/ssl.h")
    include_directories("${OPENSSL_INC}")
endif()
    # 抑制OpenSSL弃用API警告
    target_compile_definitions(${CMAKE_PROJECT_NAME} PRIVATE OSSL_SUPPRESS_DEPRECATED=1)
