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
                
                val audio = withContext(Dispatchers.IO) {
                    val voiceStyle = voiceStyleLoader.loadVoiceStyle(currentState.selectedVoiceStyle)
                    tts.generateSpeech(
                        text = currentState.inputText,
                        voiceStyle = voiceStyle,
                        speed = currentState.speed,
                        totalSteps = currentState.denoisingSteps
                    )
                }
                
                _uiState.value = currentState.copy(
                    isGenerating = false,
                    status = "Ready",
                    generatedAudio = audio,
                    playbackPosition = 0,
                    playbackDuration = audio.size
                )
                
                withContext(Dispatchers.IO) {
                    audioPlayer?.loadAudio(audio)
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
                val fileName = "supertonic_${timestamp}.wav"
                
                val result = withContext(Dispatchers.IO) {
                    audioSaver.saveAudioToDownloads(audio, tts.sampleRate, fileName)
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
    val isGenerating: Boolean = false,
    val isInitialized: Boolean = false,
    val status: String = "Initializing...",
    val generatedAudio: FloatArray? = null,
    val isPlaying: Boolean = false,
    val playbackPosition: Int = 0,
    val playbackDuration: Int = 0,
    val sampleRate: Int = 22050
)
