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

    fun processText(text: String, language: String = "en"): Pair<Array<LongArray>, Array<FloatArray>> {
        val preprocessedText = preprocessText(text, language)
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
    
    private fun preprocessText(text: String, language: String): String {
        var processed = text
        
        // Normalize Unicode
        processed = Normalizer.normalize(processed, Normalizer.Form.NFKD)
        
        // Remove emojis (wide Unicode range)
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
        // FIXME: this should be fixed for non-English languages
        processed = processed.replace(Regex("[\u0302\u0303\u0304\u0305\u0306\u0307\u0308\u030A\u030B\u030C\u0327\u0328\u0329\u032A\u032B\u032C\u032D\u032E\u032F]"), "")
        
        // Remove special symbols
        processed = processed.replace(Regex("[♥☆♡©\\\\]"), "")
        
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
        
        // Add language tags for v2
        val validLanguages = listOf("en", "ko", "es", "pt", "fr")
        val lang = if (language in validLanguages) language else "en"
        processed = "<$lang>$processed</$lang>"
        
        return processed
    }

    fun createMask(length: Int, maxLen: Int): FloatArray {
        return FloatArray(maxLen) { if (it < length) 1.0f else 0.0f }
    }
}
