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

    fun loadModel(modelPath: String, modelId: String): Boolean {
        val e = env ?: return false
        return try {
            val opts = OrtSession.SessionOptions().apply { setOptimizationLevel(OrtSession.SessionOptions.OptLevel.ALL_OPT) }
            sessions[modelId] = e.createSession(modelPath, opts)
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

    fun release(modelId: String) { sessions.remove(modelId)?.close() }
    fun releaseAll() { sessions.values.forEach { it.close() }; sessions.clear() }
    fun isLoaded(modelId: String): Boolean = sessions.containsKey(modelId)
}
