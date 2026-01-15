package com.sup3rmass1ve.supertonic.tts

import android.content.Context
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.sup3rmass1ve.supertonic.model.VoiceStyle
import com.sup3rmass1ve.supertonic.model.StyleData

class VoiceStyleLoader(private val context: Context) {
    
    fun getAvailableVoiceStyles(): List<String> {
        return try {
            context.assets.list("voice_styles")?.filter { it.endsWith(".json") } ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    fun loadVoiceStyle(fileName: String): VoiceStyle {
        val json = context.assets.open("voice_styles/$fileName").bufferedReader().use { it.readText() }
        val jsonObject = JsonParser.parseString(json).asJsonObject
        
        // Parse style_ttl
        val styleTtlObj = jsonObject.getAsJsonObject("style_ttl")
        val ttlDims = Gson().fromJson(styleTtlObj.get("dims"), Array<Int>::class.java).toList()
        val ttlDataJson = styleTtlObj.getAsJsonArray("data")
        val ttlData = parseNestedArray(ttlDataJson)
        
        // Parse style_dp
        val styleDpObj = jsonObject.getAsJsonObject("style_dp")
        val dpDims = Gson().fromJson(styleDpObj.get("dims"), Array<Int>::class.java).toList()
        val dpDataJson = styleDpObj.getAsJsonArray("data")
        val dpData = parseNestedArray(dpDataJson)
        
        return VoiceStyle(
            name = fileName.removeSuffix(".json"),
            styleTtl = StyleData(dims = ttlDims, data = ttlData),
            styleDp = StyleData(dims = dpDims, data = dpData)
        )
    }
    
    private fun parseNestedArray(jsonArray: com.google.gson.JsonArray): List<List<List<Float>>> {
        val result = mutableListOf<List<List<Float>>>()
        for (i in 0 until jsonArray.size()) {
            val level2 = jsonArray[i].asJsonArray
            val level2List = mutableListOf<List<Float>>()
            for (j in 0 until level2.size()) {
                val level3 = level2[j].asJsonArray
                val level3List = mutableListOf<Float>()
                for (k in 0 until level3.size()) {
                    level3List.add(level3[k].asFloat)
                }
                level2List.add(level3List)
            }
            result.add(level2List)
        }
        return result
    }
}
