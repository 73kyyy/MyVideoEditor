#include <jni.h>
#include <android/log.h>
#include <vector>
#include <cmath>
#include <cstring>

// OpenGL ES headers
#include <GLES2/gl2.h>
#include <GLES2/gl2ext.h>

#define TAG "RenderEngine"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

// Simple shader-based GPU renderer for video effects

// Vertex shader
static const char* VERTEX_SHADER =
    "attribute vec4 aPosition;\n"
    "attribute vec2 aTexCoord;\n"
    "varying vec2 vTexCoord;\n"
    "void main() {\n"
    "    gl_Position = aPosition;\n"
    "    vTexCoord = aTexCoord;\n"
    "}\n";

// Fragment shader for basic texture rendering
static const char* FRAG_TEXTURE =
    "precision mediump float;\n"
    "varying vec2 vTexCoord;\n"
    "uniform sampler2D uTexture;\n"
    "void main() {\n"
    "    gl_FragColor = texture2D(uTexture, vTexCoord);\n"
    "}\n";

// Fragment shader for blur effect
static const char* FRAG_BLUR =
    "precision mediump float;\n"
    "varying vec2 vTexCoord;\n"
    "uniform sampler2D uTexture;\n"
    "uniform vec2 uResolution;\n"
    "uniform float uRadius;\n"
    "void main() {\n"
    "    vec2 texelSize = 1.0 / uResolution;\n"
    "    vec4 sum = vec4(0.0);\n"
    "    float total = 0.0;\n"
    "    for (float x = -4.0; x <= 4.0; x += 1.0) {\n"
    "        for (float y = -4.0; y <= 4.0; y += 1.0) {\n"
    "            float weight = exp(-(x*x + y*y) / (2.0 * uRadius * uRadius));\n"
    "            sum += texture2D(uTexture, vTexCoord + vec2(x, y) * texelSize) * weight;\n"
    "            total += weight;\n"
    "        }\n"
    "    }\n"
    "    gl_FragColor = sum / total;\n"
    "}\n";

// Fragment shader for color grading
static const char* FRAG_COLOR_GRADE =
    "precision mediump float;\n"
    "varying vec2 vTexCoord;\n"
    "uniform sampler2D uTexture;\n"
    "uniform float uBrightness;\n"
    "uniform float uContrast;\n"
    "uniform float uSaturation;\n"
    "uniform float uTemperature;\n"
    "void main() {\n"
    "    vec4 color = texture2D(uTexture, vTexCoord);\n"
    "    // Brightness\n"
    "    color.rgb += uBrightness;\n"
    "    // Contrast\n"
    "    color.rgb = (color.rgb - 0.5) * uContrast + 0.5;\n"
    "    // Saturation\n"
    "    float gray = dot(color.rgb, vec3(0.299, 0.587, 0.114));\n"
    "    color.rgb = mix(vec3(gray), color.rgb, uSaturation);\n"
    "    // Temperature\n"
    "    color.r += uTemperature * 0.1;\n"
    "    color.b -= uTemperature * 0.1;\n"
    "    gl_FragColor = clamp(color, 0.0, 1.0);\n"
    "}\n";

static GLuint g_program = 0;
static GLuint g_blur_program = 0;
static GLuint g_grade_program = 0;
static GLuint g_texture = 0;
static GLint g_position_loc = -1;
static GLint g_texcoord_loc = -1;

static GLuint compileShader(GLenum type, const char* source) {
    GLuint shader = glCreateShader(type);
    glShaderSource(shader, 1, &source, nullptr);
    glCompileShader(shader);
    GLint compiled = 0;
    glGetShaderiv(shader, GL_COMPILE_STATUS, &compiled);
    if (!compiled) {
        GLint len = 0;
        glGetShaderiv(shader, GL_INFO_LOG_LENGTH, &len);
        if (len > 0) {
            char* log = new char[len];
            glGetShaderInfoLog(shader, len, nullptr, log);
            LOGE("Shader compile error: %s", log);
            delete[] log;
        }
        glDeleteShader(shader);
        return 0;
    }
    return shader;
}

static GLuint createProgram(const char* vertSrc, const char* fragSrc) {
    GLuint vert = compileShader(GL_VERTEX_SHADER, vertSrc);
    GLuint frag = compileShader(GL_FRAGMENT_SHADER, fragSrc);
    if (!vert || !frag) return 0;

    GLuint program = glCreateProgram();
    glAttachShader(program, vert);
    glAttachShader(program, frag);
    glLinkProgram(program);

    GLint linked = 0;
    glGetProgramiv(program, GL_LINK_STATUS, &linked);
    if (!linked) {
        LOGE("Program link failed");
        glDeleteProgram(program);
        program = 0;
    }

    glDeleteShader(vert);
    glDeleteShader(frag);
    return program;
}

extern "C" JNIEXPORT jlong JNICALL
Java_com_myvideo_editor_core_export_RenderEngine_nativeInit(JNIEnv *env, jobject thiz) {
    g_program = createProgram(VERTEX_SHADER, FRAG_TEXTURE);
    g_blur_program = createProgram(VERTEX_SHADER, FRAG_BLUR);
    g_grade_program = createProgram(VERTEX_SHADER, FRAG_COLOR_GRADE);

    if (!g_program) { LOGE("Failed to create render programs"); return 0; }

    // Create texture
    glGenTextures(1, &g_texture);
    glBindTexture(GL_TEXTURE_2D, g_texture);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

    LOGD("RenderEngine initialized: program=%u blur=%u grade=%u", g_program, g_blur_program, g_grade_program);
    return reinterpret_cast<jlong>(1);
}

extern "C" JNIEXPORT void JNICALL
Java_com_myvideo_editor_core_export_RenderEngine_nativeRenderFrame(JNIEnv *env, jobject thiz,
    jlong handle, jbyteArray frameData, jint w, jint h) {
    if (!g_program) return;

    jbyte* data = env->GetByteArrayElements(frameData, nullptr);

    // Upload texture
    glBindTexture(GL_TEXTURE_2D, g_texture);
    glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, w, h, 0, GL_RGBA, GL_UNSIGNED_BYTE, data);

    // Render
    glUseProgram(g_program);

    // Full-screen quad
    GLfloat vertices[] = {
        -1, -1, 0, 1, -1, 0, -1, 1, 0, 1, 1, 0
    };
    GLfloat texCoords[] = {
        0, 1, 1, 1, 0, 0, 1, 0
    };

    GLint posLoc = glGetAttribLocation(g_program, "aPosition");
    GLint texLoc = glGetAttribLocation(g_program, "aTexCoord");

    glEnableVertexAttribArray(posLoc);
    glVertexAttribPointer(posLoc, 3, GL_FLOAT, GL_FALSE, 0, vertices);
    glEnableVertexAttribArray(texLoc);
    glVertexAttribPointer(texLoc, 2, GL_FLOAT, GL_FALSE, 0, texCoords);

    glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);

    glDisableVertexAttribArray(posLoc);
    glDisableVertexAttribArray(texLoc);

    env->ReleaseByteArrayElements(frameData, data, JNI_ABORT);
}

extern "C" JNIEXPORT void JNICALL
Java_com_myvideo_editor_core_export_RenderEngine_nativeApplyBlur(JNIEnv *env, jobject thiz,
    jlong handle, jfloat radius) {
    if (!g_blur_program) return;
    glUseProgram(g_blur_program);
    glUniform1f(glGetUniformLocation(g_blur_program, "uRadius"), radius);
    GLint viewport[4];
    glGetIntegerv(GL_VIEWPORT, viewport);
    glUniform2f(glGetUniformLocation(g_blur_program, "uResolution"), viewport[2], viewport[3]);
}

extern "C" JNIEXPORT void JNICALL
Java_com_myvideo_editor_core_export_RenderEngine_nativeApplyColorGrade(JNIEnv *env, jobject thiz,
    jlong handle, jfloat brightness, jfloat contrast, jfloat saturation, jfloat temperature) {
    if (!g_grade_program) return;
    glUseProgram(g_grade_program);
    glUniform1f(glGetUniformLocation(g_grade_program, "uBrightness"), brightness);
    glUniform1f(glGetUniformLocation(g_grade_program, "uContrast"), contrast);
    glUniform1f(glGetUniformLocation(g_grade_program, "uSaturation"), saturation);
    glUniform1f(glGetUniformLocation(g_grade_program, "uTemperature"), temperature);
}

extern "C" JNIEXPORT void JNICALL
Java_com_myvideo_editor_core_export_RenderEngine_nativeRelease(JNIEnv *env, jobject thiz, jlong handle) {
    if (g_program) glDeleteProgram(g_program);
    if (g_blur_program) glDeleteProgram(g_blur_program);
    if (g_grade_program) glDeleteProgram(g_grade_program);
    if (g_texture) glDeleteTextures(1, &g_texture);
    g_program = g_blur_program = g_grade_program = g_texture = 0;
    LOGD("RenderEngine released");
}
