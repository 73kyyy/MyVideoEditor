package com.myvideo.editor.core.ai.common

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import java.nio.FloatBuffer
import java.nio.LongBuffer

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
            flattenOutput(output)
        } catch (ex: Exception) { null }
    }

    fun runMulti(
        modelId: String,
        floatInputs: Map<String, Pair<FloatArray, LongArray>>,
        longInputs: Map<String, Pair<LongArray, LongArray>> = emptyMap()
    ): FloatArray? {
        val session = sessions[modelId] ?: return null
        val e = env ?: return null
        return try {
            val inputTensors = mutableMapOf<String, OnnxTensor>()
            for ((name, pair) in floatInputs) {
                inputTensors[name] = OnnxTensor.createTensor(e, FloatBuffer.wrap(pair.first), pair.second)
            }
            for ((name, pair) in longInputs) {
                inputTensors[name] = OnnxTensor.createTensor(e, LongBuffer.wrap(pair.first), pair.second)
            }
            val result = session.run(inputTensors)
            val output = result[0].value
            inputTensors.values.forEach { it.close() }
            result.close()
            flattenOutput(output)
        } catch (ex: Exception) { null }
    }

    private fun flattenOutput(output: Any): FloatArray? = when (output) {
        is FloatArray -> output
        is Array<*> -> {
            val flat = mutableListOf<Float>()
            fun flattenRecursive(arr: Any) {
                when (arr) {
                    is FloatArray -> flat.addAll(arr.toList())
                    is Array<*> -> arr.forEach { if (it != null) flattenRecursive(it) }
                }
            }
            flattenRecursive(output)
            flat.toFloatArray()
        }
        else -> null
    }

    fun release(modelId: String) { sessions.remove(modelId)?.close() }
    fun releaseAll() { sessions.values.forEach { it.close() }; sessions.clear() }
    fun isLoaded(modelId: String): Boolean = sessions.containsKey(modelId)
}
