cmake_minimum_required(VERSION 3.22.1)
project("nexclip_security" C)
set(CMAKE_C_STANDARD 11)
set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -O2 -fvisibility=hidden -fstack-protector-strong -fPIE -pie")
set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -Wall -Werror -Wformat-security")
set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -D_FORTIFY_SOURCE=2")
set(CMAKE_C_FLAGS_RELEASE "${CMAKE_C_FLAGS_RELEASE} -s -fomit-frame-pointer")

set(OPENSSL_DIR ${CMAKE_SOURCE_DIR}/openssl)
set(OPENSSL_INCLUDE ${OPENSSL_DIR}/include)
set(OPENSSL_LIB ${OPENSSL_DIR}/${ANDROID_ABI}/libcrypto.a)

include_directories(${CMAKE_SOURCE_DIR})
include_directories(${OPENSSL_INCLUDE})
include_directories(${CMAKE_SOURCE_DIR}/../../security)

find_library(log-lib log)
find_library(android-lib android)

# 无OpenSSL的模块（仅需log）
add_library(native_anti_debug SHARED ${CMAKE_SOURCE_DIR}/../../security/native_anti_debug.c)
target_link_libraries(native_anti_debug ${log-lib})

add_library(native_verify SHARED ${CMAKE_SOURCE_DIR}/../../security/native_verify.c)
target_link_libraries(native_verify ${log-lib})

add_library(native_hook_detect SHARED ${CMAKE_SOURCE_DIR}/../../security/native_hook_detect.c)
target_link_libraries(native_hook_detect ${log-lib})

add_library(native_inject_detect SHARED ${CMAKE_SOURCE_DIR}/../../security/native_inject_detect.c)
target_link_libraries(native_inject_detect ${log-lib})

add_library(native_root_detect SHARED ${CMAKE_SOURCE_DIR}/../../security/native_root_detect.c)
target_link_libraries(native_root_detect ${log-lib})

add_library(native_memory_protect SHARED ${CMAKE_SOURCE_DIR}/../../security/native_memory_protect.c)
target_link_libraries(native_memory_protect ${log-lib})

add_library(native_ui_protect SHARED ${CMAKE_SOURCE_DIR}/../../security/native_ui_protect.c)
target_link_libraries(native_ui_protect ${log-lib})

add_library(native_device_fingerprint SHARED ${CMAKE_SOURCE_DIR}/../../security/native_device_fingerprint.c)
target_link_libraries(native_device_fingerprint ${log-lib})

# 需要OpenSSL的模块
add_library(native_comm_secure SHARED ${CMAKE_SOURCE_DIR}/../../security/native_comm_secure.c)
target_link_libraries(native_comm_secure ${log-lib} ${OPENSSL_LIB})

add_library(native_data_protect SHARED ${CMAKE_SOURCE_DIR}/../../security/native_data_protect.c)
target_link_libraries(native_data_protect ${log-lib} ${OPENSSL_LIB})

add_library(native_continuous_monitor SHARED ${CMAKE_SOURCE_DIR}/../../security/native_continuous_monitor.c)
target_link_libraries(native_continuous_monitor ${log-lib} ${OPENSSL_LIB})

add_library(native_compliance SHARED ${CMAKE_SOURCE_DIR}/../../security/native_compliance.c)
target_link_libraries(native_compliance ${log-lib} ${OPENSSL_LIB})

add_library(native_self_build SHARED ${CMAKE_SOURCE_DIR}/../../security/native_self_build.c)
target_link_libraries(native_self_build ${log-lib} ${OPENSSL_LIB} ${android-lib} dl)
