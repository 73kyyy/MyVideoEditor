add_library(self_verify SHARED
    self_verify.cpp
    crc_checker.cpp
    text_segment_hash.cpp
    cross_so_verifier.cpp
)
# OpenSSL headers and libraries
set(OPENSSL_INC "${CMAKE_CURRENT_SOURCE_DIR}/../../../../../../../app/src/main/cpp/openssl/include")
set(OPENSSL_LIB "${CMAKE_CURRENT_SOURCE_DIR}/../../../../../../../app/src/main/cpp/openssl/${ANDROID_ABI}")

if(EXISTS "${OPENSSL_INC}/openssl/ssl.h")
    include_directories("${OPENSSL_INC}")
    if(EXISTS "${OPENSSL_LIB}/libcrypto.a")
        target_link_libraries(self_verify log android "${OPENSSL_LIB}/libcrypto.a" "${OPENSSL_LIB}/libssl.a")
    else()
        target_link_libraries(self_verify log android)
    endif()
else()
    target_link_libraries(self_verify log android)
endif()
add_definitions(-DOSSL_SUPPRESS_DEPRECATED=1)
