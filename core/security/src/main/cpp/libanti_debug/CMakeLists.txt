add_library(anti_debug SHARED
    anti_debug.cpp
    tracerpid_checker.cpp
    frida_detector.cpp
    xposed_detector.cpp
    plt_checker.cpp
    timing_detector.cpp
    signal_handler.cpp
)
target_link_libraries(anti_debug log android)

# OpenSSL headers
set(OPENSSL_INC "${CMAKE_CURRENT_SOURCE_DIR}/../../../../../../../app/src/main/cpp/openssl/include")
if(EXISTS "${OPENSSL_INC}/openssl/ssl.h")
    include_directories("${OPENSSL_INC}")
endif()
    # 抑制OpenSSL弃用API警告
    add_definitions(-DOSSL_SUPPRESS_DEPRECATED=1)
