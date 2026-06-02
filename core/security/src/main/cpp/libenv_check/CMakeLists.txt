add_library(env_check SHARED
    env_check.cpp
    root_detector.cpp
    emulator_detector.cpp
)
target_link_libraries(env_check log android)

# OpenSSL headers
set(OPENSSL_INC "${CMAKE_CURRENT_SOURCE_DIR}/../../../../../../../app/src/main/cpp/openssl/include")
if(EXISTS "${OPENSSL_INC}/openssl/ssl.h")
    include_directories("${OPENSSL_INC}")
endif()
