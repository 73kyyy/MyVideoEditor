add_library(net_secure SHARED
    net_secure.cpp
    conscrypt_init.cpp
    cert_pinner.cpp
    request_signer.cpp
    response_decryptor.cpp
)
target_link_libraries(net_secure log android)
