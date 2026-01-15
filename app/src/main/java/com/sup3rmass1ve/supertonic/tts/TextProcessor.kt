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
        
        // Remove emojis (comprehensive Unicode range matching Python)
        val emojiPattern = Regex(
            "[\uD83D\uDE00-\uD83D\uDE4F" +  // emoticons
            "\uD83C\uDF00-\uD83D\uDDFF" +   // symbols & pictographs
            "\uD83D\uDE80-\uD83D\uDEFF" +   // transport & map symbols
            "\uD83D\uDC00-\uD83D\uDDFF" +   // animals & nature
            "\uD83C\uDF00-\uD83C\uDFFF" +   // food & drink
            "\u2600-\u26FF" +                // misc symbols
            "\u2700-\u27BF" +                // dingbats
            "\uD83C\uDDE6-\uD83C\uDDFF]+"   // flags
        )
        processed = emojiPattern.replace(processed, "")
        
        // Replace various dashes and symbols
        val replacements = mapOf(
            "–" to "-",
            "‑" to "-",
            "—" to "-",
            "_" to " ",
            "\u201c" to "\"",  // left double quote "
            "\u201d" to "\"",  // right double quote "
            "\u2018" to "'",   // left single quote '
            "\u2019" to "'",   // right single quote '
            "´" to "'",
            "`" to "'",
            "[" to " ",
            "]" to " ",
            "|" to " ",
            "/" to " ",
            "#" to " ",
            "→" to " ",
            "←" to " "
        )
        replacements.forEach { (old, new) ->
            processed = processed.replace(old, new)
        }
        
        // Remove special symbols
        processed = processed.replace(Regex("[♥☆♡©\\\\]"), "")
        
        // Replace known expressions
        val expressionReplacements = mapOf(
            "@" to " at ",
            "e.g.," to "for example, ",
            "i.e.," to "that is, "
        )
        expressionReplacements.forEach { (old, new) ->
            processed = processed.replace(old, new)
        }
        
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
        
        // If text doesn't end with punctuation, quotes, or closing brackets, add a period
        if (!processed.matches(Regex(".*[.!?;:,'\"'\\)\\]}…。」』】〉》›»]$"))) {
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
