add_library(crypto_core SHARED
    crypto_core.cpp
    aes_gcm.cpp
    key_derivation.cpp
    secure_random.cpp
)
target_link_libraries(crypto_core log android)

# OpenSSL headers
set(OPENSSL_INC "${CMAKE_CURRENT_SOURCE_DIR}/../../../../../../../app/src/main/cpp/openssl/include")
if(EXISTS "${OPENSSL_INC}/openssl/ssl.h")
    include_directories("${OPENSSL_INC}")
endif()
