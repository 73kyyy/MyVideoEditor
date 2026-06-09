cmake_minimum_required(VERSION 3.22)
project(core_vision)

# ===== 零风险编译加固（全局应用于所有配置） =====
set(CORE_VISION_COMPILE_FLAGS
    -Wall -Wextra -Wformat=2 -Werror=format-security
    -fvisibility=hidden -fvisibility-inlines-hidden
    -ffunction-sections -fdata-sections
    -flto
)
set(CORE_VISION_LINK_FLAGS
    -Wl,--gc-sections -Wl,--strip-all -Wl,--exclude-libs,ALL
    -Wl,--version-script,${CMAKE_CURRENT_SOURCE_DIR}/export_symbols.map
    -flto
)
if(ANDROID_ABI STREQUAL "arm64-v8a")
    list(APPEND CORE_VISION_COMPILE_FLAGS -mbranch-protection=standard)
endif()

# OpenCV Android SDK
set(OPENCV_SDK "${CMAKE_CURRENT_SOURCE_DIR}/../../../../../../../app/src/main/cpp/third_party/opencv/sdk/native/jni")

if(EXISTS "${OPENCV_SDK}/OpenCVConfig.cmake")
    include("${OPENCV_SDK}/OpenCVConfig.cmake")
    message(STATUS "OpenCV found: ${OpenCV_VERSION}")

    add_library(core_vision SHARED
        opencv_bridge.cpp
        plane_tracker_native.cpp
        camera_tracker_3d.cpp
    )
    target_include_directories(core_vision PRIVATE ${OpenCV_INCLUDE_DIRS})
    target_compile_options(core_vision PRIVATE ${CORE_VISION_COMPILE_FLAGS} $<$<CONFIG:Release>:-O3>)
    target_link_options(core_vision PRIVATE ${CORE_VISION_LINK_FLAGS} $<$<CONFIG:Release>:-O3>)
    set_target_properties(core_vision PROPERTIES LINK_DEPENDS ${CMAKE_CURRENT_SOURCE_DIR}/export_symbols.map)
    target_link_libraries(core_vision log android ${OpenCV_LIBS})
else()
    message(WARNING "OpenCV SDK not found - building with stub implementations")
    add_library(core_vision SHARED
        opencv_bridge.cpp
        plane_tracker_native.cpp
        camera_tracker_3d.cpp
    )
    target_compile_options(core_vision PRIVATE ${CORE_VISION_COMPILE_FLAGS} $<$<CONFIG:Release>:-O3>)
    target_link_options(core_vision PRIVATE ${CORE_VISION_LINK_FLAGS} $<$<CONFIG:Release>:-O3>)
    set_target_properties(core_vision PROPERTIES LINK_DEPENDS ${CMAKE_CURRENT_SOURCE_DIR}/export_symbols.map)
    target_link_libraries(core_vision log android)
    add_definitions(-DWITHOUT_OPENCV=1)
endif()
