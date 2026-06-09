# AI module ProGuard rules
# 激进混淆保护AI推理逻辑和模型加载

-optimizationpasses 5
-allowaccessmodification
-overloadaggressively
-mergeinterfacesaggressively
-repackageclasses ''

# Keep JNI methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep ONNX Runtime API (reflection-based)
-keep class ai.onnxruntime.** { *; }
-dontwarn ai.onnxruntime.**

# Keep AI bridge API (called from app module)
-keep class com.myvideo.editor.core.ai.AIIntegrationBridge { public *; }
-keep class com.myvideo.editor.core.ai.AIIntegrationBridge$AIResult { *; }
-keep class com.myvideo.editor.core.ai.AIIntegrationBridge$AIFeature { *; }
-keep class com.myvideo.editor.core.ai.ModelRegistry { public *; }
-keep class com.myvideo.editor.core.ai.DeviceTierDetector { public *; }

# Keep SecureModelLoader JNI bridge
-keep class com.myvideo.editor.core.ai.common.SecureModelLoader { public *; }

# Obfuscate wrapper implementations
-dontwarn com.microsoft.onnxruntime.**

# Remove debug logs in release
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
    public static int i(...);
    public static int w(...);
    public static int e(...);
}
