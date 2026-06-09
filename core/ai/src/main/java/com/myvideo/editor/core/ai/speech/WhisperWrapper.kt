package com.myvideo.editor.core.ai.speech

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import com.myvideo.editor.core.ai.common.InferenceSessionManager
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.nio.LongBuffer

class WhisperWrapper(private val sessionManager: InferenceSessionManager) {
    private var encoderModelId = "whisper_encoder"
    private var decoderModelId = "whisper_decoder"
    private var env: OrtEnvironment? = null
    private var decoderSession: OrtSession? = null
    private var tokens: List<String> = emptyList()
    var isReady = false; private set

    data class TranscriptionResult(
        val text: String, val segments: List<Segment> = emptyList()
    )
    data class Segment(val startMs: Long, val endMs: Long, val text: String)

    fun init(encoderPath: String, decoderPath: String, tokensPath: String): Boolean {
        env = OrtEnvironment.getEnvironment()
        val encOk = sessionManager.loadModel(encoderPath, encoderModelId)
        val decOk = loadDecoder(decoderPath)
        tokens = loadTokens(tokensPath)
        isReady = encOk && decOk && tokens.isNotEmpty()
        return isReady
    }

    fun init(modelPath: String): Boolean {
        // Single model init - try to find encoder/decoder in same directory
        val dir = File(modelPath).parentFile ?: return false
        val encPath = File(dir, "whisper_encoder.onnx").absolutePath
        val decPath = File(dir, "whisper_decoder.onnx").absolutePath
        val tokPath = File(dir, "whisper_tokens.txt").absolutePath
        return init(encPath, decPath, tokPath)
    }

    fun initFromSession(encoderModelId: String = "whisper_encoder", decoderModelId: String = "whisper_decoder", tokensPath: String, context: android.content.Context): Boolean {
        this.encoderModelId = encoderModelId
        this.decoderModelId = decoderModelId
        // Load tokens from assets
        this.tokens = loadTokensFromAssets(tokensPath, context)
        // Decoder session needs to be loaded separately (it uses int64 inputs)
        // For now, we'll load it from the session manager if available
        isReady = sessionManager.isLoaded(encoderModelId)
        return isReady
    }

    private fun loadTokensFromAssets(path: String, context: android.content.Context): List<String> {
        return try {
            context.assets.open(path).bufferedReader().use { it.lineSequence().toList() }
        } catch (e: Exception) { emptyList() }
    }

    private fun loadDecoder(path: String): Boolean {
        return try {
            val e = env ?: return false
            val opts = OrtSession.SessionOptions()
            opts.setOptimizationLevel(OrtSession.SessionOptions.OptLevel.ALL_OPT)
            decoderSession = e.createSession(path, opts)
            true
        } catch (ex: Exception) { false }
    }

    private fun loadTokens(path: String): List<String> {
        return try {
            BufferedReader(FileReader(path)).use { reader ->
                reader.lineSequence().toList()
            }
        } catch (e: Exception) { emptyList() }
    }

    fun transcribe(audioPath: String): TranscriptionResult? {
        if (!isReady) return null
        return try {
            val audioData = readAudioFile(audioPath)
            if (audioData.isEmpty()) return null
            transcribeFromPcm(audioData, 16000)
        } catch (e: Exception) { null }
    }

    fun transcribeFromPcm(pcm: FloatArray, sampleRate: Int = 16000): TranscriptionResult? {
        if (!isReady) return null
        return try {
            // Step 1: Compute mel spectrogram [1, 80, 3000]
            val melSpec = computeMelSpectrogram(pcm, sampleRate)

            // Step 2: Run encoder
            val encoderOutput = sessionManager.run(
                encoderModelId, "mel_spectrogram", melSpec, longArrayOf(1, 80, 3000)
            ) ?: return null

            // Step 3: Greedy decode
            val text = greedyDecode(encoderOutput)
            TranscriptionResult(text = text, segments = listOf(Segment(0, pcm.size.toLong() * 1000 / sampleRate, text)))
        } catch (e: Exception) { null }
    }

    private fun greedyDecode(encoderOutput: FloatArray): String {
        val e = env ?: return ""
        val session = decoderSession ?: return ""
        val sb = StringBuilder()

        // Start with SOT token (50258 for Whisper) and language token (50259 for English, 50359 for auto)
        val sotToken = 50258L
        val langToken = 50359L  // auto-detect language
        val noSpeechToken = 50362L
        val eotToken = 50257L

        val currentTokens = mutableListOf(sotToken, langToken, noSpeechToken)
        val maxSteps = 448  // max sequence length for Whisper tiny

        try {
            for (step in 0 until maxSteps) {
                val tokenArr = LongArray(currentTokens.size) { currentTokens[it] }
                val encoderShape = longArrayOf(1, 1500, 384)

                val tokenTensor = OnnxTensor.createTensor(e, LongBuffer.wrap(tokenArr), longArrayOf(1, tokenArr.size.toLong()))
                val encoderTensor = OnnxTensor.createTensor(e, java.nio.FloatBuffer.wrap(encoderOutput), encoderShape)

                val result = session.run(mapOf(
                    "tokens" to tokenTensor,
                    "encoder_hidden_states" to encoderTensor
                ))

                val logits = (result[0].value as Array<FloatArray>)[0]
                tokenTensor.close()
                encoderTensor.close()
                result.close()

                // Get the predicted token from the last position
                val vocabSize = tokens.size.coerceAtLeast(1)
                val lastPosLogits = logits.copyOfRange((logits.size - vocabSize).coerceAtLeast(0), logits.size)
                val nextToken = argMax(lastPosLogits).toLong() + (vocabSize - lastPosLogits.size)

                if (nextToken == eotToken) break

                currentTokens.add(nextToken)
                if (nextToken in tokens.indices) {
                    val word = tokens[nextToken.toInt()]
                    if (word.length > 1 && word.startsWith(" ") && sb.isNotEmpty()) {
                        sb.append(" ")
                        sb.append(word.substring(1))
                    } else {
                        sb.append(word.replace("<|", "").replace("|>", ""))
                    }
                }
            }
        } catch (ex: Exception) {
            // Decoder error, return partial result
        }

        return sb.toString().trim()
    }

    private fun argMax(arr: FloatArray): Int {
        var maxIdx = 0
        var maxVal = arr[0]
        for (i in arr.indices) {
            if (arr[i] > maxVal) { maxVal = arr[i]; maxIdx = i }
        }
        return maxIdx
    }

    private fun readAudioFile(path: String): FloatArray {
        // Read WAV file or raw PCM
        return try {
            val file = File(path)
            val bytes = file.readBytes()
            // Skip WAV header (44 bytes) if present
            val offset = if (bytes.size > 44 && String(bytes, 0, 4) == "RIFF") 44 else 0
            val numSamples = (bytes.size - offset) / 2
            val pcm = FloatArray(numSamples)
            for (i in 0 until numSamples) {
                val sample = ((bytes[offset + i * 2 + 1].toInt() shl 8) or (bytes[offset + i * 2].toInt() and 0xFF))
                pcm[i] = sample.toFloat() / 32768f
            }
            pcm
        } catch (e: Exception) { FloatArray(0) }
    }

    private fun computeMelSpectrogram(audio: FloatArray, sampleRate: Int = 16000): FloatArray {
        // Whisper expects 80-band mel spectrogram, 3000 time frames (30s at 100fps)
        val numMelBands = 80
        val targetFrames = 3000

        // Resample to 16kHz if needed
        val audio16k = if (sampleRate != 16000) {
            val ratio = 16000.0 / sampleRate
            val newLen = (audio.size * ratio).toInt()
            FloatArray(newLen) { i -> audio[(i / ratio).toInt().coerceIn(audio.indices)] }
        } else audio

        // Simple mel spectrogram approximation using FFT
        val fftSize = 400   // 25ms at 16kHz
        val hopSize = 160    // 10ms at 16kHz
        val numFrames = minOf((audio16k.size - fftSize) / hopSize + 1, targetFrames)

        val melSpec = FloatArray(numMelBands * targetFrames)

        // Mel filterbank center frequencies (Hz) for 80 bands, 0-8000Hz
        val melLow = 0.0
        val melHigh = hzToMel(8000.0)
        val melCenters = DoubleArray(numMelBands + 2) { i -> melToHz(melLow + (melHigh - melLow) * i / (numMelBands + 1)) }
        val fftBins = DoubleArray(fftSize / 2 + 1) { i -> i * sampleRate.toDouble() / fftSize }

        for (frame in 0 until numFrames) {
            val offset = frame * hopSize
            // Apply Hann window and compute power spectrum
            val powerSpectrum = DoubleArray(fftSize / 2 + 1)
            for (k in 0..fftSize / 2) {
                var real = 0.0; var imag = 0.0
                for (n in 0 until fftSize) {
                    val idx = offset + n
                    val sample = if (idx < audio16k.size) audio16k[idx].toDouble() else 0.0
                    val windowed = sample * (0.5 - 0.5 * Math.cos(2.0 * Math.PI * n / fftSize))
                    val angle = -2.0 * Math.PI * k * n / fftSize
                    real += windowed * Math.cos(angle)
                    imag += windowed * Math.sin(angle)
                }
                powerSpectrum[k] = (real * real + imag * imag) / fftSize
            }

            // Apply mel filterbank
            for (m in 0 until numMelBands) {
                var melEnergy = 0.0
                for (k in 0..fftSize / 2) {
                    if (fftBins[k] >= melCenters[m] && fftBins[k] <= melCenters[m + 2]) {
                        val weight = if (fftBins[k] <= melCenters[m + 1]) {
                            (fftBins[k] - melCenters[m]) / (melCenters[m + 1] - melCenters[m])
                        } else {
                            (melCenters[m + 2] - fftBins[k]) / (melCenters[m + 2] - melCenters[m + 1])
                        }
                        melEnergy += powerSpectrum[k] * weight
                    }
                }
                // Log mel spectrogram (Whisper uses log-mel)
                melSpec[m * targetFrames + frame] = (Math.log(melEnergy + 1e-10) / Math.log(10.0) + 4.0).coerceIn(-4.0, 4.0).toFloat()
            }
        }

        return melSpec
    }

    private fun hzToMel(hz: Double): Double = 2595.0 * Math.log10(1.0 + hz / 700.0)
    private fun melToHz(mel: Double): Double = 700.0 * (Math.pow(10.0, mel / 2595.0) - 1.0)

    fun release() {
        sessionManager.release(encoderModelId)
        decoderSession?.close()
        decoderSession = null
        isReady = false
    }
}
