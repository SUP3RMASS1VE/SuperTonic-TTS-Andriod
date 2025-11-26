package com.sup3rmass1ve.supertonic.tts

import android.content.Context
import ai.onnxruntime.*
import com.google.gson.Gson
import com.sup3rmass1ve.supertonic.model.*
import java.nio.FloatBuffer
import java.nio.LongBuffer
import kotlin.math.ceil
import kotlin.random.Random

data class TTSResult(
    val audio: FloatArray,
    val seedUsed: Long
)

class SupertonicTTS(private val context: Context) {
    private val ortEnv = OrtEnvironment.getEnvironment()
    private val sessionOptions = OrtSession.SessionOptions()
    
    private lateinit var dpSession: OrtSession
    private lateinit var textEncSession: OrtSession
    private lateinit var vectorEstSession: OrtSession
    private lateinit var vocoderSession: OrtSession
    
    private lateinit var textProcessor: TextProcessor
    private lateinit var config: TTSConfig
    
    val sampleRate: Int get() = if (::config.isInitialized) config.ae.sample_rate else 44100
    
    fun initialize() {
        try {
            // Load config
            val configJson = context.assets.open("onnx/tts.json").bufferedReader().use { it.readText() }
            config = Gson().fromJson(configJson, TTSConfig::class.java)
            
            // Initialize text processor
            textProcessor = TextProcessor(context)
            
            // Configure session options for optimal Android performance
            sessionOptions.apply {
                try {
                    // Try to enable NNAPI for hardware acceleration (GPU/DSP/NPU)
                    addNnapi()
                } catch (e: Exception) {
                    // NNAPI not available, will use CPU
                }
                setOptimizationLevel(OrtSession.SessionOptions.OptLevel.ALL_OPT)
                setIntraOpNumThreads(Runtime.getRuntime().availableProcessors())
                setInterOpNumThreads(2)
            }
            
            // Load ONNX models
            dpSession = ortEnv.createSession(
                context.assets.open("onnx/duration_predictor.onnx").readBytes(),
                sessionOptions
            )
            textEncSession = ortEnv.createSession(
                context.assets.open("onnx/text_encoder.onnx").readBytes(),
                sessionOptions
            )
            vectorEstSession = ortEnv.createSession(
                context.assets.open("onnx/vector_estimator.onnx").readBytes(),
                sessionOptions
            )
            vocoderSession = ortEnv.createSession(
                context.assets.open("onnx/vocoder.onnx").readBytes(),
                sessionOptions
            )
        } catch (e: Exception) {
            throw Exception("Failed to initialize TTS: ${e.message}", e)
        }
    }
    
    fun generateSpeech(
        text: String,
        voiceStyle: VoiceStyle,
        speed: Float = 1.05f,
        totalSteps: Int = 5,
        seed: Long? = null
    ): TTSResult {
        // Generate or use provided seed
        val actualSeed = seed ?: System.currentTimeMillis()
        
        // Split text into chunks if needed
        val chunks = chunkText(text)
        val allAudio = mutableListOf<FloatArray>()
        
        for (chunk in chunks) {
            val audio = generateChunk(chunk, voiceStyle, speed, totalSteps, actualSeed)
            allAudio.add(audio)
            
            // Add silence between chunks
            if (chunk != chunks.last()) {
                val silenceSamples = (0.3f * sampleRate).toInt()
                allAudio.add(FloatArray(silenceSamples))
            }
        }
        
        // Concatenate all audio
        val totalLength = allAudio.sumOf { it.size }
        val result = FloatArray(totalLength)
        var offset = 0
        for (audio in allAudio) {
            audio.copyInto(result, offset)
            offset += audio.size
        }
        
        return TTSResult(result, actualSeed)
    }
    
    private fun generateChunk(
        text: String,
        voiceStyle: VoiceStyle,
        speed: Float,
        totalSteps: Int,
        seed: Long?
    ): FloatArray {
        // Process text
        val (textIds, textMask) = textProcessor.processText(text)
        val textLen = textIds[0].size
        
        // Prepare style tensors
        val styleTtl = voiceStyle.styleTtl.data[0].flatten().toFloatArray()
        val styleDp = voiceStyle.styleDp.data[0].flatten().toFloatArray()
        
        val styleTtlShape = longArrayOf(1, voiceStyle.styleTtl.dims[1].toLong(), voiceStyle.styleTtl.dims[2].toLong())
        val styleDpShape = longArrayOf(1, voiceStyle.styleDp.dims[1].toLong(), voiceStyle.styleDp.dims[2].toLong())
        
        // Create text mask tensor
        val textMaskTensor = FloatArray(textLen) { 1.0f }
        
        // Step 1: Duration Prediction
        val dpInputs = mapOf(
            "text_ids" to OnnxTensor.createTensor(ortEnv, LongBuffer.wrap(textIds[0]), longArrayOf(1, textLen.toLong())),
            "style_dp" to OnnxTensor.createTensor(ortEnv, FloatBuffer.wrap(styleDp), styleDpShape),
            "text_mask" to OnnxTensor.createTensor(ortEnv, FloatBuffer.wrap(textMaskTensor), longArrayOf(1, 1, textLen.toLong()))
        )
        
        val dpResult = dpSession.run(dpInputs)
        val durationArray = dpResult[0].value as FloatArray
        val duration = durationArray[0] / speed
        dpResult.close()
        dpInputs.values.forEach { it.close() }
        
        // Step 2: Text Encoding
        val textEncInputs = mapOf(
            "text_ids" to OnnxTensor.createTensor(ortEnv, LongBuffer.wrap(textIds[0]), longArrayOf(1, textLen.toLong())),
            "style_ttl" to OnnxTensor.createTensor(ortEnv, FloatBuffer.wrap(styleTtl), styleTtlShape),
            "text_mask" to OnnxTensor.createTensor(ortEnv, FloatBuffer.wrap(textMaskTensor), longArrayOf(1, 1, textLen.toLong()))
        )
        
        val textEncResult = textEncSession.run(textEncInputs)
        val textEmb = textEncResult[0].value as Array<Array<FloatArray>>
        textEncInputs.values.forEach { it.close() }
        
        // Step 3: Sample noisy latent
        val wavLen = (duration * sampleRate).toInt()
        val chunkSize = config.ae.base_chunk_size * config.ttl.chunk_compress_factor
        val latentLen = ceil(wavLen.toFloat() / chunkSize).toInt()
        val latentDim = config.ttl.latent_dim * config.ttl.chunk_compress_factor
        
        val random = if (seed != null) Random(seed) else Random.Default
        var noisyLatent = Array(1) { Array(latentDim) { FloatArray(latentLen) { random.nextFloat() * 2 - 1 } } }
        val latentMask = createLatentMask(wavLen, latentLen, latentDim)
        
        // Step 4: Iterative denoising
        val totalStepArray = FloatArray(1) { totalSteps.toFloat() }
        
        for (step in 0 until totalSteps) {
            val currentStepArray = FloatArray(1) { step.toFloat() }
            
            val flatNoisyLatent = noisyLatent[0].flatMap { it.toList() }.toFloatArray()
            val flatTextEmb = textEmb[0].flatMap { it.toList() }.toFloatArray()
            
            val vecEstInputs = mapOf(
                "noisy_latent" to OnnxTensor.createTensor(ortEnv, FloatBuffer.wrap(flatNoisyLatent), longArrayOf(1, latentDim.toLong(), latentLen.toLong())),
                "text_emb" to OnnxTensor.createTensor(ortEnv, FloatBuffer.wrap(flatTextEmb), longArrayOf(1, textEmb[0].size.toLong(), textEmb[0][0].size.toLong())),
                "style_ttl" to OnnxTensor.createTensor(ortEnv, FloatBuffer.wrap(styleTtl), styleTtlShape),
                "text_mask" to OnnxTensor.createTensor(ortEnv, FloatBuffer.wrap(textMaskTensor), longArrayOf(1, 1, textLen.toLong())),
                "latent_mask" to OnnxTensor.createTensor(ortEnv, FloatBuffer.wrap(latentMask), longArrayOf(1, 1, latentLen.toLong())),
                "current_step" to OnnxTensor.createTensor(ortEnv, FloatBuffer.wrap(currentStepArray), longArrayOf(1)),
                "total_step" to OnnxTensor.createTensor(ortEnv, FloatBuffer.wrap(totalStepArray), longArrayOf(1))
            )
            
            val vecEstResult = vectorEstSession.run(vecEstInputs)
            val updatedLatent = vecEstResult[0].value as Array<Array<FloatArray>>
            noisyLatent = updatedLatent
            
            vecEstResult.close()
            vecEstInputs.values.forEach { it.close() }
        }
        
        textEncResult.close()
        
        // Step 5: Vocoder
        val flatFinalLatent = noisyLatent[0].flatMap { it.toList() }.toFloatArray()
        val vocoderInputs = mapOf(
            "latent" to OnnxTensor.createTensor(ortEnv, FloatBuffer.wrap(flatFinalLatent), longArrayOf(1, latentDim.toLong(), latentLen.toLong()))
        )
        
        val vocoderResult = vocoderSession.run(vocoderInputs)
        val wav = (vocoderResult[0].value as Array<FloatArray>)[0]
        
        vocoderResult.close()
        vocoderInputs.values.forEach { it.close() }
        
        return wav
    }
    
    private fun createLatentMask(wavLen: Int, latentLen: Int, latentDim: Int): FloatArray {
        return FloatArray(latentLen) { 1.0f }
    }
    
    private fun chunkText(text: String, maxLen: Int = 300): List<String> {
        if (text.length <= maxLen) return listOf(text)
        
        val chunks = mutableListOf<String>()
        val sentences = text.split(Regex("(?<=[.!?])\\s+"))
        
        var currentChunk = ""
        for (sentence in sentences) {
            if (currentChunk.length + sentence.length <= maxLen) {
                currentChunk += (if (currentChunk.isEmpty()) "" else " ") + sentence
            } else {
                if (currentChunk.isNotEmpty()) chunks.add(currentChunk)
                currentChunk = sentence
            }
        }
        if (currentChunk.isNotEmpty()) chunks.add(currentChunk)
        
        return chunks
    }
    
    fun close() {
        dpSession.close()
        textEncSession.close()
        vectorEstSession.close()
        vocoderSession.close()
    }
}
