cmake_minimum_required(VERSION 3.22)
project(core_export)

add_library(render_engine SHARED
    render_engine.cpp
)

target_link_libraries(render_engine log android GLESv2)
