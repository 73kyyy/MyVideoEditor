package com.myvideo.editor.core.ai

import android.content.Context
import java.io.File

class ModelRegistry(private val context: Context) {

    data class RegisteredModel(
        val id: String,
        val name: String,
        val category: String,
        val assetPath: String,
        val precision: String,
        val sizeMb: Int,
        val requiresMembership: Boolean = true
    )

    // 方案A：全部打包进APK
    private val models = listOf(
        RegisteredModel("rnnoise", "RNNoise降噪", "audio", "ai_models/rnnoise_fp32.bin", "FP32", 5),
        RegisteredModel("rife_v4", "RIFE插帧", "video", "ai_models/rife_v4_fp32.bin", "FP32", 60),
        RegisteredModel("sam2_base", "SAM2抠图", "segmentation", "ai_models/sam2_base_fp32.bin", "FP32", 156),
        RegisteredModel("esrgan_x4", "ESRGAN超分", "superres", "ai_models/esrgan_x4_fp32.bin", "FP32", 128),
        RegisteredModel("whisper_tiny", "Whisper语音识别", "audio", "ai_models/whisper_tiny_fp32.bin", "FP32", 75),
        RegisteredModel("demucs_ft", "Demucs人声分离", "audio", "ai_models/demucs_ft_fp16.bin", "FP16", 160)
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
        val localFile = File(context.filesDir, model.assetPath)
        if (localFile.exists()) return localFile.absolutePath
        return null
    }

    fun isModelInstalled(modelId: String): Boolean {
        val model = models.find { it.id == modelId } ?: return false
        val localFile = File(context.filesDir, model.assetPath)
        return localFile.exists() && localFile.length() > 0
    }

    fun extractModelFromAssets(modelId: String): Boolean {
        val model = models.find { it.id == modelId } ?: return false
        val destFile = File(context.filesDir, model.assetPath)
        if (destFile.exists() && destFile.length() > 0) return true
        return try {
            destFile.parentFile?.mkdirs()
            context.assets.open(model.assetPath).use { input ->
                destFile.outputStream().use { output -> input.copyTo(output) }
            }
            true
        } catch (e: Exception) { false }
    }

    fun extractAllModels(): Boolean = models.all { extractModelFromAssets(it.id) }
    fun getExtractProgress(): Pair<Int, Int> {
        val installed = models.count { isModelInstalled(it.id) }
        return installed to models.size
    }
}
