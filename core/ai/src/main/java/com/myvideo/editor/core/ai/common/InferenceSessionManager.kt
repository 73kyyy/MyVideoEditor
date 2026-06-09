package com.myvideo.editor.core.ai.common

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import java.nio.FloatBuffer

class InferenceSessionManager {
    private var env: OrtEnvironment? = null
    private val sessions = mutableMapOf<String, OrtSession>()

    fun init(): Boolean {
        return try { env = OrtEnvironment.getEnvironment(); true }
        catch (e: Exception) { false }
    }

    /**
     * 从文件路径加载模型（未加密模型或调试用）
     */
    fun loadModel(modelPath: String, modelId: String): Boolean {
        val e = env ?: return false
        return try {
            val opts = OrtSession.SessionOptions().apply { setOptimizationLevel(OrtSession.SessionOptions.OptLevel.ALL_OPT) }
            sessions[modelId] = e.createSession(modelPath, opts)
            true
        } catch (ex: Exception) { false }
    }

    /**
     * 从字节数组加载模型（安全模式：C++解密后的模型数据）
     * 模型不落盘，直接从内存加载，防止文件被提取
     */
    fun loadModelFromBytes(modelId: String, modelBytes: ByteArray): Boolean {
        val e = env ?: return false
        return try {
            val opts = OrtSession.SessionOptions().apply { setOptimizationLevel(OrtSession.SessionOptions.OptLevel.ALL_OPT) }
            sessions[modelId] = e.createSession(modelBytes, opts)
            true
        } catch (ex: Exception) { false }
    }

    fun run(modelId: String, inputName: String, input: FloatArray, shape: LongArray): FloatArray? {
        val session = sessions[modelId] ?: return null
        val e = env ?: return null
        return try {
            val tensor = OnnxTensor.createTensor(e, FloatBuffer.wrap(input), shape)
            val result = session.run(mapOf(inputName to tensor))
            val output = result[0].value
            tensor.close(); result.close()
            when (output) { is FloatArray -> output; is Array<*> -> { val arr = output as Array<FloatArray>; arr.flatMap { it.toList() }.toFloatArray() }; else -> null }
        } catch (ex: Exception) { null }
    }

    fun runMulti(modelId: String, inputs: Map<String, Pair<FloatArray, LongArray>>): FloatArray? {
        val session = sessions[modelId] ?: return null
        val e = env ?: return null
        return try {
            val inputTensors = inputs.map { (name, pair) ->
                val (data, shape) = pair
                name to OnnxTensor.createTensor(e, FloatBuffer.wrap(data), shape)
            }.toMap()
            val result = session.run(inputTensors)
            val output = result[0].value
            inputTensors.values.forEach { it.close() }
            result.close()
            when (output) {
                is FloatArray -> output
                is Array<*> -> {
                    val arr = output as Array<FloatArray>
                    arr.flatMap { it.toList() }.toFloatArray()
                }
                else -> null
            }
        } catch (ex: Exception) { null }
    }

    fun release(modelId: String) { sessions.remove(modelId)?.close() }
    fun releaseAll() { sessions.values.forEach { it.close() }; sessions.clear() }
    fun isLoaded(modelId: String): Boolean = sessions.containsKey(modelId)
}
