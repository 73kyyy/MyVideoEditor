#include <jni.h>
#include <android/log.h>
#include <vector>
#include <string>
#include <algorithm>

#define TAG "SAM2Inference"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

#if USE_ONNX_RUNTIME

#include "onnxruntime_helper.h"

struct SAM2Context {
    std::unique_ptr<ORTSession> encoder;
    std::unique_ptr<ORTSession> decoder;
};

extern "C" JNIEXPORT jlong JNICALL
Java_com_myvideo_editor_core_ai_segmentation_SAM2Wrapper_nativeInit(
        JNIEnv *env, jobject /*thiz*/, jstring modelPath) {
    if (!modelPath) {
        LOGE("modelPath is null");
        return 0;
    }

    const char* path = env->GetStringUTFChars(modelPath, nullptr);
    std::string basePath(path);
    env->ReleaseStringUTFChars(modelPath, path);

    // Derive encoder and decoder paths from the same directory
    std::string dir = basePath.substr(0, basePath.find_last_of('/'));
    std::string encoderPath = dir + "/sam_encoder.onnx";
    std::string decoderPath = dir + "/sam_decoder.onnx";

    auto* ctx = new SAM2Context();

    ctx->encoder.reset(ORTSession::create(encoderPath));
    if (!ctx->encoder) {
        LOGE("Failed to create SAM encoder session");
        delete ctx;
        return 0;
    }

    ctx->decoder.reset(ORTSession::create(decoderPath));
    if (!ctx->decoder) {
        LOGE("Failed to create SAM decoder session");
        delete ctx;
        return 0;
    }

    LOGD("SAM2 initialized (encoder + decoder)");
    return reinterpret_cast<jlong>(ctx);
}

extern "C" JNIEXPORT jfloatArray JNICALL
Java_com_myvideo_editor_core_ai_segmentation_SAM2Wrapper_nativeSegment(
        JNIEnv *env, jobject /*thiz*/,
        jlong handle, jfloatArray input, jint width, jint height,
        jfloat pointX, jfloat pointY) {
    auto* ctx = reinterpret_cast<SAM2Context*>(handle);
    if (!ctx || !ctx->encoder || !ctx->decoder) {
        LOGE("Invalid SAM2 handle");
        return nullptr;
    }

    jfloat* inputData = env->GetFloatArrayElements(input, nullptr);
    if (!inputData) {
        LOGE("Failed to get input data");
        return nullptr;
    }

    try {
        auto memory_info = ORTEngine::getMemoryInfo();

        // ── 1. Run encoder ────────────────────────────────────────────────
        // Encoder input: image [1, 3, 1024, 1024]
        // The input image should be pre-processed (resized to 1024x1024, normalized)
        // on the Java side. We receive it as CHW float array.
        int inputSize = env->GetArrayLength(input);
        std::vector<int64_t> enc_input_shape = {1, 3, static_cast<int64_t>(height), static_cast<int64_t>(width)};

        std::vector<Ort::Value> enc_inputs;
        enc_inputs.push_back(Ort::Value::CreateTensor<float>(
                memory_info, inputData, inputSize,
                enc_input_shape.data(), enc_input_shape.size()));

        auto enc_outputs = ctx->encoder->run(enc_inputs);
        env->ReleaseFloatArrayElements(input, inputData, JNI_ABORT);

        if (enc_outputs.empty()) {
            LOGE("SAM encoder returned no outputs");
            return nullptr;
        }

        // Clone encoder output
        auto& enc_out = enc_outputs[0];
        auto enc_shape = enc_out.GetTensorTypeAndShapeInfo().GetShape();
        size_t enc_size = 1;
        for (auto d : enc_shape) enc_size *= d;
        const float* enc_data = enc_out.GetTensorData<float>();
        std::vector<float> embeddings(enc_data, enc_data + enc_size);

        // ── 2. Run decoder ────────────────────────────────────────────────
        // Decoder inputs:
        //   image_embeddings: [1, 256, 64, 64]
        //   point_coords: [1, 1, 2]  (normalized to [0, 1] range relative to 1024)
        //   point_labels: [1, 1]     (1 = foreground point)

        // Normalize point coords to 1024x1024 space
        float normX = pointX * 1024.0f / width;
        float normY = pointY * 1024.0f / height;
        float point_coords_data[] = {normX, normY};
        float point_labels_data[] = {1.0f};

        std::vector<int64_t> emb_shape = enc_shape;
        std::vector<int64_t> coord_shape = {1, 1, 2};
        std::vector<int64_t> label_shape = {1, 1};

        std::vector<Ort::Value> dec_inputs;
        dec_inputs.reserve(3);

        dec_inputs.push_back(Ort::Value::CreateTensor<float>(
                memory_info, embeddings.data(), embeddings.size(),
                emb_shape.data(), emb_shape.size()));
        dec_inputs.push_back(Ort::Value::CreateTensor<float>(
                memory_info, point_coords_data, 2,
                coord_shape.data(), coord_shape.size()));
        dec_inputs.push_back(Ort::Value::CreateTensor<float>(
                memory_info, point_labels_data, 1,
                label_shape.data(), label_shape.size()));

        // If decoder expects mask_input and has_mask_input, add zero tensors
        if (ctx->decoder->input_names.size() > 3) {
            // mask_input: [1, 1, 256, 256] zeros
            std::vector<float> mask_input(256 * 256, 0.0f);
            std::vector<int64_t> mask_shape = {1, 1, 256, 256};
            dec_inputs.push_back(Ort::Value::CreateTensor<float>(
                    memory_info, mask_input.data(), mask_input.size(),
                    mask_shape.data(), mask_shape.size()));

            // has_mask_input: [1] = 0
            if (ctx->decoder->input_names.size() > 4) {
                float has_mask = 0.0f;
                std::vector<int64_t> has_mask_shape = {1};
                dec_inputs.push_back(Ort::Value::CreateTensor<float>(
                        memory_info, &has_mask, 1,
                        has_mask_shape.data(), has_mask_shape.size()));
            }
        }

        auto dec_outputs = ctx->decoder->run(dec_inputs);

        if (dec_outputs.empty()) {
            LOGE("SAM decoder returned no outputs");
            return nullptr;
        }

        // ── 3. Process mask output ────────────────────────────────────────
        auto& mask_out = dec_outputs[0];
        auto mask_shape = mask_out.GetTensorTypeAndShapeInfo().GetShape();
        const float* mask_data = mask_out.GetTensorData<float>();

        // If multi-mask output [1, 4, 256, 256], select the best mask (highest IoU)
        int mask_idx = 0;
        if (dec_outputs.size() >= 2 && mask_shape.size() == 4 && mask_shape[1] > 1) {
            auto& iou_out = dec_outputs[1];
            const float* iou_data = iou_out.GetTensorData<float>();
            auto iou_shape = iou_out.GetTensorTypeAndShapeInfo().GetShape();
            int num_masks = static_cast<int>(mask_shape[1]);
            float best_iou = -1.0f;
            for (int i = 0; i < num_masks && i < static_cast<int>(iou_shape.back()); i++) {
                if (iou_data[i] > best_iou) {
                    best_iou = iou_data[i];
                    mask_idx = i;
                }
            }
        }

        // Compute mask dimensions
        int mask_h = 256, mask_w = 256;
        if (mask_shape.size() >= 2) {
            mask_h = static_cast<int>(mask_shape[mask_shape.size() - 2]);
            mask_w = static_cast<int>(mask_shape[mask_shape.size() - 1]);
        }

        // Resize mask to original image size (simple nearest-neighbor)
        int mask_size = width * height;
        std::vector<float> result_mask(mask_size, 0.0f);

        int single_mask_size = mask_h * mask_w;
        const float* selected_mask = mask_data + mask_idx * single_mask_size;

        for (int y = 0; y < height; y++) {
            int src_y = std::min(y * mask_h / height, mask_h - 1);
            for (int x = 0; x < width; x++) {
                int src_x = std::min(x * mask_w / width, mask_w - 1);
                result_mask[y * width + x] = selected_mask[src_y * mask_w + src_x];
            }
        }

        // Create result
        jfloatArray result = env->NewFloatArray(mask_size);
        if (!result) {
            LOGE("Failed to allocate mask array");
            return nullptr;
        }
        env->SetFloatArrayRegion(result, 0, mask_size, result_mask.data());
        return result;

    } catch (const Ort::Exception& e) {
        LOGE("SAM2 inference error: %s", e.what());
        env->ReleaseFloatArrayElements(input, inputData, JNI_ABORT);
        return nullptr;
    } catch (const std::exception& e) {
        LOGE("SAM2 error: %s", e.what());
        env->ReleaseFloatArrayElements(input, inputData, JNI_ABORT);
        return nullptr;
    }
}

extern "C" JNIEXPORT void JNICALL
Java_com_myvideo_editor_core_ai_segmentation_SAM2Wrapper_nativeRelease(
        JNIEnv *env, jobject /*thiz*/, jlong handle) {
    auto* ctx = reinterpret_cast<SAM2Context*>(handle);
    if (ctx) {
        ctx->encoder.reset();
        ctx->decoder.reset();
        delete ctx;
        LOGD("SAM2 released");
    }
}

#else // !USE_ONNX_RUNTIME

extern "C" JNIEXPORT jlong JNICALL
Java_com_myvideo_editor_core_ai_segmentation_SAM2Wrapper_nativeInit(
        JNIEnv *env, jobject /*thiz*/, jstring modelPath) {
    LOGE("ONNX Runtime not available - SAM2 nativeInit stub");
    return 0;
}

extern "C" JNIEXPORT jfloatArray JNICALL
Java_com_myvideo_editor_core_ai_segmentation_SAM2Wrapper_nativeSegment(
        JNIEnv *env, jobject /*thiz*/,
        jlong handle, jfloatArray input, jint width, jint height,
        jfloat pointX, jfloat pointY) {
    LOGE("ONNX Runtime not available - SAM2 nativeSegment stub");
    return nullptr;
}

extern "C" JNIEXPORT void JNICALL
Java_com_myvideo_editor_core_ai_segmentation_SAM2Wrapper_nativeRelease(
        JNIEnv *env, jobject /*thiz*/, jlong handle) {
    LOGE("ONNX Runtime not available - SAM2 nativeRelease stub");
}

#endif // USE_ONNX_RUNTIME
