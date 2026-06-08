cmake_minimum_required(VERSION 3.22)
project(core_vision)

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
    target_link_libraries(core_vision log android ${OpenCV_LIBS})
else()
    message(WARNING "OpenCV SDK not found - building with stub implementations")
    add_library(core_vision SHARED
        opencv_bridge.cpp
        plane_tracker_native.cpp
        camera_tracker_3d.cpp
    )
    target_link_libraries(core_vision log android)
    add_definitions(-DWITHOUT_OPENCV=1)
endif()
