add_library(anti_debug SHARED
    anti_debug.cpp
    tracerpid_checker.cpp
    frida_detector.cpp
    xposed_detector.cpp
    plt_checker.cpp
    timing_detector.cpp
    signal_handler.cpp
)
target_link_libraries(anti_debug log android)
