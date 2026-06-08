add_library(anti_debug SHARED
    anti_debug.cpp
    tracerpid_checker.cpp
    frida_detector.cpp
    xposed_detector.cpp
    plt_checker.cpp
    timing_detector.cpp
    signal_handler.cpp
)
# OpenSSL headers and libraries
set(OPENSSL_INC "${CMAKE_CURRENT_SOURCE_DIR}/../../../../../../../app/src/main/cpp/openssl/include")
set(OPENSSL_LIB "${CMAKE_CURRENT_SOURCE_DIR}/../../../../../../../app/src/main/cpp/openssl/${ANDROID_ABI}")

if(EXISTS "${OPENSSL_INC}/openssl/ssl.h")
    include_directories("${OPENSSL_INC}")
    if(EXISTS "${OPENSSL_LIB}/libcrypto.a")
        target_link_libraries(anti_debug log android "${OPENSSL_LIB}/libcrypto.a" "${OPENSSL_LIB}/libssl.a")
    else()
        target_link_libraries(anti_debug log android)
    endif()
else()
    target_link_libraries(anti_debug log android)
endif()
add_definitions(-DOSSL_SUPPRESS_DEPRECATED=1)
