add_library(integrity SHARED
    integrity.cpp
    signature_verifier.cpp
    dex_integrity.cpp
)
target_link_libraries(integrity log android)
