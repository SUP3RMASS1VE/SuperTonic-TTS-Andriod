package com.sup3rmass1ve.supertonic.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class AudioPlayer(private val sampleRate: Int) {
    private var audioTrack: AudioTrack? = null
    private var currentAudioData: FloatArray? = null
    private var playbackJob: Job? = null
    private var currentPosition: Int = 0
    private var isCurrentlyPlaying: Boolean = false
    
    var onPositionUpdate: ((position: Int, duration: Int) -> Unit)? = null
    var onPlaybackComplete: (() -> Unit)? = null
    
    fun loadAudio(audioData: FloatArray) {
        stop()
        currentAudioData = audioData
        currentPosition = 0
        isCurrentlyPlaying = false
    }
    
    fun play() {
        val data = currentAudioData ?: return
        
        if (isCurrentlyPlaying) {
            return
        }
        
        isCurrentlyPlaying = true
        
        playbackJob?.cancel()
        playbackJob = CoroutineScope(Dispatchers.IO).launch {
            try {
                val bufferSize = AudioTrack.getMinBufferSize(
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_FLOAT
                )
                
                val track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_FLOAT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(bufferSize)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()
                
                audioTrack = track
                track.play()
                
                val chunkSize = bufferSize / 4 // Convert bytes to floats
                var position = currentPosition
                
                while (isActive && isCurrentlyPlaying && position < data.size) {
                    val remaining = data.size - position
                    val toWrite = minOf(chunkSize, remaining)
                    
                    val written = track.write(
                        data,
                        position,
                        toWrite,
                        AudioTrack.WRITE_BLOCKING
                    )
                    
                    if (written > 0) {
                        position += written
                        currentPosition = position
                        onPositionUpdate?.invoke(position, data.size)
                    }
                    
                    delay(10)
                }
                
                // Wait for audio to finish playing
                if (position >= data.size) {
                    delay(100)
                    onPlaybackComplete?.invoke()
                    currentPosition = 0
                }
                
                // Clean up track
                try {
                    if (track.state == AudioTrack.STATE_INITIALIZED) {
                        track.stop()
                    }
                    track.release()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                
                audioTrack = null
                
            } catch (e: Exception) {
                e.printStackTrace()
                audioTrack?.let { track ->
                    try {
                        if (track.state == AudioTrack.STATE_INITIALIZED) {
                            track.stop()
                        }
                        track.release()
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                    }
                }
                audioTrack = null
            } finally {
                isCurrentlyPlaying = false
            }
        }
    }
    
    fun pause() {
        isCurrentlyPlaying = false
        playbackJob?.cancel()
        try {
            audioTrack?.let { track ->
                if (track.state == AudioTrack.STATE_INITIALIZED) {
                    track.pause()
                    track.flush()
                }
                track.release()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            audioTrack = null
        }
    }
    
    fun stop() {
        isCurrentlyPlaying = false
        playbackJob?.cancel()
        try {
            audioTrack?.let { track ->
                if (track.state == AudioTrack.STATE_INITIALIZED) {
                    track.stop()
                }
                track.release()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            audioTrack = null
            currentPosition = 0
        }
    }
    
    fun seekTo(position: Int) {
        val data = currentAudioData ?: return
        val wasPlaying = isCurrentlyPlaying
        
        pause()
        
        currentPosition = position.coerceIn(0, data.size)
        
        if (wasPlaying) {
            play()
        }
    }
    
    fun isPlaying(): Boolean {
        return isCurrentlyPlaying
    }
    
    fun getCurrentPosition(): Int = currentPosition
    
    fun getDuration(): Int = currentAudioData?.size ?: 0
}
