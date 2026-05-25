add_library(self_verify SHARED
    self_verify.cpp
    crc_checker.cpp
    text_segment_hash.cpp
    cross_so_verifier.cpp
)
target_link_libraries(self_verify log android)
