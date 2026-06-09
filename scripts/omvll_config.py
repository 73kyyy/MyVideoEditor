"""
O-MVLL 混淆配置 - NexClip 项目

⚠️  这是可选配置，不是必须的！
⚠️  使用前必须安装 O-MVLL 并使用 NDK r26d
⚠️  不要对 JNI 函数做控制流平坦化！
⚠️  只使用 StringEncOptStack，不要用 StringEncOptGlobal（有 SIGSEGV bug）

安装步骤:
1. pip install omvll
2. 下载 O-MVLL 预编译包: https://github.com/open-obfuscator/o-mvll/releases
3. 设置环境变量:
   export OMVLL_CONFIG=/path/to/omvll_config.py
   export OMVLL_PYTHONPATH=$(python3 -c "import site; print(site.getsitepackages()[0])")
   export LD_LIBRARY_PATH=/path/to/omvll/lib:$LD_LIBRARY_PATH
4. 在 CMakeLists.txt 中添加:
   target_compile_options(your_lib PRIVATE -fpass-plugin=/path/to/libOMVLL.so)

当前状态: 未启用（需要手动安装 O-MVLL 后取消 CMakeLists.txt 中的注释）
"""

import omvll
from functools import lru_cache


class NexClipObfuscationConfig(omvll.ObfuscationConfig):
    """NexClip 项目 O-MVLL 混淆配置

    安全策略:
    - 仅对安全核心函数启用混淆
    - 所有 JNI 函数排除（防止崩溃）
    - 仅使用 StringEncOptStack（StringEncOptGlobal 有已知 SIGSEGV bug）
    - 控制流平坦化仅用于纯计算函数
    """

    def __init__(self):
        super().__init__()

        # ===== 全局排除列表 =====
        # 排除所有 JNI 入口函数 - 绝对不能混淆！
        omvll.config.global_func_exclude = [
            "JNI_OnLoad",
            "JNI_OnUnload",
        ]

        # 排除第三方代码
        omvll.config.global_mod_exclude = [
            "third_party/",
            "openssl/",
        ]

    def obfuscate_string(self, module, func, string):
        """字符串加密 - 仅对安全核心函数中的字符串启用

        ⚠️ 只使用 StringEncOptStack
        ⚠️ 不要使用 StringEncOptGlobal（Issue #160: SIGSEGV）
        """
        # 仅对安全推理引擎中的关键函数加密字符串
        if func is None:
            return False

        func_name = func.demangled_name if func.demangled_name else func.name

        # 密钥相关函数 - 字符串必须加密
        key_functions = [
            "ObfuscatedKey::reconstructMasterKey",
            "ObfuscatedKey::deriveEncryptionKey",
            "MembershipGuard::getTokenSigningKey",
            "MembershipGuard::verifyToken",
            "MembershipGuard::generateToken",
            "AntiTamper::verifyAppSignature",
        ]

        for kf in key_functions:
            if kf in func_name:
                return omvll.StringEncOptStack(20)

        # 不加密其他函数的字符串
        return False

    def flatten_cfg(self, module, func):
        """控制流平坦化 - 仅对纯计算函数启用

        ⚠️ 绝对不能对 JNI 函数启用！
        ⚠️ 绝对不能对包含 JNIEnv 调用的函数启用！
        ⚠️ 绝对不能对包含异常处理的函数启用！
        """
        if func is None:
            return False

        func_name = func.name

        # 排除所有 JNI 函数
        if func_name.startswith("Java_") or func_name == "JNI_OnLoad":
            return False

        # 仅对纯计算函数启用控制流平坦化
        safe_flatten_functions = [
            "reconstructMasterKey",
            "deriveEncryptionKey",
            "getTokenSigningKey",
        ]

        for sf in safe_flatten_functions:
            if sf in func_name:
                return True

        return False

    def obfuscate_arithmetic(self, module, func):
        """MBA 算术混淆 - 仅对密钥运算函数启用"""
        if func is None:
            return False

        func_name = func.demangled_name if func.demangled_name else func.name

        # 仅对密钥 XOR 运算启用
        if "ObfuscatedKey" in func_name:
            return True

        return False

    def anti_hooking(self, module, func):
        """反 Hook - 仅对敏感 JNI 入口函数启用

        注意: 这会在函数入口注入 Frida 检测代码
        """
        if func is None:
            return False

        func_name = func.name

        # 仅对模型解密和会员验证的 JNI 入口注入反 Hook
        protected_jni_functions = [
            "Java_com_myvideo_editor_core_ai_common_SecureModelLoader_nativeDecryptModel",
            "Java_com_myvideo_editor_core_ai_common_SecureModelLoader_nativeVerifyMembership",
            "Java_com_myvideo_editor_core_ai_common_SecureModelLoader_nativeCheckIntegrity",
        ]

        return func_name in protected_jni_functions

    def obfuscate_struct_access(self, module, func, S):
        """结构体访问混淆

        ⚠️ 不要混淆 JNINativeInterface！可能导致所有 JNI 调用崩溃！
        """
        # 禁用 - 风险太高
        return False

    def break_control_flow(self, module, func):
        """控制流断裂 - 仅对非 JNI 函数启用"""
        if func is None:
            return False

        func_name = func.name

        # 排除所有 JNI 函数
        if func_name.startswith("Java_") or func_name == "JNI_OnLoad":
            return False

        # 仅对安全核心函数启用
        safe_functions = [
            "reconstructMasterKey",
            "deriveEncryptionKey",
            "verifyToken",
        ]

        for sf in safe_functions:
            if sf in func_name:
                return True

        return False


@lru_cache(maxsize=1)
def omvll_get_config():
    return NexClipObfuscationConfig()
