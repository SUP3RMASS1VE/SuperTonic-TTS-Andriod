package com.sup3rmass1ve.supertonic.audio

import android.content.ContentValues
import android.content.Context
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaMuxer
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

class AudioSaver(private val context: Context) {
    
    fun saveAudioToDownloads(
        audioData: FloatArray, 
        sampleRate: Int, 
        fileName: String,
        format: AudioFormat = AudioFormat.WAV
    ): Result<String> {
        return try {
            val audioBytes = when (format) {
                AudioFormat.WAV -> convertToWav(audioData, sampleRate)
                AudioFormat.M4A -> convertToM4a(audioData, sampleRate)
            }
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                saveToMediaStore(audioBytes, fileName, format)
            } else {
                saveToLegacyStorage(audioBytes, fileName)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun saveToMediaStore(audioData: ByteArray, fileName: String, format: AudioFormat): Result<String> {
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, format.mimeType)
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
        }
        
        val uri = context.contentResolver.insert(
            MediaStore.Downloads.EXTERNAL_CONTENT_URI,
            contentValues
        ) ?: return Result.failure(Exception("Failed to create file"))
        
        context.contentResolver.openOutputStream(uri)?.use { outputStream ->
            outputStream.write(audioData)
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
    
    private fun convertToM4a(audioData: FloatArray, sampleRate: Int): ByteArray {
        // Create a temporary file for muxing
        val tempFile = File.createTempFile("temp_audio", ".m4a", context.cacheDir)
        
        try {
            // Convert float to 16-bit PCM first
            val pcmData = ByteBuffer.allocate(audioData.size * 2).order(ByteOrder.LITTLE_ENDIAN)
            for (sample in audioData) {
                val pcmSample = (sample * 32767f).toInt().coerceIn(-32768, 32767).toShort()
                pcmData.putShort(pcmSample)
            }
            pcmData.rewind()
            
            // Setup MediaCodec for AAC encoding
            val format = MediaFormat.createAudioFormat(MediaFormat.MIMETYPE_AUDIO_AAC, sampleRate, 1)
            format.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC)
            format.setInteger(MediaFormat.KEY_BIT_RATE, 128000)
            format.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 16384)
            
            val codec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_AUDIO_AAC)
            codec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
            codec.start()
            
            // Setup MediaMuxer
            val muxer = MediaMuxer(tempFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
            var trackIndex = -1
            var muxerStarted = false
            
            val bufferInfo = MediaCodec.BufferInfo()
            var inputDone = false
            var outputDone = false
            var presentationTimeUs = 0L
            
            try {
                while (!outputDone) {
                    // Feed input
                    if (!inputDone) {
                        val inputBufferId = codec.dequeueInputBuffer(10000)
                        if (inputBufferId >= 0) {
                            val inputBuffer = codec.getInputBuffer(inputBufferId)
                            if (inputBuffer != null) {
                                inputBuffer.clear()
                                
                                val chunkSize = minOf(inputBuffer.remaining(), pcmData.remaining())
                                if (chunkSize > 0) {
                                    val chunk = ByteArray(chunkSize)
                                    pcmData.get(chunk)
                                    inputBuffer.put(chunk)
                                    codec.queueInputBuffer(inputBufferId, 0, chunkSize, presentationTimeUs, 0)
                                    presentationTimeUs += (chunkSize / 2 * 1000000L) / sampleRate
                                } else {
                                    codec.queueInputBuffer(inputBufferId, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                                    inputDone = true
                                }
                            }
                        }
                    }
                    
                    // Get output
                    val outputBufferId = codec.dequeueOutputBuffer(bufferInfo, 10000)
                    when {
                        outputBufferId == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
                            val newFormat = codec.outputFormat
                            trackIndex = muxer.addTrack(newFormat)
                            muxer.start()
                            muxerStarted = true
                        }
                        outputBufferId >= 0 -> {
                            val outputBuffer = codec.getOutputBuffer(outputBufferId)
                            if (bufferInfo.size > 0 && outputBuffer != null && muxerStarted) {
                                outputBuffer.position(bufferInfo.offset)
                                outputBuffer.limit(bufferInfo.offset + bufferInfo.size)
                                muxer.writeSampleData(trackIndex, outputBuffer, bufferInfo)
                            }
                            
                            codec.releaseOutputBuffer(outputBufferId, false)
                            
                            if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                                outputDone = true
                            }
                        }
                    }
                }
            } finally {
                if (muxerStarted) {
                    muxer.stop()
                }
                muxer.release()
                codec.stop()
                codec.release()
            }
            
            // Read the muxed file
            return tempFile.readBytes()
        } finally {
            tempFile.delete()
        }
    }
}
