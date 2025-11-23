package com.sup3rmass1ve.supertonic.audio

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

class AudioSaver(private val context: Context) {
    
    fun saveAudioToDownloads(audioData: FloatArray, sampleRate: Int, fileName: String): Result<String> {
        return try {
            val wavData = convertToWav(audioData, sampleRate)
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                saveToMediaStore(wavData, fileName)
            } else {
                saveToLegacyStorage(wavData, fileName)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun saveToMediaStore(wavData: ByteArray, fileName: String): Result<String> {
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "audio/wav")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
        }
        
        val uri = context.contentResolver.insert(
            MediaStore.Downloads.EXTERNAL_CONTENT_URI,
            contentValues
        ) ?: return Result.failure(Exception("Failed to create file"))
        
        context.contentResolver.openOutputStream(uri)?.use { outputStream ->
            outputStream.write(wavData)
        } ?: return Result.failure(Exception("Failed to open output stream"))
        
        return Result.success("Saved to Downloads/$fileName")
    }
    
    private fun saveToLegacyStorage(wavData: ByteArray, fileName: String): Result<String> {
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val file = File(downloadsDir, fileName)
        
        FileOutputStream(file).use { outputStream ->
            outputStream.write(wavData)
        }
        
        return Result.success("Saved to ${file.absolutePath}")
    }
    
    private fun convertToWav(audioData: FloatArray, sampleRate: Int): ByteArray {
        val channels = 1
        val bitsPerSample = 16
        val byteRate = sampleRate * channels * bitsPerSample / 8
        val blockAlign = channels * bitsPerSample / 8
        val dataSize = audioData.size * 2 // 16-bit = 2 bytes per sample
        
        val buffer = ByteBuffer.allocate(44 + dataSize).order(ByteOrder.LITTLE_ENDIAN)
        
        // WAV header
        buffer.put("RIFF".toByteArray())
        buffer.putInt(36 + dataSize)
        buffer.put("WAVE".toByteArray())
        
        // fmt chunk
        buffer.put("fmt ".toByteArray())
        buffer.putInt(16) // fmt chunk size
        buffer.putShort(1) // audio format (PCM)
        buffer.putShort(channels.toShort())
        buffer.putInt(sampleRate)
        buffer.putInt(byteRate)
        buffer.putShort(blockAlign.toShort())
        buffer.putShort(bitsPerSample.toShort())
        
        // data chunk
        buffer.put("data".toByteArray())
        buffer.putInt(dataSize)
        
        // Convert float samples to 16-bit PCM
        for (sample in audioData) {
            val pcmSample = (sample * 32767f).toInt().coerceIn(-32768, 32767).toShort()
            buffer.putShort(pcmSample)
        }
        
        return buffer.array()
    }
}
