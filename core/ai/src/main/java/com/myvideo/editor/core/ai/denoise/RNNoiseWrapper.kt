package com.myvideo.editor.core.ai.denoise

import com.myvideo.editor.core.ai.common.InferenceSessionManager
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

class RNNoiseWrapper(private val sessionManager: InferenceSessionManager) {
    private var modelId = "rnnoise"
    var isReady = false; private set

    companion object {
        private const val FRAME_SIZE = 480       // 10ms at 48kHz
        private const val NUM_BANDS = 65          // RNNoise feature bands
        private const val SAMPLE_RATE = 48000
        private const val MIN_FRAMES = 5          // Minimum frames for Conv1d (kernel=3, 2 layers)
    }

    fun init(modelPath: String): Boolean {
        isReady = sessionManager.loadModel(modelPath, modelId)
        return isReady
    }

    fun denoise(pcmData: FloatArray): FloatArray? {
        if (!isReady) return null
        val numFrames = pcmData.size / FRAME_SIZE
        if (numFrames < MIN_FRAMES) return pcmData.copyOf()

        // Step 1: Extract features from PCM (band energy computation)
        val features = extractFeatures(pcmData, numFrames)

        // Step 2: Run ONNX inference
        val gains = sessionManager.run(modelId, "features", features, longArrayOf(1, numFrames.toLong(), NUM_BANDS.toLong()))
            ?: return pcmData.copyOf()

        // Step 3: Apply gains to PCM data
        return applyGains(pcmData, gains, numFrames)
    }

    fun denoiseChunked(pcmData: FloatArray, chunkSize: Int = FRAME_SIZE * 100): FloatArray {
        val result = FloatArray(pcmData.size)
        var offset = 0
        while (offset < pcmData.size) {
            val end = min(offset + chunkSize, pcmData.size)
            val chunk = pcmData.copyOfRange(offset, end)
            val denoised = denoise(chunk) ?: chunk
            System.arraycopy(denoised, 0, result, offset, denoised.size)
            offset += chunkSize
        }
        return result
    }

    private fun extractFeatures(pcm: FloatArray, numFrames: Int): FloatArray {
        val features = FloatArray(numFrames * NUM_BANDS)

        // Bark-scale band boundaries (simplified from xiph/rnnoise)
        val bandBounds = intArrayOf(
            0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19,
            20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37,
            38, 39, 40, 42, 44, 46, 48, 50, 52, 54, 56, 58, 60, 64, 68, 72, 76, 80,
            88, 96, 104, 112, 128, 144, 160, 176, 192, 224
        )

        for (f in 0 until numFrames) {
            val frameOffset = f * FRAME_SIZE

            // Compute band energies using simple windowed FFT approximation
            val bandEnergies = FloatArray(NUM_BANDS)
            for (b in 0 until NUM_BANDS - 1) {
                var energy = 0.0f
                val lo = bandBounds[b]
                val hi = bandBounds[b + 1]
                for (k in lo until hi) {
                    // Simple DFT at frequency bin k
                    var real = 0.0f; var imag = 0.0f
                    for (n in 0 until FRAME_SIZE) {
                        val idx = frameOffset + n
                        val sample = if (idx < pcm.size) pcm[idx] else 0f
                        val angle = -2.0f * Math.PI.toFloat() * k * n / FRAME_SIZE
                        real += sample * kotlin.math.cos(angle)
                        imag += sample * kotlin.math.sin(angle)
                    }
                    energy += real * real + imag * imag
                }
                // Log energy (RNNoise uses log-compressed features)
                bandEnergies[b] = (log10(energy / FRAME_SIZE + 1e-10f) + 4f).coerceIn(-4f, 4f)
            }
            // Last band = total energy
            var totalEnergy = 0.0f
            for (i in frameOffset until min(frameOffset + FRAME_SIZE, pcm.size)) {
                totalEnergy += pcm[i] * pcm[i]
            }
            bandEnergies[NUM_BANDS - 1] = (log10(totalEnergy / FRAME_SIZE + 1e-10f) + 4f).coerceIn(-4f, 4f)

            System.arraycopy(bandEnergies, 0, features, f * NUM_BANDS, NUM_BANDS)
        }
        return features
    }

    private fun applyGains(pcm: FloatArray, gains: FloatArray, numFrames: Int): FloatArray {
        val output = pcm.copyOf()
        // Conv1d with kernel_size=3 and 2 layers reduces frames by 4
        val outFrames = numFrames - 4
        val frameOffset = 2  // Center offset due to Conv1d

        // gains output: [1, outFrames, 32] flattened
        for (f in 0 until outFrames) {
            // Average gain across 32 bands
            var avgGain = 0.0f
            for (b in 0 until 32) {
                val idx = f * 32 + b
                avgGain += if (idx < gains.size) gains[idx] else 1f
            }
            avgGain /= 32f

            // Apply gain to the corresponding PCM frame
            val pcmFrame = (f + frameOffset) * FRAME_SIZE
            for (i in 0 until FRAME_SIZE) {
                val idx = pcmFrame + i
                if (idx < output.size) {
                    output[idx] = pcm[idx] * avgGain
                }
            }
        }
        return output
    }

    fun release() { sessionManager.release(modelId); isReady = false }
}
