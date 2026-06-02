add_library(net_secure SHARED
    net_secure.cpp
    conscrypt_init.cpp
    cert_pinner.cpp
    request_signer.cpp
    response_decryptor.cpp
)
target_link_libraries(net_secure log android)

# OpenSSL headers
set(OPENSSL_INC "${CMAKE_CURRENT_SOURCE_DIR}/../../../../../../../app/src/main/cpp/openssl/include")
if(EXISTS "${OPENSSL_INC}/openssl/ssl.h")
    include_directories("${OPENSSL_INC}")
endif()
    # 抑制OpenSSL弃用API警告
    target_compile_definitions(${CMAKE_PROJECT_NAME} PRIVATE OSSL_SUPPRESS_DEPRECATED=1)
