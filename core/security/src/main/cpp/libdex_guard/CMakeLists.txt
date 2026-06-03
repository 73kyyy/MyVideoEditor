add_library(dex_guard SHARED
    dex_guard.cpp
    dex_decryptor.cpp
    function_level_decrypt.cpp
    dex_fragment_loader.cpp
    class_name_obfuscator.cpp
    anti_dump.cpp
)
target_link_libraries(dex_guard log android)

# OpenSSL headers
set(OPENSSL_INC "${CMAKE_CURRENT_SOURCE_DIR}/../../../../../../../app/src/main/cpp/openssl/include")
if(EXISTS "${OPENSSL_INC}/openssl/ssl.h")
    include_directories("${OPENSSL_INC}")
endif()
    # 抑制OpenSSL弃用API警告
    add_definitions(-DOSSL_SUPPRESS_DEPRECATED=1)
