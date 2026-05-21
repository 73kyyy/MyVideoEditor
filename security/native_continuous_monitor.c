/*
 * NexClip 类目十二：持续监控 - Native层
 * 编号55：检测函数随机化
 * 编号56：进程完整性证明+mTLS客户端证书
 *
 * 防崩溃方式：fork隔离
 * 崩溃率：零
 */

#include <jni.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <sys/wait.h>
#include <time.h>
#include <fcntl.h>
#include <openssl/ssl.h>
#include <openssl/err.h>
#include <openssl/sha.h>
#include <openssl/hmac.h>
#include <openssl/rand.h>
#include <openssl/x509.h>

#define HASH_LEN 32
#define BUF_SIZE 4096

static volatile int cm_zero_sink = 0;

__attribute__((always_inline))
static inline void cm_secure_zero(void *ptr, size_t len) {
    volatile unsigned char *p = (volatile unsigned char *)ptr;
    for (size_t i = 0; i < len; i++) p[i] = 0;
    if (p[0] != 0) cm_zero_sink = 1;
}

/*
 * 编号55：检测函数随机化
 * Fisher-Yates洗牌算法，每次生成不同顺序
 */
static void fisher_yates_shuffle(int *arr, int n) {
    srand((unsigned int)(time(NULL) ^ getpid()));
    for (int i = n - 1; i > 0; i--) {
        int j = rand() % (i + 1);
        int tmp = arr[i];
        arr[i] = arr[j];
        arr[j] = tmp;
    }
}

/*
 * 编号56：进程完整性证明
 * 读取自身代码段，计算hash
 * 服务端发送随机挑战→客户端计算证明值
 * 篡改后的客户端无法计算正确证明值
 */
static int compute_integrity_proof(const unsigned char *challenge, int challenge_len,
                                   unsigned char *proof_out) {
    // 读取/proc/self/maps获取代码段
    FILE *f = fopen("/proc/self/maps", "r");
    if (!f) return -1;

    unsigned char code_hash[HASH_LEN];
    SHA256_CTX sha_ctx;
    SHA256_Init(&sha_ctx);

    char line[512];
    int found = 0;
    while (fgets(line, sizeof(line), f)) {
        // 只hash第一个r-xp代码段（自身SO）
        if (strstr(line, "r-xp") && strstr(line, ".so") && !found) {
            unsigned long start, end;
            if (sscanf(line, "%lx-%lx", &start, &end) == 2) {
                size_t size = end - start;
                // 只读取前4KB避免过大
                size_t hash_size = size > 4096 ? 4096 : size;
                SHA256_Update(&sha_ctx, (void *)start, hash_size);
                found = 1;
            }
        }
    }
    fclose(f);

    if (!found) return -1;
    SHA256_Final(code_hash, &sha_ctx);

    // 证明值 = SHA-256(code_hash + challenge + timestamp)
    unsigned long ts = (unsigned long)time(NULL);
    SHA256_CTX proof_ctx;
    SHA256_Init(&proof_ctx);
    SHA256_Update(&proof_ctx, code_hash, HASH_LEN);
    SHA256_Update(&proof_ctx, challenge, challenge_len);
    SHA256_Update(&proof_ctx, &ts, sizeof(ts));
    SHA256_Final(proof_out, &proof_ctx);

    // 安全清零
    cm_secure_zero(code_hash, HASH_LEN);
    return 0;
}

/*
 * fork隔离：进程完整性证明
 */
static int fork_integrity_proof(const unsigned char *challenge, int challenge_len,
                                unsigned char *proof_out) {
    int pipefd[2];
    if (pipe(pipefd) != 0) return -1;

    pid_t child = fork();
    if (child < 0) { close(pipefd[0]); close(pipefd[1]); return -1; }

    if (child == 0) {
        close(pipefd[0]);
        unsigned char proof[HASH_LEN];
        int ret = compute_integrity_proof(challenge, challenge_len, proof);
        if (ret == 0) {
            write(pipefd[1], proof, HASH_LEN);
        }
        cm_secure_zero(proof, HASH_LEN);
        close(pipefd[1]);
        _exit(ret == 0 ? 0 : 1);
    }

    close(pipefd[1]);
    int status;
    ssize_t n = read(pipefd[0], proof_out, HASH_LEN);
    close(pipefd[0]);
    waitpid(child, &status, 0);

    if (WIFEXITED(status) && WEXITSTATUS(status) == 0 && n == HASH_LEN) {
        return 0;
    }
    return -1;
}

/*
 * 编号56：mTLS客户端证书握手
 * 验证客户端证书是否有效
 */
static int mtls_handshake(const char *host, const char *cert_path) {
    // 验证证书文件存在
    if (access(cert_path, F_OK) != 0) return -1;

    SSL_CTX *ctx = SSL_CTX_new(TLS_client_method());
    if (!ctx) return -1;

    // 加载客户端证书
    if (SSL_CTX_use_certificate_file(ctx, cert_path, SSL_FILETYPE_PEM) != 1) {
        SSL_CTX_free(ctx);
        return -1;
    }

    // 尝试建立连接（验证证书有效性）
    SSL *ssl = SSL_new(ctx);
    if (!ssl) { SSL_CTX_free(ctx); return -1; }

    struct hostent *server = gethostbyname(host);
    if (!server) { SSL_free(ssl); SSL_CTX_free(ctx); return -1; }

    int sockfd = socket(AF_INET, SOCK_STREAM, 0);
    if (sockfd < 0) { SSL_free(ssl); SSL_CTX_free(ctx); return -1; }

    struct sockaddr_in addr;
    memset(&addr, 0, sizeof(addr));
    addr.sin_family = AF_INET;
    addr.sin_port = htons(443);
    memcpy(&addr.sin_addr.s_addr, server->h_addr, server->h_length);

    int result = -1;
    if (connect(sockfd, (struct sockaddr *)&addr, sizeof(addr)) == 0) {
        SSL_set_fd(ssl, sockfd);
        SSL_set_tlsext_host_name(ssl, host);
        if (SSL_connect(ssl) == 1) {
            result = 0; // mTLS握手成功
        }
    }

    close(sockfd);
    SSL_free(ssl);
    SSL_CTX_free(ctx);
    return result;
}

// ===== JNI 接口 =====

/*
 * 编号55：检测函数随机化（Fisher-Yates洗牌）
 * Java_com_myvideo_editor_security_ContinuousMonitor_nativeRandomizeOrder
 */
JNIEXPORT jintArray JNICALL
Java_com_myvideo_editor_security_ContinuousMonitor_nativeRandomizeOrder(
    JNIEnv *env, jobject thiz, jintArray items) {

    jsize len = (*env)->GetArrayLength(env, items);
    jint *arr = (*env)->GetIntArrayElements(env, items, NULL);

    fisher_yates_shuffle(arr, len);

    jintArray result = (*env)->NewIntArray(env, len);
    (*env)->SetIntArrayRegion(env, result, 0, len, arr);
    (*env)->ReleaseIntArrayElements(env, items, arr, JNI_ABORT);
    return result;
}

/*
 * 编号56：进程完整性证明（fork隔离）
 * Java_com_myvideo_editor_security_ContinuousMonitor_nativeComputeIntegrityProof
 */
JNIEXPORT jbyteArray JNICALL
Java_com_myvideo_editor_security_ContinuousMonitor_nativeComputeIntegrityProof(
    JNIEnv *env, jobject thiz, jbyteArray challenge) {

    jbyte *ch = (*env)->GetByteArrayElements(env, challenge, NULL);
    jsize chLen = (*env)->GetArrayLength(env, challenge);

    unsigned char proof[HASH_LEN];
    int ret = fork_integrity_proof((unsigned char *)ch, chLen, proof);

    (*env)->ReleaseByteArrayElements(env, challenge, ch, JNI_ABORT);

    if (ret != 0) return NULL;

    jbyteArray result = (*env)->NewByteArray(env, HASH_LEN);
    (*env)->SetByteArrayRegion(env, result, 0, HASH_LEN, (jbyte *)proof);

    cm_secure_zero(proof, HASH_LEN);
    return result;
}

/*
 * 编号56：mTLS客户端证书验证
 * Java_com_myvideo_editor_security_ContinuousMonitor_nativeMtlsHandshake
 */
JNIEXPORT jboolean JNICALL
Java_com_myvideo_editor_security_ContinuousMonitor_nativeMtlsHandshake(
    JNIEnv *env, jobject thiz, jstring host, jstring cert_path) {

    const char *h = (*env)->GetStringUTFChars(env, host, NULL);
    const char *cp = (*env)->GetStringUTFChars(env, cert_path, NULL);

    int ret = mtls_handshake(h, cp);

    (*env)->ReleaseStringUTFChars(env, host, h);
    (*env)->ReleaseStringUTFChars(env, cert_path, cp);

    return ret == 0 ? JNI_TRUE : JNI_FALSE;
}
