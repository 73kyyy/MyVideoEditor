cmake_minimum_required(VERSION 3.22)
project(core_security)

add_subdirectory(libanti_debug)
add_subdirectory(libanti_sniff)
add_subdirectory(libenv_check)
add_subdirectory(libintegrity)
add_subdirectory(libcrypto_core)
add_subdirectory(libdex_guard)
add_subdirectory(libmodel_shield)
add_subdirectory(libself_verify)
add_subdirectory(libnet_secure)

# OpenSSL headers
set(OPENSSL_INC "${CMAKE_CURRENT_SOURCE_DIR}/../../../../../../app/src/main/cpp/openssl/include")
if(EXISTS "${OPENSSL_INC}/openssl/ssl.h")
    include_directories("${OPENSSL_INC}")
endif()
