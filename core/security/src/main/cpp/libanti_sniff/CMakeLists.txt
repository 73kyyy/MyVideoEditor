add_library(anti_sniff SHARED
    anti_sniff.cpp
    port_scanner.cpp
    vpn_detector.cpp
    package_scanner.cpp
    proxy_detector.cpp
    cert_chain_checker.cpp
    crawler_detector.cpp
)
# OpenSSL headers and libraries
set(OPENSSL_INC "${CMAKE_CURRENT_SOURCE_DIR}/../../../../../../../app/src/main/cpp/openssl/include")
set(OPENSSL_LIB "${CMAKE_CURRENT_SOURCE_DIR}/../../../../../../../app/src/main/cpp/openssl/${ANDROID_ABI}")

if(EXISTS "${OPENSSL_INC}/openssl/ssl.h")
    include_directories("${OPENSSL_INC}")
    if(EXISTS "${OPENSSL_LIB}/libcrypto.a")
        target_link_libraries(anti_sniff log android "${OPENSSL_LIB}/libcrypto.a" "${OPENSSL_LIB}/libssl.a")
    else()
        target_link_libraries(anti_sniff log android)
    endif()
else()
    target_link_libraries(anti_sniff log android)
endif()
add_definitions(-DOSSL_SUPPRESS_DEPRECATED=1)
