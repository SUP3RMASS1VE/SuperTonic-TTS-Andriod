package com.sup3rmass1ve.supertonic.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sup3rmass1ve.supertonic.audio.AudioPlayer
import com.sup3rmass1ve.supertonic.tts.SupertonicTTS
import com.sup3rmass1ve.supertonic.tts.VoiceStyleLoader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TTSViewModel(application: Application) : AndroidViewModel(application) {
    private var supertonicTTS: SupertonicTTS? = null
    private val voiceStyleLoader = VoiceStyleLoader(application)
    private var audioPlayer: AudioPlayer? = null
    private val audioSaver = com.sup3rmass1ve.supertonic.audio.AudioSaver(application)
    private val context = application
    
    private val _uiState = MutableStateFlow(TTSUiState())
    val uiState: StateFlow<TTSUiState> = _uiState
    
    init {
        viewModelScope.launch {
            initializeEngine()
        }
    }
    
    private suspend fun initializeEngine() {
        try {
            _uiState.value = _uiState.value.copy(
                status = "Initializing Supertonic engine...",
                isInitialized = false
            )
            
            withContext(Dispatchers.IO) {
                if (supertonicTTS == null) {
                    supertonicTTS = SupertonicTTS(context)
                }
                supertonicTTS!!.initialize()
            }
            
            val sampleRate = supertonicTTS!!.sampleRate
            
            // Recreate audio player with correct sample rate
            audioPlayer?.stop()
            audioPlayer = AudioPlayer(sampleRate).apply {
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
            
            // Load all voices from assets
            val voices = voiceStyleLoader.getAvailableVoiceStyles()
            
            _uiState.value = _uiState.value.copy(
                voiceStyles = voices,
                selectedVoiceStyle = voices.firstOrNull() ?: "",
                status = "Ready",
                isInitialized = true,
                sampleRate = sampleRate,
                generatedAudio = null,
                generationInfo = null
            )
        } catch (e: Exception) {
            e.printStackTrace()
            _uiState.value = _uiState.value.copy(
                status = "Error initializing: ${e.message}",
                isInitialized = false
            )
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
                
                val tts = supertonicTTS ?: throw IllegalStateException("Supertonic TTS not initialized")
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
                val audio = result.audio
                val seedUsed = result.seedUsed
                val sampleRate = tts.sampleRate
                
                val generationTime = System.currentTimeMillis() - startTime
                
                val audioDuration = audio.size.toFloat() / sampleRate
                val generationInfo = GenerationInfo(
                    seed = seedUsed,
                    speed = currentState.speed,
                    denoisingSteps = currentState.denoisingSteps,
                    voiceStyle = currentState.selectedVoiceStyle.removeSuffix(".json"),
                    audioDuration = audioDuration,
                    generationTime = generationTime / 1000f,
                    sampleRate = sampleRate,
                    audioSamples = audio.size
                )
                
                _uiState.value = currentState.copy(
                    isGenerating = false,
                    status = "Ready",
                    generatedAudio = audio,
                    playbackPosition = 0,
                    playbackDuration = audio.size,
                    generationInfo = generationInfo
                )
                
                withContext(Dispatchers.IO) {
                    audioPlayer?.loadAudio(audio)
                    audioPlayer?.play()
                }
                
                _uiState.value = _uiState.value.copy(isPlaying = true)
                
            } catch (e: Exception) {
                e.printStackTrace()
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
                    audioSaver.saveAudioToDownloads(audio, currentState.sampleRate, fileName, format)
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
        supertonicTTS?.close()
    }
}

data class TTSUiState(
    val inputText: String = "This morning, I took a walk in the park, and the sound of the birds and the breeze was so pleasant that I stopped for a long time just to listen.",
    val voiceStyles: List<String> = emptyList(),
    val selectedVoiceStyle: String = "",
    val speed: Float = 1.0f,
    val denoisingSteps: Int = 5,
    val seed: Long? = null,
    val isGenerating: Boolean = false,
    val isInitialized: Boolean = false,
    val status: String = "Initializing...",
    val generatedAudio: FloatArray? = null,
    val isPlaying: Boolean = false,
    val playbackPosition: Int = 0,
    val playbackDuration: Int = 0,
    val sampleRate: Int = 44100,
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
