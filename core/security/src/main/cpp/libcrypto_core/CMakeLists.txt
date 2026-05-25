add_library(crypto_core SHARED
    crypto_core.cpp
    aes_gcm.cpp
    key_derivation.cpp
    secure_random.cpp
)
target_link_libraries(crypto_core log android)
