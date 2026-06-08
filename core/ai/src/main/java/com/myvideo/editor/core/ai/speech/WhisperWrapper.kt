package com.myvideo.editor.core.ai.speech

import com.myvideo.editor.core.ai.common.InferenceSessionManager
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.cos
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.PI
import kotlin.math.pow
import kotlin.math.sin

class WhisperWrapper(private val sessionManager: InferenceSessionManager) {
    private val encoderModelId = "whisper_encoder"
    private val decoderModelId = "whisper_decoder"
    var isReady = false; private set

    private var tokens = listOf<String>()

    data class TranscriptionResult(
        val text: String, val segments: List<Segment> = emptyList()
    )
    data class Segment(val startMs: Long, val endMs: Long, val text: String)

    fun init(modelDir: String): Boolean {
        val dir = File(modelDir)
        val encoderPath = File(dir, "whisper_encoder.onnx")
        val decoderPath = File(dir, "whisper_decoder.onnx")
        val tokensFile = File(dir, "whisper_tokens.txt")

        if (!encoderPath.exists() || !decoderPath.exists() || !tokensFile.exists()) return false

        val encoderLoaded = sessionManager.loadModel(encoderPath.absolutePath, encoderModelId)
        val decoderLoaded = sessionManager.loadModel(decoderPath.absolutePath, decoderModelId)
        if (!encoderLoaded || !decoderLoaded) return false

        loadTokens(tokensFile.absolutePath)
        isReady = true
        return isReady
    }

    fun transcribe(audioPath: String): TranscriptionResult? {
        if (!isReady) return null
        return try {
            val audioData = readAudioFile(audioPath)
            if (audioData.isEmpty()) return null
            val melSpec = computeMelSpectrogram(audioData)
            val encoderOutput = runEncoder(melSpec)
            val tokenIds = decodeAutoregressive(encoderOutput)
            parseTranscription(tokenIds)
        } catch (e: Exception) { null }
    }

    fun transcribeFromPcm(pcm: FloatArray, sampleRate: Int): TranscriptionResult? {
        if (!isReady) return null
        return try {
            val audio16k = if (sampleRate != 16000) resample(pcm, sampleRate, 16000) else pcm
            val melSpec = computeMelSpectrogram(audio16k)
            val encoderOutput = runEncoder(melSpec)
            val tokenIds = decodeAutoregressive(encoderOutput)
            parseTranscription(tokenIds)
        } catch (e: Exception) { null }
    }

    // ── Audio I/O ────────────────────────────────────────────────────────────

    private fun readAudioFile(path: String): FloatArray {
        val file = File(path)
        if (!file.exists()) return FloatArray(0)
        val bytes = file.readBytes()
        val buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)

        // Check RIFF header
        val riff = ByteArray(4); buffer.get(riff)
        if (String(riff) != "RIFF") return FloatArray(0)
        buffer.getInt() // file size - 8
        val wave = ByteArray(4); buffer.get(wave)
        if (String(wave) != "WAVE") return FloatArray(0)

        var channels = 1
        var sampleRate = 16000
        var bitsPerSample = 16
        var audioData: FloatArray? = null

        while (buffer.remaining() > 8) {
            val chunkId = ByteArray(4); buffer.get(chunkId)
            val chunkSize = buffer.int
            val chunkIdStr = String(chunkId)

            when (chunkIdStr) {
                "fmt " -> {
                    buffer.short // audio format (1 = PCM)
                    channels = buffer.short.toInt()
                    sampleRate = buffer.int
                    buffer.int // byte rate
                    buffer.short // block align
                    bitsPerSample = buffer.short.toInt()
                    if (chunkSize > 16) {
                        buffer.position(buffer.position() + chunkSize - 16)
                    }
                }
                "data" -> {
                    val bytesPerSample = bitsPerSample / 8
                    val numSamples = chunkSize / bytesPerSample
                    val samples = FloatArray(numSamples)
                    when (bitsPerSample) {
                        16 -> {
                            for (i in 0 until numSamples) {
                                if (buffer.remaining() >= 2) {
                                    samples[i] = buffer.short.toFloat() / 32768f
                                }
                            }
                        }
                        8 -> {
                            for (i in 0 until numSamples) {
                                if (buffer.hasRemaining()) {
                                    samples[i] = (buffer.get().toInt() and 0xFF - 128) / 128f
                                }
                            }
                        }
                        32 -> {
                            for (i in 0 until numSamples) {
                                if (buffer.remaining() >= 4) {
                                    samples[i] = buffer.float
                                }
                            }
                        }
                        else -> {
                            buffer.position(buffer.position() + chunkSize)
                        }
                    }
                    audioData = samples
                    break
                }
                else -> {
                    val skipBytes = chunkSize + (chunkSize % 2) // word-align
                    if (buffer.remaining() >= skipBytes) {
                        buffer.position(buffer.position() + skipBytes)
                    } else break
                }
            }
        }

        var result = audioData ?: return FloatArray(0)

        // Mix to mono if stereo
        if (channels > 1) {
            val monoLen = result.size / channels
            result = FloatArray(monoLen) { i ->
                var sum = 0f
                for (c in 0 until channels) sum += audioData!![i * channels + c]
                sum / channels
            }
        }

        // Resample to 16kHz
        if (sampleRate != 16000) {
            result = resample(result, sampleRate, 16000)
        }

        return result
    }

    private fun resample(audio: FloatArray, fromRate: Int, toRate: Int): FloatArray {
        if (fromRate == toRate) return audio
        val ratio = fromRate.toDouble() / toRate
        val newLength = (audio.size / ratio).toInt()
        if (newLength <= 0) return FloatArray(0)
        val result = FloatArray(newLength)
        for (i in 0 until newLength) {
            val srcIdx = i * ratio
            val idx0 = srcIdx.toInt()
            val idx1 = (idx0 + 1).coerceAtMost(audio.size - 1)
            val frac = (srcIdx - idx0).toFloat()
            result[i] = audio[idx0] * (1f - frac) + audio[idx1] * frac
        }
        return result
    }

    // ── Mel Spectrogram ──────────────────────────────────────────────────────

    private fun computeMelSpectrogram(audio: FloatArray): FloatArray {
        val sampleRate = 16000
        val nFft = 512       // zero-pad from 400 to next power of 2
        val winLen = 400
        val hopLen = 160
        val nMels = 80
        val nFreqs = nFft / 2 + 1  // 257
        val targetSamples = 30 * sampleRate  // 480000

        // Pad or trim to 30 seconds
        val padded = if (audio.size >= targetSamples) {
            audio.copyOf(targetSamples)
        } else {
            audio.copyOf(targetSamples)
        }

        // Center-pad by n_fft//2 for center=True STFT
        val centerPad = nFft / 2
        val centered = FloatArray(padded.size + 2 * centerPad)
        System.arraycopy(padded, 0, centered, centerPad, padded.size)

        // Number of frames: match Whisper's 3000 frames for 30s
        val nFrames = 3000

        // Hann window
        val window = FloatArray(winLen) { i ->
            0.5f * (1f - cos(2f * PI.toFloat() * i / (winLen - 1)))
        }

        // Mel filterbank
        val melFB = createMelFilterbank(nMels, nFreqs, sampleRate)

        // Compute STFT → mel spectrogram
        val melSpec = FloatArray(nMels * nFrames)
        var maxMel = -Float.MAX_VALUE

        for (frame in 0 until nFrames) {
            val start = frame * hopLen

            // Apply window and zero-pad to nFft
            val frameData = FloatArray(nFft)
            for (i in 0 until winLen) {
                val idx = start + i
                if (idx < centered.size) {
                    frameData[i] = centered[idx] * window[i]
                }
            }

            // Compute FFT
            val re = frameData.copyOf()
            val im = FloatArray(nFft)
            fft(re, im)

            // Power spectrum for first nFreqs bins
            val power = FloatArray(nFreqs)
            for (k in 0 until nFreqs) {
                power[k] = re[k] * re[k] + im[k] * im[k]
            }

            // Apply mel filterbank
            for (m in 0 until nMels) {
                var melEnergy = 0f
                for (k in 0 until nFreqs) {
                    melEnergy += melFB[m * nFreqs + k] * power[k]
                }
                // log10 with floor
                val logMel = log10(max(melEnergy, 1e-10f))
                melSpec[m * nFrames + frame] = logMel
                if (logMel > maxMel) maxMel = logMel
            }
        }

        // Clamp to max - 8.0, then scale: (x + 4.0) / 4.0
        val clampFloor = maxMel - 8f
        for (i in melSpec.indices) {
            melSpec[i] = max(melSpec[i], clampFloor)
            melSpec[i] = (melSpec[i] + 4f) / 4f
        }

        return melSpec
    }

    private fun createMelFilterbank(nMels: Int, nFreqs: Int, sampleRate: Int): FloatArray {
        val fMax = sampleRate / 2f
        val melMin = 0f
        val melMax = 2595f * log10(1f + fMax / 700f)

        // nMels + 2 points evenly spaced in mel scale
        val melPoints = FloatArray(nMels + 2) { i ->
            melMin + (melMax - melMin) * i / (nMels + 1)
        }

        // Convert mel to frequency
        val freqPoints = FloatArray(nMels + 2) { i ->
            700f * (10f.pow(melPoints[i] / 2595f) - 1f)
        }

        // Convert frequency to FFT bin index
        val binPoints = FloatArray(nMels + 2) { i ->
            freqPoints[i] * (nFreqs - 1) * 2f / sampleRate
        }

        // Create triangular filterbank
        val filterbank = FloatArray(nMels * nFreqs)
        for (m in 0 until nMels) {
            val fLeft = binPoints[m]
            val fCenter = binPoints[m + 1]
            val fRight = binPoints[m + 2]

            for (k in 0 until nFreqs) {
                val weight = when {
                    k.toFloat() < fLeft -> 0f
                    k.toFloat() < fCenter -> {
                        if (fCenter == fLeft) 0f
                        else (k.toFloat() - fLeft) / (fCenter - fLeft)
                    }
                    k.toFloat() < fRight -> {
                        if (fRight == fCenter) 0f
                        else (fRight - k.toFloat()) / (fRight - fCenter)
                    }
                    else -> 0f
                }
                filterbank[m * nFreqs + k] = weight
            }
        }
        return filterbank
    }

    // ── FFT ──────────────────────────────────────────────────────────────────

    private fun fft(re: FloatArray, im: FloatArray) {
        val n = re.size
        require(n > 0 && (n and (n - 1)) == 0) { "FFT size must be a power of 2" }

        // Bit-reversal permutation
        var j = 0
        for (i in 1 until n) {
            var bit = n shr 1
            while (j and bit != 0) { j = j xor bit; bit = bit shr 1 }
            j = j xor bit
            if (i < j) {
                re[i] = re[j].also { re[j] = re[i] }
                im[i] = im[j].also { im[j] = im[i] }
            }
        }

        // Cooley-Tukey iterative FFT
        var len = 2
        while (len <= n) {
            val halfLen = len / 2
            val angle = -2.0 * PI / len
            for (i in 0 until n step len) {
                for (k in 0 until halfLen) {
                    val wAngle = angle * k
                    val wRe = cos(wAngle).toFloat()
                    val wIm = sin(wAngle).toFloat()
                    val evenIdx = i + k
                    val oddIdx = i + k + halfLen
                    val tRe = re[oddIdx] * wRe - im[oddIdx] * wIm
                    val tIm = re[oddIdx] * wIm + im[oddIdx] * wRe
                    re[oddIdx] = re[evenIdx] - tRe
                    im[oddIdx] = im[evenIdx] - tIm
                    re[evenIdx] += tRe
                    im[evenIdx] += tIm
                }
            }
            len *= 2
        }
    }

    // ── ONNX Encoder / Decoder ───────────────────────────────────────────────

    private fun runEncoder(melSpec: FloatArray): FloatArray {
        return sessionManager.run(
            encoderModelId, "mel", melSpec, longArrayOf(1, 80, 3000)
        ) ?: throw IllegalStateException("Whisper encoder failed")
    }

    private fun decodeAutoregressive(encoderOutput: FloatArray): List<Int> {
        val encoderLen = 1500
        val hiddenSize = encoderOutput.size / encoderLen

        // Initial tokens: SOT, English, Transcribe, NoTimestamps
        val sotToken = 50258
        val enToken = 50259
        val transcribeToken = 50359
        val noTimestampsToken = 50363
        val eosToken = 50257
        val maxTokens = 448

        val currentTokens = mutableListOf(sotToken, enToken, transcribeToken, noTimestampsToken)
        var vocabSize = 0

        for (step in 0 until maxTokens) {
            val decoderInputIds = LongArray(currentTokens.size) { i -> currentTokens[i].toLong() }

            val floatInputs = mapOf(
                "encoder_hidden_states" to Pair(
                    encoderOutput,
                    longArrayOf(1, encoderLen.toLong(), hiddenSize.toLong())
                )
            )
            val longInputs = mapOf(
                "decoder_input_ids" to Pair(
                    decoderInputIds,
                    longArrayOf(1, currentTokens.size.toLong())
                )
            )

            val logits = sessionManager.runMulti(decoderModelId, floatInputs, longInputs)
                ?: break

            // Infer vocab size from first run
            if (vocabSize == 0) {
                vocabSize = logits.size / currentTokens.size
                if (vocabSize <= 0) break
            }

            // Get logits for the last token position
            val lastTokenStart = (currentTokens.size - 1) * vocabSize
            val lastTokenEnd = lastTokenStart + vocabSize
            if (lastTokenEnd > logits.size) break

            // Argmax
            var maxIdx = 0
            var maxVal = logits[lastTokenStart]
            for (i in 1 until vocabSize) {
                if (logits[lastTokenStart + i] > maxVal) {
                    maxVal = logits[lastTokenStart + i]
                    maxIdx = i
                }
            }

            if (maxIdx == eosToken) break
            currentTokens.add(maxIdx)
        }

        // Remove initial special tokens (SOT, EN, Transcribe, NoTimestamps)
        return if (currentTokens.size > 4) currentTokens.subList(4, currentTokens.size) else emptyList()
    }

    // ── Token Decoding ───────────────────────────────────────────────────────

    private fun loadTokens(path: String) {
        tokens = File(path).readLines()
    }

    private fun parseTranscription(tokenIds: List<Int>): TranscriptionResult {
        val sb = StringBuilder()
        for (id in tokenIds) {
            if (id < 0 || id >= tokens.size) continue
            val token = tokens[id]
            // Skip special tokens
            if (token.startsWith("<|") && token.endsWith("|>")) continue
            // Handle byte tokens like <0x0A>
            if (token.startsWith("<0x") && token.length == 6 && token.endsWith(">")) {
                val hexStr = token.substring(3, 5)
                val byteVal = hexStr.toIntOrNull(16)
                if (byteVal != null) {
                    sb.append(byteVal.toChar())
                }
                continue
            }
            // BPE token: Ġ represents space, Ċ represents newline
            sb.append(token.replace("Ġ", " ").replace("Ċ", "\n"))
        }
        val text = sb.toString().trim()
        return TranscriptionResult(text = text)
    }

    fun release() {
        sessionManager.release(encoderModelId)
        sessionManager.release(decoderModelId)
        isReady = false
    }
}
