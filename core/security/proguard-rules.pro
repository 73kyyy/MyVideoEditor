# Security module ProGuard rules
# 激进混淆保护安全逻辑

-optimizationpasses 5
-allowaccessmodification
-overloadaggressively
-mergeinterfacesaggressively
-repackageclasses ''

# Keep JNI methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep membership API (called from other modules)
-keep class com.myvideo.editor.core.security.membership.MembershipValidator { *; }
-keep class com.myvideo.editor.core.security.membership.FeatureGate { *; }
-keep class com.myvideo.editor.core.security.membership.TokenManager { public *; }

# Obfuscate internal implementation
-dontwarn javax.crypto.**
-dontwarn java.security.**

# Remove debug logs in release
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
    public static int i(...);
    public static int w(...);
    public static int e(...);
}
