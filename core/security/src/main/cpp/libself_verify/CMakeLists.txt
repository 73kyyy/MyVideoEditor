add_library(self_verify SHARED
    self_verify.cpp
    crc_checker.cpp
    text_segment_hash.cpp
    cross_so_verifier.cpp
)
target_link_libraries(self_verify log android)

# OpenSSL headers
set(OPENSSL_INC "${CMAKE_CURRENT_SOURCE_DIR}/../../../../../../../app/src/main/cpp/openssl/include")
if(EXISTS "${OPENSSL_INC}/openssl/ssl.h")
    include_directories("${OPENSSL_INC}")
endif()
    # 抑制OpenSSL弃用API警告
    target_compile_definitions(${CMAKE_PROJECT_NAME} PRIVATE OSSL_SUPPRESS_DEPRECATED=1)
