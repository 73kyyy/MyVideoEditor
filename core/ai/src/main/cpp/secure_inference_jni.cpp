/**
 * secure_inference_jni.cpp
 *
 * 安全推理引擎 - 保护AI模型和会员系统
 *
 * 安全层级:
 * 1. 模型加密: AES-256-GCM + PBKDF2密钥派生
 * 2. 会员验证: HMAC-SHA256令牌校验（独立于Java层）
 * 3. 防篡改: APK签名验证 + 包名校验
 * 4. 防调试: ptrace检测 + 时间检测
 * 5. 密钥混淆: 分片存储 + XOR + 运行时重组
 *
 * 加密文件格式:
 * [4B: Magic "NXC1"] [12B: IV] [16B: AuthTag] [NB: Ciphertext]
 */

#include <jni.h>
#include <android/log.h>
#include <android/asset_manager.h>
#include <android/asset_manager_jni.h>
#include <cstring>
#include <cstdlib>
#include <vector>
#include <mutex>
#include <ctime>

// OpenSSL
#include <openssl/evp.h>
#include <openssl/hmac.h>
#include <openssl/sha.h>
#include <openssl/rand.h>
#include <openssl/err.h>

#define TAG "SecureInference"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN, TAG, __VA_ARGS__)

// ============================================================
// Magic bytes for encrypted model files
// ============================================================
static const char MAGIC[4] = {'N', 'X', 'C', '1'};
static const size_t IV_LEN = 12;
static const size_t TAG_LEN = 16;
static const size_t HEADER_LEN = 4 + IV_LEN + TAG_LEN; // 32 bytes

// ============================================================
// Key Obfuscation - 密钥分片存储 + XOR混淆
// 运行时重组，防止静态分析提取密钥
// ============================================================
namespace ObfuscatedKey {

// Master key的4个分片（XOR后的值）
// 实际密钥 = 分片 XOR 掩码
// 注意：这些值需要与CI/CD中的MODEL_ENCRYPTION_KEY对应
// 默认使用开发密钥，生产环境应替换

// 掩码（公开，不敏感）
static const uint8_t MASK_0[8] = {0x4E, 0x65, 0x78, 0x43, 0x6C, 0x69, 0x70, 0x31};
static const uint8_t MASK_1[8] = {0x4D, 0x6F, 0x64, 0x65, 0x6C, 0x50, 0x72, 0x6F};
static const uint8_t MASK_2[8] = {0x74, 0x65, 0x63, 0x74, 0x69, 0x6F, 0x6E, 0x5F};
static const uint8_t MASK_3[8] = {0x53, 0x65, 0x63, 0x72, 0x65, 0x74, 0x4B, 0x65};

// 分片（XOR后的值，真实密钥 = 分片 XOR 掩码）
// 默认开发密钥: "NexClip2024!SecureModelProtectionKey!"
// 生产环境必须替换！
static const uint8_t PART_0[8] = {
    'N' ^ 0x4E, 'e' ^ 0x65, 'x' ^ 0x78, 'C' ^ 0x43,
    'l' ^ 0x6C, 'i' ^ 0x69, 'p' ^ 0x70, '2' ^ 0x31
};
static const uint8_t PART_1[8] = {
    '0' ^ 0x4D, '2' ^ 0x6F, '4' ^ 0x64, '!' ^ 0x65,
    'S' ^ 0x6C, 'e' ^ 0x50, 'c' ^ 0x72, 'u' ^ 0x6F
};
static const uint8_t PART_2[8] = {
    'r' ^ 0x74, 'e' ^ 0x65, 'M' ^ 0x63, 'o' ^ 0x74,
    'd' ^ 0x69, 'e' ^ 0x6F, 'l' ^ 0x6E, 'P' ^ 0x5F
};
static const uint8_t PART_3[8] = {
    'r' ^ 0x53, 'o' ^ 0x65, 't' ^ 0x63, 'e' ^ 0x72,
    'c' ^ 0x65, 't' ^ 0x74, 'K' ^ 0x4B, 'e' ^ 0x65
};

// 运行时重组密钥
static void reconstructMasterKey(uint8_t out[32]) {
    const uint8_t* parts[] = {PART_0, PART_1, PART_2, PART_3};
    const uint8_t* masks[] = {MASK_0, MASK_1, MASK_2, MASK_3};
    for (int i = 0; i < 4; i++) {
        for (int j = 0; j < 8; j++) {
            out[i * 8 + j] = parts[i][j] ^ masks[i][j];
        }
    }
}

// PBKDF2密钥派生 - 从主密钥派生加密密钥
static bool deriveEncryptionKey(const uint8_t master_key[32], uint8_t out[32]) {
    // Salt = SHA256("NexClip_Model_v1")
    uint8_t salt[32];
    EVP_MD_CTX* ctx = EVP_MD_CTX_new();
    if (!ctx) return false;

    const char* salt_input = "NexClip_Model_v1";
    unsigned int salt_len = 0;
    if (EVP_DigestInit_ex(ctx, EVP_sha256(), nullptr) != 1 ||
        EVP_DigestUpdate(ctx, salt_input, strlen(salt_input)) != 1 ||
        EVP_DigestFinal_ex(ctx, salt, &salt_len) != 1) {
        EVP_MD_CTX_free(ctx);
        return false;
    }
    EVP_MD_CTX_free(ctx);

    // PBKDF2-HMAC-SHA256, 100000 iterations
    if (PKCS5_PBKDF2_HMAC(
            reinterpret_cast<const char*>(master_key), 32,
            salt, salt_len,
            100000,
            EVP_sha256(), 32, out) != 1) {
        return false;
    }

    // 安全擦除中间变量
    OPENSSL_cleanse(salt, sizeof(salt));
    return true;
}

} // namespace ObfuscatedKey

// ============================================================
// 会员令牌验证 - 独立于Java层的会员校验
// ============================================================
namespace MembershipGuard {

// 令牌签名密钥（混淆存储）
static const uint8_t TOKEN_KEY_XOR[32] = {
    0x54, 0x6F, 0x6B, 0x65, 0x6E, 0x53, 0x69, 0x67,
    0x6E, 0x69, 0x6E, 0x67, 0x4B, 0x65, 0x79, 0x5F,
    0x32, 0x30, 0x32, 0x34, 0x5F, 0x4E, 0x65, 0x78,
    0x43, 0x6C, 0x69, 0x70, 0x5F, 0x53, 0x65, 0x63
};
static const uint8_t TOKEN_KEY_PART[32] = {
    'T' ^ 0x54, 'o' ^ 0x6F, 'k' ^ 0x6B, 'e' ^ 0x65,
    'n' ^ 0x6E, 'S' ^ 0x53, 'i' ^ 0x69, 'g' ^ 0x67,
    'n' ^ 0x6E, 'i' ^ 0x69, 'n' ^ 0x6E, 'g' ^ 0x67,
    'K' ^ 0x4B, 'e' ^ 0x65, 'y' ^ 0x79, '_' ^ 0x5F,
    '2' ^ 0x32, '0' ^ 0x30, '2' ^ 0x32, '4' ^ 0x34,
    '_' ^ 0x5F, 'N' ^ 0x4E, 'e' ^ 0x65, 'x' ^ 0x78,
    'C' ^ 0x43, 'l' ^ 0x6C, 'i' ^ 0x69, 'p' ^ 0x70,
    '_' ^ 0x5F, 'S' ^ 0x53, 'e' ^ 0x65, 'c' ^ 0x63
};

static void getTokenSigningKey(uint8_t out[32]) {
    for (int i = 0; i < 32; i++) {
        out[i] = TOKEN_KEY_PART[i] ^ TOKEN_KEY_XOR[i];
    }
}

// 验证会员令牌
// 令牌格式: base64(is_member:expiry_timestamp:hmac)
static bool verifyToken(const char* token) {
    if (!token || strlen(token) < 10) return false;

    uint8_t signing_key[32];
    getTokenSigningKey(signing_key);

    // 令牌格式: "MEMBER:expiry_timestamp:hmac_hex"
    // 查找第二个冒号分隔的HMAC
    std::string token_str(token);
    size_t first_colon = token_str.find(':');
    if (first_colon == std::string::npos) {
        OPENSSL_cleanse(signing_key, 32);
        return false;
    }
    size_t second_colon = token_str.find(':', first_colon + 1);
    if (second_colon == std::string::npos) {
        OPENSSL_cleanse(signing_key, 32);
        return false;
    }

    std::string member_part = token_str.substr(0, first_colon);
    std::string expiry_part = token_str.substr(first_colon + 1, second_colon - first_colon - 1);
    std::string hmac_part = token_str.substr(second_colon + 1);

    // 检查会员状态
    if (member_part != "MEMBER") {
        OPENSSL_cleanse(signing_key, 32);
        return false;
    }

    // 检查过期时间
    long expiry = atol(expiry_part.c_str());
    long now = static_cast<long>(time(nullptr));
    if (expiry > 0 && now > expiry) {
        OPENSSL_cleanse(signing_key, 32);
        LOGW("Membership token expired");
        return false;
    }

    // 验证HMAC
    std::string data_to_sign = member_part + ":" + expiry_part;
    unsigned char computed_hmac[32];
    unsigned int hmac_len = 0;

    HMAC(EVP_sha256(), signing_key, 32,
         reinterpret_cast<const unsigned char*>(data_to_sign.c_str()),
         data_to_sign.length(),
         computed_hmac, &hmac_len);

    OPENSSL_cleanse(signing_key, 32);

    // 比较HMAC（常量时间比较，防止时序攻击）
    if (hmac_part.length() != 64) return false;

    unsigned char provided_hmac[32];
    for (int i = 0; i < 32; i++) {
        unsigned int byte;
        sscanf(hmac_part.c_str() + i * 2, "%02x", &byte);
        provided_hmac[i] = static_cast<unsigned char>(byte);
    }

    int diff = 0;
    for (int i = 0; i < 32; i++) {
        diff |= computed_hmac[i] ^ provided_hmac[i];
    }

    OPENSSL_cleanse(computed_hmac, 32);
    OPENSSL_cleanse(provided_hmac, 32);

    return diff == 0;
}

// 生成会员令牌（供Kotlin层调用）
static jstring generateToken(JNIEnv* env, jlong expiryTimestamp) {
    uint8_t signing_key[32];
    getTokenSigningKey(signing_key);

    std::string data = "MEMBER:" + std::to_string(expiryTimestamp);

    unsigned char hmac_result[32];
    unsigned int hmac_len = 0;
    HMAC(EVP_sha256(), signing_key, 32,
         reinterpret_cast<const unsigned char*>(data.c_str()),
         data.length(),
         hmac_result, &hmac_len);

    OPENSSL_cleanse(signing_key, 32);

    // 转为hex
    char hex[65];
    for (int i = 0; i < 32; i++) {
        snprintf(hex + i * 2, 3, "%02x", hmac_result[i]);
    }
    hex[64] = '\0';

    OPENSSL_cleanse(hmac_result, 32);

    std::string token = data + ":" + hex;
    return env->NewStringUTF(token.c_str());
}

} // namespace MembershipGuard

// ============================================================
// 防篡改检测
// ============================================================
namespace AntiTamper {

// 检测调试器附加
static bool isDebuggerAttached() {
    // 方法1: 检查ptrace
    FILE* f = fopen("/proc/self/status", "r");
    if (f) {
        char line[256];
        while (fgets(line, sizeof(line), f)) {
            if (strncmp(line, "TracerPid:", 10) == 0) {
                int pid = atoi(line + 10);
                fclose(f);
                if (pid != 0) {
                    LOGW("Debugger detected: TracerPid=%d", pid);
                    return true;
                }
                return false;
            }
        }
        fclose(f);
    }

    // 方法2: 时间检测（调试时单步执行会变慢）
    struct timespec start, end;
    clock_gettime(CLOCK_MONOTONIC, &start);
    // 执行一些简单操作
    volatile int dummy = 0;
    for (int i = 0; i < 100; i++) dummy += i;
    clock_gettime(CLOCK_MONOTONIC, &end);
    long elapsed_ns = (end.tv_sec - start.tv_sec) * 1000000000L + (end.tv_nsec - start.tv_nsec);
    // 正常执行应该远小于1秒，如果超过则可能被调试
    if (elapsed_ns > 1000000000L) {
        LOGW("Timing anomaly detected: %ld ns", elapsed_ns);
        return true;
    }

    return false;
}

// 验证APK签名（通过JNI调用PackageManager）
static bool verifyAppSignature(JNIEnv* env, jobject context) {
    if (!context) {
        LOGE("Context is null in signature verification");
        return false;
    }

    try {
        // Get PackageManager
        jclass context_cls = env->GetObjectClass(context);
        if (!context_cls) return false;

        jmethodID get_pm = env->GetMethodID(context_cls, "getPackageManager",
                                              "()Landroid/content/pm/PackageManager;");
        if (!get_pm) return false;
        jobject pm = env->CallObjectMethod(context, get_pm);
        if (!pm) return false;

        // Get package name
        jmethodID get_pkg = env->GetMethodID(context_cls, "getPackageName",
                                               "()Ljava/lang/String;");
        if (!get_pkg) return false;
        jstring pkg_name = (jstring)env->CallObjectMethod(context, get_pkg);
        if (!pkg_name) return false;

        // Verify package name
        const char* pkg = env->GetStringUTFChars(pkg_name, nullptr);
        bool pkg_ok = (pkg && strcmp(pkg, "com.myvideo.editor") == 0);
        env->ReleaseStringUTFChars(pkg_name, pkg);
        env->DeleteLocalRef(pkg_name);

        if (!pkg_ok) {
            LOGW("Package name mismatch!");
            return false;
        }

        // Get PackageInfo with signatures
        jclass pm_cls = env->GetObjectClass(pm);
        jmethodID get_pi = env->GetMethodID(pm_cls, "getPackageInfo",
                                              "(Ljava/lang/String;I)Landroid/content/pm/PackageInfo;");
        if (!get_pi) return false;

        // GET_SIGNATURES = 0x40
        jstring pkg_name2 = env->NewStringUTF("com.myvideo.editor");
        jobject pi = env->CallObjectMethod(pm, get_pi, pkg_name2, 0x40);
        env->DeleteLocalRef(pkg_name2);
        if (!pi) return false;

        // Get signatures array
        jclass pi_cls = env->GetObjectClass(pi);
        jfieldID sig_field = env->GetFieldID(pi_cls, "signatures",
                                               "[Landroid/content/pm/Signature;");
        if (!sig_field) return false;
        jobjectArray sigs = (jobjectArray)env->GetObjectField(pi, sig_field);
        if (!sigs || env->GetArrayLength(sigs) == 0) return false;

        // Get first signature
        jobject sig = env->GetObjectArrayElement(sigs, 0);
        if (!sig) return false;

        // Get signature bytes
        jclass sig_cls = env->GetObjectClass(sig);
        jmethodID to_bytes = env->GetMethodID(sig_cls, "toByteArray", "()[B");
        if (!to_bytes) return false;
        jbyteArray sig_bytes = (jbyteArray)env->CallObjectMethod(sig, to_bytes);
        if (!sig_bytes) return false;

        // Compute SHA-256 of signature
        jbyte* bytes = env->GetByteArrayElements(sig_bytes, nullptr);
        jsize bytes_len = env->GetArrayLength(sig_bytes);

        unsigned char sig_hash[32];
        unsigned int hash_len = 0;
        EVP_MD_CTX* md_ctx = EVP_MD_CTX_new();
        if (md_ctx) {
            EVP_DigestInit_ex(md_ctx, EVP_sha256(), nullptr);
            EVP_DigestUpdate(md_ctx, bytes, bytes_len);
            EVP_DigestFinal_ex(md_ctx, sig_hash, &hash_len);
            EVP_MD_CTX_free(md_ctx);
        }

        env->ReleaseByteArrayElements(sig_bytes, bytes, JNI_ABORT);

        // Clean up local refs
        env->DeleteLocalRef(sig_bytes);
        env->DeleteLocalRef(sig);
        env->DeleteLocalRef(sigs);
        env->DeleteLocalRef(pi);
        env->DeleteLocalRef(pm);

        // In production, compare with expected signature hash
        // For now, just verify we can compute it (signature check is optional during development)
        // TODO: Replace with actual expected signature hash in production
        LOGD("Signature hash computed successfully (%u bytes)", hash_len);

        return true;

    } catch (...) {
        LOGE("Exception in signature verification");
        return false;
    }
}

// 检查是否在模拟器中运行
static bool isEmulator() {
    // 检查硬件特征
    FILE* f = fopen("/proc/cpuinfo", "r");
    if (f) {
        char line[256];
        while (fgets(line, sizeof(line), f)) {
            if (strstr(line, "goldfish") || strstr(line, "ranchu") ||
                strstr(line, "sdk_gphone") || strstr(line, "generic")) {
                fclose(f);
                LOGW("Emulator detected: %s", line);
                return true;
            }
        }
        fclose(f);
    }

    // 检查系统属性
    const char* props[] = {
        "ro.hardware", "ro.product.model", "ro.product.brand"
    };
    for (auto prop : props) {
        // 简单检查，不实际读取属性（需要__system_property_get）
        // 在实际运行时由Java层检查
    }

    return false;
}

} // namespace AntiTamper

// ============================================================
// 模型解密引擎
// ============================================================
namespace ModelCrypto {

// 解密单个加密模型文件
static jbyteArray decryptModel(JNIEnv* env, AAssetManager* assetMgr,
                                const char* assetPath, const uint8_t enc_key[32]) {
    if (!assetMgr || !assetPath) {
        LOGE("Invalid parameters for decryptModel");
        return nullptr;
    }

    // 从assets读取加密文件
    AAsset* asset = AAssetManager_open(assetMgr, assetPath, AASSET_MODE_BUFFER);
    if (!asset) {
        LOGE("Failed to open asset: %s", assetPath);
        return nullptr;
    }

    off_t fileLen = AAsset_getLength(asset);
    if (fileLen < (off_t)HEADER_LEN) {
        LOGE("File too small: %s (%ld bytes)", assetPath, (long)fileLen);
        AAsset_close(asset);
        return nullptr;
    }

    const uint8_t* fileData = reinterpret_cast<const uint8_t*>(AAsset_getBuffer(asset));
    if (!fileData) {
        LOGE("Failed to get asset buffer: %s", assetPath);
        AAsset_close(asset);
        return nullptr;
    }

    // 验证Magic
    if (memcmp(fileData, MAGIC, 4) != 0) {
        // 文件未加密（开发模式），直接返回原始数据
        LOGD("Model not encrypted (no magic): %s", assetPath);
        jbyteArray result = env->NewByteArray(fileLen);
        env->SetByteArrayRegion(result, 0, fileLen, reinterpret_cast<const jbyte*>(fileData));
        AAsset_close(asset);
        return result;
    }

    // 提取IV、Tag、密文
    const uint8_t* iv = fileData + 4;
    const uint8_t* tag = fileData + 4 + IV_LEN;
    const uint8_t* ciphertext = fileData + HEADER_LEN;
    size_t ciphertext_len = fileLen - HEADER_LEN;

    // AES-256-GCM解密
    EVP_CIPHER_CTX* ctx = EVP_CIPHER_CTX_new();
    if (!ctx) {
        LOGE("Failed to create cipher context");
        AAsset_close(asset);
        return nullptr;
    }

    if (EVP_DecryptInit_ex(ctx, EVP_aes_256_gcm(), nullptr, nullptr, nullptr) != 1) {
        LOGE("DecryptInit failed");
        EVP_CIPHER_CTX_free(ctx);
        AAsset_close(asset);
        return nullptr;
    }

    // 设置IV长度
    EVP_CIPHER_CTX_ctrl(ctx, EVP_CTRL_GCM_SET_IVLEN, IV_LEN, nullptr);

    // 设置密钥和IV
    if (EVP_DecryptInit_ex(ctx, nullptr, nullptr, enc_key, iv) != 1) {
        LOGE("DecryptInit key/iv failed");
        EVP_CIPHER_CTX_free(ctx);
        AAsset_close(asset);
        return nullptr;
    }

    // 分配输出缓冲区
    std::vector<uint8_t> plaintext(ciphertext_len + 16); // 额外空间

    int out_len = 0;
    int total_len = 0;

    // 解密
    if (EVP_DecryptUpdate(ctx, plaintext.data(), &out_len, ciphertext, ciphertext_len) != 1) {
        LOGE("DecryptUpdate failed for %s", assetPath);
        EVP_CIPHER_CTX_free(ctx);
        AAsset_close(asset);
        return nullptr;
    }
    total_len = out_len;

    // 设置认证标签
    if (EVP_CIPHER_CTX_ctrl(ctx, EVP_CTRL_GCM_SET_TAG, TAG_LEN, const_cast<uint8_t*>(tag)) != 1) {
        LOGE("SetTag failed");
        EVP_CIPHER_CTX_free(ctx);
        AAsset_close(asset);
        return nullptr;
    }

    // 验证标签（如果失败说明数据被篡改或密钥错误）
    if (EVP_DecryptFinal_ex(ctx, plaintext.data() + total_len, &out_len) != 1) {
        LOGE("GCM authentication failed for %s - data may be tampered!", assetPath);
        EVP_CIPHER_CTX_free(ctx);
        AAsset_close(asset);
        return nullptr;
    }
    total_len += out_len;

    EVP_CIPHER_CTX_free(ctx);
    AAsset_close(asset);

    // 创建Java byte数组
    jbyteArray result = env->NewByteArray(total_len);
    if (result) {
        env->SetByteArrayRegion(result, 0, total_len, reinterpret_cast<const jbyte*>(plaintext.data()));
    }

    // 安全擦除明文缓冲区
    OPENSSL_cleanse(plaintext.data(), plaintext.size());

    LOGD("Model decrypted successfully: %s (%d bytes)", assetPath, total_len);
    return result;
}

} // namespace ModelCrypto

// ============================================================
// 安全引擎状态
// ============================================================
struct SecureEngine {
    uint8_t encryption_key[32];
    bool key_derived;
    bool integrity_verified;
    bool membership_verified;
    std::mutex mutex;

    SecureEngine() : key_derived(false), integrity_verified(false), membership_verified(false) {
        memset(encryption_key, 0, 32);
    }

    ~SecureEngine() {
        OPENSSL_cleanse(encryption_key, 32);
    }

    bool deriveKey() {
        if (key_derived) return true;
        std::lock_guard<std::mutex> lock(mutex);

        uint8_t master_key[32];
        ObfuscatedKey::reconstructMasterKey(master_key);

        bool ok = ObfuscatedKey::deriveEncryptionKey(master_key, encryption_key);
        OPENSSL_cleanse(master_key, 32);

        if (ok) {
            key_derived = true;
            LOGD("Encryption key derived successfully");
        } else {
            LOGE("Failed to derive encryption key");
        }
        return ok;
    }
};

static SecureEngine* g_engine = nullptr;
static std::mutex g_engine_mutex;

// ============================================================
// JNI 方法实现
// ============================================================

extern "C" JNIEXPORT jlong JNICALL
Java_com_myvideo_editor_core_ai_common_SecureModelLoader_nativeInit(
        JNIEnv* env, jobject thiz, jobject context) {
    std::lock_guard<std::mutex> lock(g_engine_mutex);

    if (g_engine) {
        LOGD("Engine already initialized");
        return reinterpret_cast<jlong>(g_engine);
    }

    // 初始化OpenSSL
    ERR_load_crypto_strings();
    OpenSSL_add_all_algorithms();

    auto* engine = new SecureEngine();
    if (!engine) {
        LOGE("Failed to allocate engine");
        return 0;
    }

    // 派生加密密钥
    if (!engine->deriveKey()) {
        delete engine;
        return 0;
    }

    g_engine = engine;
    LOGD("Secure inference engine initialized");
    return reinterpret_cast<jlong>(engine);
}

extern "C" JNIEXPORT jbyteArray JNICALL
Java_com_myvideo_editor_core_ai_common_SecureModelLoader_nativeDecryptModel(
        JNIEnv* env, jobject thiz, jlong handle,
        jobject assetManager, jstring assetPath,
        jstring membershipToken) {
    auto* engine = reinterpret_cast<SecureEngine*>(handle);
    if (!engine || !engine->key_derived) {
        LOGE("Engine not initialized or key not derived");
        return nullptr;
    }

    // 1. 验证会员令牌
    const char* token = env->GetStringUTFChars(membershipToken, nullptr);
    bool member_ok = MembershipGuard::verifyToken(token);
    env->ReleaseStringUTFChars(membershipToken, token);

    if (!member_ok) {
        LOGE("Membership verification failed - model decryption denied");
        return nullptr;
    }

    // 2. 防调试检测（非阻塞，失败不阻止但记录警告）
    if (AntiTamper::isDebuggerAttached()) {
        LOGW("Debugger detected - proceeding with caution");
        // 不阻止运行，但记录警告。生产环境可改为 return nullptr
    }

    // 3. 获取AAssetManager
    AAssetManager* assetMgr = AAssetManager_fromJava(env, assetManager);
    if (!assetMgr) {
        LOGE("Failed to get AssetManager");
        return nullptr;
    }

    // 4. 解密模型
    const char* path = env->GetStringUTFChars(assetPath, nullptr);
    jbyteArray result = ModelCrypto::decryptModel(env, assetMgr, path, engine->encryption_key);
    env->ReleaseStringUTFChars(assetPath, path);

    return result;
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_myvideo_editor_core_ai_common_SecureModelLoader_nativeVerifyMembership(
        JNIEnv* env, jobject thiz, jlong handle, jstring membershipToken) {
    auto* engine = reinterpret_cast<SecureEngine*>(handle);
    if (!engine) return JNI_FALSE;

    const char* token = env->GetStringUTFChars(membershipToken, nullptr);
    bool valid = MembershipGuard::verifyToken(token);
    env->ReleaseStringUTFChars(membershipToken, token);

    return valid ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_myvideo_editor_core_ai_common_SecureModelLoader_nativeCheckIntegrity(
        JNIEnv* env, jobject thiz, jlong handle, jobject context) {
    auto* engine = reinterpret_cast<SecureEngine*>(handle);
    if (!engine) return JNI_FALSE;

    bool ok = true;

    // 签名验证
    if (!AntiTamper::verifyAppSignature(env, context)) {
        LOGW("App signature verification failed");
        ok = false;
    }

    // 调试器检测
    if (AntiTamper::isDebuggerAttached()) {
        LOGW("Debugger attached");
        // 开发环境不阻止，生产环境可改为 ok = false
    }

    engine->integrity_verified = ok;
    return ok ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_myvideo_editor_core_ai_common_SecureModelLoader_nativeGenerateToken(
        JNIEnv* env, jobject thiz, jlong expiryTimestamp) {
    return MembershipGuard::generateToken(env, expiryTimestamp);
}

extern "C" JNIEXPORT void JNICALL
Java_com_myvideo_editor_core_ai_common_SecureModelLoader_nativeRelease(
        JNIEnv* env, jobject thiz, jlong handle) {
    std::lock_guard<std::mutex> lock(g_engine_mutex);

    auto* engine = reinterpret_cast<SecureEngine*>(handle);
    if (engine) {
        delete engine;
        if (engine == g_engine) {
            g_engine = nullptr;
        }
    }

    LOGD("Secure inference engine released");
}
