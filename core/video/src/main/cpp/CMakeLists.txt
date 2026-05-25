cmake_minimum_required(VERSION 3.22)
project(ffmpeg_bridge)

find_package(PkgConfig REQUIRED)
pkg_check_modules(FFMPEG REQUIRED libavcodec libavformat libavutil libswscale)

add_library(ffmpeg_bridge SHARED ffmpeg_bridge.cpp)

target_include_directories(ffmpeg_bridge PRIVATE ${FFMPEG_INCLUDE_DIRS})
target_link_libraries(ffmpeg_bridge ${FFMPEG_LIBRARIES} log android)
