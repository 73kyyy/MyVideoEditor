cmake_minimum_required(VERSION 3.22)
project(core_vision)

add_library(core_vision SHARED
    opencv_bridge.cpp
    plane_tracker_native.cpp
)

target_link_libraries(core_vision log android)
