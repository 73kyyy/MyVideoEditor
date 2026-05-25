add_library(dex_guard SHARED
    dex_guard.cpp
    dex_decryptor.cpp
    function_level_decrypt.cpp
    dex_fragment_loader.cpp
    class_name_obfuscator.cpp
    anti_dump.cpp
)
target_link_libraries(dex_guard log android)
