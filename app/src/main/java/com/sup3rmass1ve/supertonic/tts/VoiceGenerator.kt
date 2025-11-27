package com.sup3rmass1ve.supertonic.tts

import android.content.Context
import com.sup3rmass1ve.supertonic.model.VoiceStyle
import com.sup3rmass1ve.supertonic.model.StyleData
import com.google.gson.Gson
import java.io.File

/**
 * Generate new voices by interpolating/mixing existing Supertonic voice embeddings
 */
class VoiceGenerator(private val context: Context) {
    private val voiceLoader = VoiceStyleLoader(context)
    
    /**
     * Interpolate between two voices
     * @param voice1Name First voice (e.g., "F1.json")
     * @param voice2Name Second voice (e.g., "F2.json")
     * @param weight Weight for voice1 (0.0 to 1.0). voice2 gets (1.0 - weight)
     * @param newName Name for the generated voice
     */
    fun interpolate(
        voice1Name: String,
        voice2Name: String,
        weight: Float = 0.5f,
        newName: String = "interpolated"
    ): VoiceStyle {
        val voice1 = voiceLoader.loadVoiceStyle(voice1Name)
        val voice2 = voiceLoader.loadVoiceStyle(voice2Name)
        
        val weight1 = weight.coerceIn(0f, 1f)
        val weight2 = 1f - weight1
        
        // Interpolate style_ttl
        val newStyleTtl = interpolateStyleData(voice1.styleTtl, voice2.styleTtl, weight1, weight2)
        
        // Interpolate style_dp
        val newStyleDp = interpolateStyleData(voice1.styleDp, voice2.styleDp, weight1, weight2)
        
        return VoiceStyle(
            name = newName,
            styleTtl = newStyleTtl,
            styleDp = newStyleDp
        )
    }
    
    // Helper functions
    
    private fun interpolateStyleData(
        style1: StyleData,
        style2: StyleData,
        weight1: Float,
        weight2: Float
    ): StyleData {
        require(style1.dims == style2.dims) { "Voice dimensions must match" }
        
        // Flatten, interpolate, then reshape
        val flat1 = flattenStyleData(style1)
        val flat2 = flattenStyleData(style2)
        
        val interpolated = FloatArray(flat1.size) { i ->
            flat1[i] * weight1 + flat2[i] * weight2
        }
        
        return unflattenStyleData(interpolated, style1.dims)
    }
    
    private fun flattenStyleData(style: StyleData): FloatArray {
        val result = mutableListOf<Float>()
        // Structure: List<List<List<Float>>> = [batch][frames][values]
        for (batch in style.data) {
            for (frame in batch) {
                result.addAll(frame)
            }
        }
        return result.toFloatArray()
    }
    
    private fun unflattenStyleData(flat: FloatArray, dims: List<Int>): StyleData {
        // dims is [batch, frames, values_per_frame]
        // Structure: List<List<List<Float>>> = [batch][frames][values]
        
        val batchSize = dims[0]
        val numFrames = dims[1]
        val valuesPerFrame = dims[2]
        
        val data = mutableListOf<List<List<Float>>>()
        var idx = 0
        
        for (b in 0 until batchSize) {
            val batch = mutableListOf<List<Float>>()
            for (f in 0 until numFrames) {
                val frame = mutableListOf<Float>()
                for (v in 0 until valuesPerFrame) {
                    frame.add(flat[idx++])
                }
                batch.add(frame)
            }
            data.add(batch)
        }
        
        return StyleData(dims = dims, data = data)
    }
    
    /**
     * Export a voice to JSON format
     * @param voice The voice to export
     * @param fileName Output filename (e.g., "MyVoice.json")
     * @return JSON string
     */
    fun exportVoiceToJson(voice: VoiceStyle): String {
        val voiceJson = mapOf(
            "style_ttl" to mapOf(
                "dims" to voice.styleTtl.dims,
                "data" to voice.styleTtl.data
            ),
            "style_dp" to mapOf(
                "dims" to voice.styleDp.dims,
                "data" to voice.styleDp.data
            )
        )
        
        return Gson().toJson(voiceJson)
    }
    
    /**
     * Save a voice to Downloads folder as JSON
     * @param voice The voice to save
     * @param fileName Output filename (e.g., "MyVoice.json")
     * @return File path where saved
     */
    fun saveVoiceToDownloads(voice: VoiceStyle, fileName: String): String {
        val json = exportVoiceToJson(voice)
        
        // Save to Downloads directory
        val downloadsDir = android.os.Environment.getExternalStoragePublicDirectory(
            android.os.Environment.DIRECTORY_DOWNLOADS
        )
        val file = File(downloadsDir, fileName)
        file.writeText(json)
        
        return file.absolutePath
    }
    
    // Preset voice combinations
    
    fun generatePresetVoices(): List<Pair<String, VoiceStyle>> {
        val presets = mutableListOf<Pair<String, VoiceStyle>>()
        
        try {
            // Female blends
            presets.add("F1_F2_Blend" to interpolate("F1.json", "F2.json", 0.5f, "F1_F2_Blend"))
            presets.add("F1_Soft" to interpolate("F1.json", "F2.json", 0.7f, "F1_Soft"))
            presets.add("F2_Soft" to interpolate("F1.json", "F2.json", 0.3f, "F2_Soft"))
            
            // Male blends
            presets.add("M1_M2_Blend" to interpolate("M1.json", "M2.json", 0.5f, "M1_M2_Blend"))
            presets.add("M1_Soft" to interpolate("M1.json", "M2.json", 0.7f, "M1_Soft"))
            presets.add("M2_Soft" to interpolate("M1.json", "M2.json", 0.3f, "M2_Soft"))
            
            // Cross-gender blends (androgynous)
            presets.add("Androgynous_1" to interpolate("F1.json", "M1.json", 0.5f, "Androgynous_1"))
            presets.add("Androgynous_2" to interpolate("F2.json", "M2.json", 0.5f, "Androgynous_2"))
            presets.add("Fem_Leaning" to interpolate("F1.json", "M1.json", 0.65f, "Fem_Leaning"))
            presets.add("Masc_Leaning" to interpolate("F1.json", "M1.json", 0.35f, "Masc_Leaning"))
            
        } catch (e: Exception) {
            android.util.Log.e("VoiceGenerator", "Error generating preset: ${e.message}")
        }
        
        return presets
    }
}
