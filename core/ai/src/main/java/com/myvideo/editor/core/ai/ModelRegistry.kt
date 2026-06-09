package com.myvideo.editor.core.ai

import android.content.Context
import java.io.File

class ModelRegistry(private val context: Context) {

    data class RegisteredModel(
        val id: String,
        val name: String,
        val category: String,
        val assetFiles: List<String>,
        val precision: String,
        val sizeMb: Int,
        val requiresMembership: Boolean = true
    )

    // All models are FP16 (converted in CI/CD)
    private val models = listOf(
        RegisteredModel("rnnoise", "RNNoise降噪", "audio",
            listOf("ai_models/rnnoise.onnx"), "FP16", 3),
        RegisteredModel("rife_v4", "RIFE插帧", "video",
            listOf("ai_models/rife_v4.onnx"), "FP16", 9),
        RegisteredModel("sam2_base", "SAM2抠图", "segmentation",
            listOf("ai_models/sam_encoder.onnx", "ai_models/sam_decoder.onnx"), "FP16", 24),
        RegisteredModel("esrgan_x4", "ESRGAN超分", "superres",
            listOf("ai_models/realesrgan_x4plus.onnx"), "FP16", 34),
        RegisteredModel("whisper_tiny", "Whisper语音识别", "audio",
            listOf("ai_models/whisper_encoder.onnx", "ai_models/whisper_decoder.onnx", "ai_models/whisper_tokens.txt"), "FP16", 30),
        RegisteredModel("demucs_ft", "Demucs人声分离", "audio",
            listOf("ai_models/demucs_htdemucs.onnx"), "FP16", 166)
    )

    fun getAllModels(): List<RegisteredModel> = models
    fun getModel(id: String): RegisteredModel? = models.find { it.id == id }
    fun getModelsByCategory(category: String): List<RegisteredModel> = models.filter { it.category == category }
    fun getTotalSizeMb(): Int = models.sumOf { it.sizeMb }

    fun isModelAvailable(modelId: String, isMember: Boolean): Boolean {
        val model = models.find { it.id == modelId } ?: return false
        if (model.requiresMembership && !isMember) return false
        return true
    }

    fun getModelLocalPath(modelId: String): String? {
        val model = models.find { it.id == modelId } ?: return null
        val firstFile = model.assetFiles.firstOrNull() ?: return null
        val localFile = File(context.filesDir, firstFile)
        if (localFile.exists()) return localFile.absolutePath
        return null
    }

    fun getModelAssetPath(modelId: String): String? {
        val model = models.find { it.id == modelId } ?: return null
        return model.assetFiles.firstOrNull()
    }

    fun getAllAssetPaths(modelId: String): List<String> {
        val model = models.find { it.id == modelId } ?: return emptyList()
        return model.assetFiles
    }

    fun isModelInstalled(modelId: String): Boolean {
        val model = models.find { it.id == modelId } ?: return false
        return model.assetFiles.all { assetPath ->
            val localFile = File(context.filesDir, assetPath)
            localFile.exists() && localFile.length() > 0
        }
    }

    fun extractModelFromAssets(modelId: String): Boolean {
        val model = models.find { it.id == modelId } ?: return false
        return model.assetFiles.all { assetPath ->
            val destFile = File(context.filesDir, assetPath)
            if (destFile.exists() && destFile.length() > 0) return@all true
            try {
                destFile.parentFile?.mkdirs()
                context.assets.open(assetPath).use { input ->
                    destFile.outputStream().use { output -> input.copyTo(output) }
                }
                true
            } catch (e: Exception) { false }
        }
    }

    fun extractAllModels(): Boolean = models.all { extractModelFromAssets(it.id) }
    fun getExtractProgress(): Pair<Int, Int> {
        val installed = models.count { isModelInstalled(it.id) }
        return installed to models.size
    }
}
