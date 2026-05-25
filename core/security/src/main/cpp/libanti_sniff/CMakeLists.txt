add_library(anti_sniff SHARED
    anti_sniff.cpp
    port_scanner.cpp
    vpn_detector.cpp
    package_scanner.cpp
    proxy_detector.cpp
    cert_chain_checker.cpp
    crawler_detector.cpp
)
target_link_libraries(anti_sniff log android)
