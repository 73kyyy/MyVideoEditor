#pragma once

#include <onnxruntime_cxx_api.h>
#include <string>
#include <vector>
#include <memory>
#include <android/log.h>

#define ORT_TAG "ORTEngine"
#define ORT_LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, ORT_TAG, __VA_ARGS__)
#define ORT_LOGE(...) __android_log_print(ANDROID_LOG_ERROR, ORT_TAG, __VA_ARGS__)
#define ORT_LOGI(...) __android_log_print(ANDROID_LOG_INFO, ORT_TAG, __VA_ARGS__)

/**
 * ORTEngine - Singleton providing shared ONNX Runtime environment and utilities.
 *
 * The Ort::Env instance is created once and shared across all sessions.
 * Thread-safe: Ort::Env and Ort::Session support concurrent usage.
 */
class ORTEngine {
public:
    static Ort::Env& getEnv() {
        static Ort::Env env{ORT_LOGGING_LEVEL_WARNING, "core_ai"};
        return env;
    }

    static Ort::MemoryInfo getMemoryInfo() {
        return Ort::MemoryInfo::CreateWithArenaAllocator(OrtMemTypeDefault);
    }

    static Ort::Session* createSession(const std::string& modelPath) {
        try {
            Ort::SessionOptions options;
            options.SetIntraOpNumThreads(4);
            options.SetGraphOptimizationLevel(GraphOptimizationLevel::ORT_ENABLE_ALL);
            auto* session = new Ort::Session(getEnv(), modelPath.c_str(), options);
            ORT_LOGI("Session created for model: %s", modelPath.c_str());
            return session;
        } catch (const Ort::Exception& e) {
            ORT_LOGE("Failed to create session for %s: %s", modelPath.c_str(), e.what());
            return nullptr;
        }
    }

private:
    ORTEngine() = delete;
    ~ORTEngine() = delete;
    ORTEngine(const ORTEngine&) = delete;
    ORTEngine& operator=(const ORTEngine&) = delete;
};

/**
 * ORTSession - RAII wrapper around Ort::Session with cached input/output names.
 *
 * Usage:
 *   auto* s = ORTSession::create("/path/to/model.onnx");
 *   if (!s) { error handling }
 *   // Use s->session, s->input_names, s->output_names
 *   delete s;  // cleanup
 */
struct ORTSession {
    std::unique_ptr<Ort::Session> session;
    std::vector<std::string> input_names_str;
    std::vector<std::string> output_names_str;
    std::vector<const char*> input_names;
    std::vector<const char*> output_names;

    static ORTSession* create(const std::string& modelPath) {
        auto* s = new ORTSession();
        auto* raw = ORTEngine::createSession(modelPath);
        if (!raw) {
            delete s;
            return nullptr;
        }
        s->session.reset(raw);

        try {
            Ort::AllocatorWithDefaultOptions allocator;

            size_t num_inputs = s->session->GetInputCount();
            s->input_names_str.reserve(num_inputs);
            s->input_names.reserve(num_inputs);
            for (size_t i = 0; i < num_inputs; i++) {
                auto name = s->session->GetInputNameAllocated(i, allocator);
                s->input_names_str.emplace_back(name.get());
            }
            for (auto& str : s->input_names_str) {
                s->input_names.push_back(str.c_str());
            }

            size_t num_outputs = s->session->GetOutputCount();
            s->output_names_str.reserve(num_outputs);
            s->output_names.reserve(num_outputs);
            for (size_t i = 0; i < num_outputs; i++) {
                auto name = s->session->GetOutputNameAllocated(i, allocator);
                s->output_names_str.emplace_back(name.get());
            }
            for (auto& str : s->output_names_str) {
                s->output_names.push_back(str.c_str());
            }

            ORT_LOGI("ORTSession created: %zu inputs, %zu outputs",
                     num_inputs, num_outputs);
            return s;
        } catch (const Ort::Exception& e) {
            ORT_LOGE("Failed to query session info: %s", e.what());
            delete s;
            return nullptr;
        }
    }

    // Run inference with the given input tensors, return output tensors.
    std::vector<Ort::Value> run(const std::vector<Ort::Value>& input_tensors) {
        return session->Run(
            Ort::RunOptions{nullptr},
            input_names.data(), input_tensors.data(), input_tensors.size(),
            output_names.data(), output_names.size()
        );
    }
};
