package com.sup3rmass1ve.supertonic.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sup3rmass1ve.supertonic.audio.AudioPlayer
import com.sup3rmass1ve.supertonic.model.VoiceStyle
import com.sup3rmass1ve.supertonic.tts.SupertonicTTS
import com.sup3rmass1ve.supertonic.tts.VoiceStyleLoader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TTSViewModel(application: Application) : AndroidViewModel(application) {
    private val tts = SupertonicTTS(application)
    private val voiceStyleLoader = VoiceStyleLoader(application)
    private var audioPlayer: AudioPlayer? = null
    private val audioSaver = com.sup3rmass1ve.supertonic.audio.AudioSaver(application)
    
    private val _uiState = MutableStateFlow(TTSUiState())
    val uiState: StateFlow<TTSUiState> = _uiState
    
    init {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(status = "Initializing TTS engine...")
                withContext(Dispatchers.IO) {
                    try {
                        tts.initialize()
                    } catch (e: Exception) {
                        throw Exception("Failed to load models: ${e.message}", e)
                    }
                }
                
                try {
                    audioPlayer = AudioPlayer(tts.sampleRate).apply {
                        onPositionUpdate = { position, duration ->
                            _uiState.value = _uiState.value.copy(
                                playbackPosition = position,
                                playbackDuration = duration
                            )
                        }
                        onPlaybackComplete = {
                            _uiState.value = _uiState.value.copy(
                                isPlaying = false,
                                status = "Ready"
                            )
                        }
                    }
                } catch (e: Exception) {
                    throw Exception("Failed to create audio player: ${e.message}", e)
                }
                
                val voiceStyles = voiceStyleLoader.getAvailableVoiceStyles()
                if (voiceStyles.isEmpty()) {
                    throw Exception("No voice styles found in assets")
                }
                
                _uiState.value = _uiState.value.copy(
                    voiceStyles = voiceStyles,
                    selectedVoiceStyle = voiceStyles.firstOrNull() ?: "",
                    status = "Ready",
                    isInitialized = true,
                    sampleRate = tts.sampleRate
                )
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = _uiState.value.copy(
                    status = "Error initializing: ${e.message}\n\nPlease reinstall the app.",
                    isInitialized = false
                )
            }
        }
    }
    
    fun updateText(text: String) {
        _uiState.value = _uiState.value.copy(inputText = text)
    }
    
    fun updateVoiceStyle(style: String) {
        _uiState.value = _uiState.value.copy(selectedVoiceStyle = style)
    }
    
    fun updateSpeed(speed: Float) {
        _uiState.value = _uiState.value.copy(speed = speed)
    }
    
    fun updateSteps(steps: Int) {
        _uiState.value = _uiState.value.copy(denoisingSteps = steps)
    }
    
    fun updateSeed(seed: Long?) {
        _uiState.value = _uiState.value.copy(seed = seed)
    }
    
    fun updateAudioFormat(format: com.sup3rmass1ve.supertonic.audio.AudioFormat) {
        _uiState.value = _uiState.value.copy(selectedAudioFormat = format)
    }
    
    fun generateSpeech() {
        val currentState = _uiState.value
        
        if (currentState.inputText.isBlank()) {
            _uiState.value = currentState.copy(status = "Please enter some text")
            return
        }
        
        if (currentState.selectedVoiceStyle.isBlank()) {
            _uiState.value = currentState.copy(status = "Please select a voice style")
            return
        }
        
        viewModelScope.launch {
            try {
                _uiState.value = currentState.copy(
                    isGenerating = true,
                    status = "Generating speech..."
                )
                
                val startTime = System.currentTimeMillis()
                val result = withContext(Dispatchers.IO) {
                    val voiceStyle = voiceStyleLoader.loadVoiceStyle(currentState.selectedVoiceStyle)
                    tts.generateSpeech(
                        text = currentState.inputText,
                        voiceStyle = voiceStyle,
                        speed = currentState.speed,
                        totalSteps = currentState.denoisingSteps,
                        seed = currentState.seed
                    )
                }
                val generationTime = System.currentTimeMillis() - startTime
                
                val audioDuration = result.audio.size.toFloat() / tts.sampleRate
                val generationInfo = GenerationInfo(
                    seed = result.seedUsed,
                    speed = currentState.speed,
                    denoisingSteps = currentState.denoisingSteps,
                    voiceStyle = currentState.selectedVoiceStyle.removeSuffix(".json"),
                    audioDuration = audioDuration,
                    generationTime = generationTime / 1000f,
                    sampleRate = tts.sampleRate,
                    audioSamples = result.audio.size
                )
                
                _uiState.value = currentState.copy(
                    isGenerating = false,
                    status = "Ready",
                    generatedAudio = result.audio,
                    playbackPosition = 0,
                    playbackDuration = result.audio.size,
                    generationInfo = generationInfo
                )
                
                withContext(Dispatchers.IO) {
                    audioPlayer?.loadAudio(result.audio)
                    audioPlayer?.play()
                }
                
                _uiState.value = _uiState.value.copy(isPlaying = true)
                
            } catch (e: Exception) {
                _uiState.value = currentState.copy(
                    isGenerating = false,
                    status = "Error: ${e.message}"
                )
            }
        }
    }
    
    fun playPauseAudio() {
        val player = audioPlayer ?: return
        
        if (_uiState.value.isPlaying) {
            player.pause()
            _uiState.value = _uiState.value.copy(isPlaying = false)
        } else {
            player.play()
            _uiState.value = _uiState.value.copy(isPlaying = true)
        }
    }
    
    fun stopAudio() {
        audioPlayer?.stop()
        _uiState.value = _uiState.value.copy(
            isPlaying = false,
            playbackPosition = 0,
            status = "Ready"
        )
    }
    
    fun seekTo(position: Int) {
        audioPlayer?.seekTo(position)
        _uiState.value = _uiState.value.copy(playbackPosition = position)
    }
    
    fun saveAudio() {
        val currentState = _uiState.value
        val audio = currentState.generatedAudio ?: return
        
        viewModelScope.launch {
            try {
                _uiState.value = currentState.copy(status = "Saving audio...")
                
                val timestamp = System.currentTimeMillis()
                val format = currentState.selectedAudioFormat
                val fileName = "supertonic_${timestamp}.${format.extension}"
                
                val result = withContext(Dispatchers.IO) {
                    audioSaver.saveAudioToDownloads(audio, tts.sampleRate, fileName, format)
                }
                
                result.fold(
                    onSuccess = { message ->
                        _uiState.value = _uiState.value.copy(status = message)
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(status = "Save failed: ${error.message}")
                    }
                )
                
            } catch (e: Exception) {
                _uiState.value = currentState.copy(status = "Error saving: ${e.message}")
            }
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        audioPlayer?.stop()
        tts.close()
    }
}

data class TTSUiState(
    val inputText: String = "This morning, I took a walk in the park, and the sound of the birds and the breeze was so pleasant that I stopped for a long time just to listen.",
    val voiceStyles: List<String> = emptyList(),
    val selectedVoiceStyle: String = "",
    val speed: Float = 1.05f,
    val denoisingSteps: Int = 5,
    val seed: Long? = null,
    val isGenerating: Boolean = false,
    val isInitialized: Boolean = false,
    val status: String = "Initializing...",
    val generatedAudio: FloatArray? = null,
    val isPlaying: Boolean = false,
    val playbackPosition: Int = 0,
    val playbackDuration: Int = 0,
    val sampleRate: Int = 22050,
    val generationInfo: GenerationInfo? = null,
    val selectedAudioFormat: com.sup3rmass1ve.supertonic.audio.AudioFormat = com.sup3rmass1ve.supertonic.audio.AudioFormat.WAV
)

data class GenerationInfo(
    val seed: Long,
    val speed: Float,
    val denoisingSteps: Int,
    val voiceStyle: String,
    val audioDuration: Float,
    val generationTime: Float,
    val sampleRate: Int,
    val audioSamples: Int
)
