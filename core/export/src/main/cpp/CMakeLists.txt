cmake_minimum_required(VERSION 3.22)
project(core_export)

add_library(render_engine SHARED
    render_engine.cpp
)

# ===== 零风险编译加固 =====
target_compile_options(render_engine PRIVATE
    -Wall -Wextra -Wformat=2 -Werror=format-security
    -fvisibility=hidden -fvisibility-inlines-hidden
    -ffunction-sections -fdata-sections
    -flto
    $<$<CONFIG:Release>:-O3>
)
if(ANDROID_ABI STREQUAL "arm64-v8a")
    target_compile_options(render_engine PRIVATE -mbranch-protection=standard)
endif()
target_link_options(render_engine PRIVATE
    -Wl,--gc-sections -Wl,--strip-all -Wl,--exclude-libs,ALL
    -Wl,--version-script,${CMAKE_CURRENT_SOURCE_DIR}/export_symbols.map
    -flto $<$<CONFIG:Release>:-O3>
)
set_target_properties(render_engine PROPERTIES LINK_DEPENDS ${CMAKE_CURRENT_SOURCE_DIR}/export_symbols.map)

target_link_libraries(render_engine log android GLESv2)
