cmake_minimum_required(VERSION 3.22)
project(ffmpeg_bridge)

set(FFMPEG_DIR "${CMAKE_SOURCE_DIR}/../../../../app/src/main/cpp/third_party/ffmpeg/${ANDROID_ABI}")

add_library(ffmpeg_bridge SHARED ffmpeg_bridge.cpp)

if(EXISTS "${FFMPEG_DIR}/lib/libavcodec.a")
    include_directories("${FFMPEG_DIR}/include")
    target_link_libraries(ffmpeg_bridge
        ${FFMPEG_DIR}/lib/libavformat.a
        ${FFMPEG_DIR}/lib/libavcodec.a
        ${FFMPEG_DIR}/lib/libswscale.a
        ${FFMPEG_DIR}/lib/libavutil.a
        z m log android
    )
    target_compile_definitions(ffmpeg_bridge PRIVATE HAS_FFMPEG=1)
else()
    message(WARNING "FFmpeg not found, building without FFmpeg support")
    target_link_libraries(ffmpeg_bridge log android)
endif()
