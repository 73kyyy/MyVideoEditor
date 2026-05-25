add_library(env_check SHARED
    env_check.cpp
    root_detector.cpp
    emulator_detector.cpp
)
target_link_libraries(env_check log android)
