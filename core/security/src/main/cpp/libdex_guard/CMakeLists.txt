add_library(dex_guard SHARED
    dex_guard.cpp
    dex_decryptor.cpp
    function_level_decrypt.cpp
    dex_fragment_loader.cpp
    class_name_obfuscator.cpp
    anti_dump.cpp
)
# OpenSSL headers and libraries
set(OPENSSL_INC "${CMAKE_CURRENT_SOURCE_DIR}/../../../../../../../app/src/main/cpp/openssl/include")
set(OPENSSL_LIB "${CMAKE_CURRENT_SOURCE_DIR}/../../../../../../../app/src/main/cpp/openssl/${ANDROID_ABI}")

if(EXISTS "${OPENSSL_INC}/openssl/ssl.h")
    include_directories("${OPENSSL_INC}")
    if(EXISTS "${OPENSSL_LIB}/libcrypto.a")
        target_link_libraries(dex_guard log android "${OPENSSL_LIB}/libcrypto.a" "${OPENSSL_LIB}/libssl.a")
    else()
        target_link_libraries(dex_guard log android)
    endif()
else()
    target_link_libraries(dex_guard log android)
endif()
add_definitions(-DOSSL_SUPPRESS_DEPRECATED=1)
