add_library(model_shield SHARED
    model_shield.cpp
    chunk_decryptor.cpp
    vfs_layer.cpp
    per_layer_weight_crypto.cpp
    inference_entry_guard.cpp
    output_perturbation.cpp
)
target_link_libraries(model_shield log android)
