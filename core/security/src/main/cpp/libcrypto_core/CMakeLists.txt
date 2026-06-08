add_library(crypto_core SHARED
    crypto_core.cpp
    aes_gcm.cpp
    key_derivation.cpp
    secure_random.cpp
)

# OpenSSL headers and libraries
set(OPENSSL_INC "${CMAKE_CURRENT_SOURCE_DIR}/../../../../../../../app/src/main/cpp/openssl/include")
set(OPENSSL_LIB "${CMAKE_CURRENT_SOURCE_DIR}/../../../../../../../app/src/main/cpp/openssl/${ANDROID_ABI}")

if(EXISTS "${OPENSSL_INC}/openssl/ssl.h")
    include_directories("${OPENSSL_INC}")
    message(STATUS "OpenSSL headers found: ${OPENSSL_INC}")

    # Link OpenSSL static libraries
    if(EXISTS "${OPENSSL_LIB}/libcrypto.a")
        target_link_libraries(crypto_core log android "${OPENSSL_LIB}/libcrypto.a" "${OPENSSL_LIB}/libssl.a")
        message(STATUS "OpenSSL libraries linked: ${OPENSSL_LIB}")
    else()
        target_link_libraries(crypto_core log android)
        message(WARNING "OpenSSL libraries NOT found at ${OPENSSL_LIB} - crypto will be stubs!")
    endif()
else()
    target_link_libraries(crypto_core log android)
    message(WARNING "OpenSSL headers NOT found - crypto will be stubs!")
endif()

# Suppress OpenSSL deprecated API warnings
add_definitions(-DOSSL_SUPPRESS_DEPRECATED=1)
