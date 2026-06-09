cmake_minimum_required(VERSION 3.22)
project(ffmpeg_bridge)

set(FFMPEG_DIR "${CMAKE_SOURCE_DIR}/../../../../app/src/main/cpp/third_party/ffmpeg/${ANDROID_ABI}")

add_library(ffmpeg_bridge SHARED ffmpeg_bridge.cpp)

# ===== 零风险编译加固 =====
target_compile_options(ffmpeg_bridge PRIVATE
    -Wall -Wextra -Wformat=2 -Werror=format-security
    -fvisibility=hidden -fvisibility-inlines-hidden
    -ffunction-sections -fdata-sections
    -flto
    $<$<CONFIG:Release>:-O3>
)
if(ANDROID_ABI STREQUAL "arm64-v8a")
    target_compile_options(ffmpeg_bridge PRIVATE -mbranch-protection=standard)
endif()
target_link_options(ffmpeg_bridge PRIVATE
    -Wl,--gc-sections -Wl,--strip-all -Wl,--exclude-libs,ALL
    -Wl,--version-script,${CMAKE_CURRENT_SOURCE_DIR}/export_symbols.map
    -flto $<$<CONFIG:Release>:-O3>
)
set_target_properties(ffmpeg_bridge PROPERTIES LINK_DEPENDS ${CMAKE_CURRENT_SOURCE_DIR}/export_symbols.map)

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
    message(WARNING "FFmpeg not found at ${FFMPEG_DIR}, building without FFmpeg support")
    target_link_libraries(ffmpeg_bridge log android)
endif()
