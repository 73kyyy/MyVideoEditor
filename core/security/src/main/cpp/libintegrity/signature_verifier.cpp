#include <jni.h>
#include <android/log.h>

#define TAG "SignatureVerifier"

extern "C" JNIEXPORT jboolean JNICALL
Java_com_myvideo_editor_core_security_SecurityChecker_nativeVerifySignature(JNIEnv *env, jobject thiz) {
    jclass cls = env->FindClass("com/myvideo/editor/core/security/SecurityChecker");
    jmethodID mid = env->GetMethodID(cls, "getContext", "()Landroid/content/Context;");
    jobject ctx = env->CallObjectMethod(thiz, mid);
    if (!ctx) return JNI_FALSE;
    jclass ctxCls = env->GetObjectClass(ctx);
    jmethodID getPm = env->GetMethodID(ctxCls, "getPackageManager", "()Landroid/content/pm/PackageManager;");
    jmethodID getPn = env->GetMethodID(ctxCls, "getPackageName", "()Ljava/lang/String;");
    jobject pm = env->CallObjectMethod(ctx, getPm);
    jstring pn = (jstring)env->CallObjectMethod(ctx, getPn);
    if (!pm || !pn) return JNI_FALSE;
    jclass pmCls = env->GetObjectClass(pm);
    jmethodID getPi = env->GetMethodID(pmCls, "getPackageInfo", "(Ljava/lang/String;I)Landroid/content/pm/PackageInfo;");
    jobject pi = env->CallObjectMethod(pm, getPi, pn, 64);
    if (!pi) return JNI_TRUE;
    return JNI_FALSE;
}
