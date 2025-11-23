package com.sup3rmass1ve.supertonic.tts

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.Normalizer

class TextProcessor(context: Context) {
    private val unicodeIndexer: IntArray

    init {
        val json = context.assets.open("onnx/unicode_indexer.json").bufferedReader().use { it.readText() }
        unicodeIndexer = Gson().fromJson(json, IntArray::class.java)
    }

    fun processText(text: String): Pair<Array<LongArray>, Array<FloatArray>> {
        val preprocessedText = preprocessText(text)
        val normalizedText = Normalizer.normalize(preprocessedText, Normalizer.Form.NFKD)
        val unicodeValues = normalizedText.map { it.code }
        
        val textIds = LongArray(unicodeValues.size) { i ->
            val unicodeCode = unicodeValues[i]
            if (unicodeCode < unicodeIndexer.size && unicodeIndexer[unicodeCode] >= 0) {
                unicodeIndexer[unicodeCode].toLong()
            } else {
                0L // Unknown character
            }
        }
        
        val textMask = FloatArray(textIds.size) { 1.0f }
        
        return Pair(arrayOf(textIds), arrayOf(textMask))
    }
    
    private fun preprocessText(text: String): String {
        var processed = text
        
        // Remove emojis
        processed = processed.replace(Regex("[\uD83C-\uDBFF\uDC00-\uDFFF]+"), "")
        
        // Replace various dashes and symbols (normalize apostrophes to standard ')
        val replacements = mapOf(
            "–" to "-", "‑" to "-", "—" to "-",
            "¯" to " ", "_" to " ",
            """ to "\"", """ to "\"",
            "'" to "'", "'" to "'", "´" to "'", "`" to "'",
            "[" to " ", "]" to " ", "|" to " ", "/" to " ",
            "#" to " ", "→" to " ", "←" to " "
        )
        replacements.forEach { (old, new) ->
            processed = processed.replace(old, new)
        }
        
        // Remove combining diacritics
        processed = processed.replace(Regex("[\u0302\u0303\u0304\u0305\u0306\u0307\u0308\u030A\u030B\u030C\u0327\u0328\u0329\u032A\u032B\u032C\u032D\u032E\u032F]"), "")
        
        // Remove special symbols
        processed = processed.replace(Regex("[♥☆♡©\\\\]"), "")
        
        // Replace apostrophes in contractions with space for better pronunciation
        // This makes "I've" → "I ve", "don't" → "don t", etc.
        processed = processed.replace(Regex("(\\w)'(\\w)"), "$1 $2")
        
        // Replace known expressions
        processed = processed.replace("@", " at ")
        processed = processed.replace("e.g.,", "for example, ")
        processed = processed.replace("i.e.,", "that is, ")
        
        // Fix spacing around punctuation
        processed = processed.replace(Regex(" ,"), ",")
        processed = processed.replace(Regex(" \\."), ".")
        processed = processed.replace(Regex(" !"), "!")
        processed = processed.replace(Regex(" \\?"), "?")
        processed = processed.replace(Regex(" ;"), ";")
        processed = processed.replace(Regex(" :"), ":")
        processed = processed.replace(Regex(" '"), "'")
        
        // Remove duplicate quotes
        while (processed.contains("\"\"")) processed = processed.replace("\"\"", "\"")
        while (processed.contains("''")) processed = processed.replace("''", "'")
        while (processed.contains("``")) processed = processed.replace("``", "`")
        
        // Remove extra spaces
        processed = processed.replace(Regex("\\s+"), " ").trim()
        
        // Add period if text doesn't end with punctuation
        if (!processed.matches(Regex(".*[.!?;:,'\"')\\]}…。」』】〉》›»]$"))) {
            processed += "."
        }
        
        return processed
    }

    fun createMask(length: Int, maxLen: Int): FloatArray {
        return FloatArray(maxLen) { if (it < length) 1.0f else 0.0f }
    }
}
