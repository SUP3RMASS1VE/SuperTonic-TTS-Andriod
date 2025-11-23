# Project Structure

## Overview
This Android app implements Supertonic TTS using ONNX Runtime for on-device speech synthesis.

## Directory Structure

```
app/
├── src/main/
│   ├── assets/                          # Bundled in APK
│   │   ├── onnx/                        # ONNX models
│   │   │   ├── duration_predictor.onnx  # Predicts speech duration
│   │   │   ├── text_encoder.onnx        # Encodes text to embeddings
│   │   │   ├── vector_estimator.onnx    # Denoising diffusion model
│   │   │   ├── vocoder.onnx             # Converts latents to audio
│   │   │   ├── tts.json                 # Model configuration
│   │   │   └── unicode_indexer.json     # Character to index mapping
│   │   └── voice_styles/                # Voice presets
│   │       ├── F1.json                  # Female voice 1
│   │       ├── F2.json                  # Female voice 2
│   │       ├── M1.json                  # Male voice 1
│   │       └── M2.json                  # Male voice 2
│   │
│   ├── java/com/sup3rmass1ve/supertonic/
│   │   ├── MainActivity.kt              # Main UI entry point
│   │   │
│   │   ├── model/                       # Data models
│   │   │   ├── VoiceStyle.kt           # Voice style data structures
│   │   │   └── TTSConfig.kt            # TTS configuration models
│   │   │
│   │   ├── tts/                         # TTS engine
│   │   │   ├── SupertonicTTS.kt        # Main TTS inference engine
│   │   │   ├── TextProcessor.kt        # Text preprocessing
│   │   │   └── VoiceStyleLoader.kt     # Loads voice styles from assets
│   │   │
│   │   ├── audio/                       # Audio playback
│   │   │   └── AudioPlayer.kt          # Handles audio output
│   │   │
│   │   ├── viewmodel/                   # UI state management
│   │   │   └── TTSViewModel.kt         # ViewModel for TTS screen
│   │   │
│   │   └── ui/theme/                    # UI theming
│   │       ├── Color.kt
│   │       ├── Theme.kt
│   │       └── Type.kt
│   │
│   └── AndroidManifest.xml              # App configuration
│
├── build.gradle.kts                     # App-level build config
└── proguard-rules.pro                   # ProGuard rules

gradle/
└── libs.versions.toml                   # Dependency versions

build.gradle.kts                         # Project-level build config
settings.gradle.kts                      # Project settings
```

## Key Components

### 1. TTS Engine (`tts/SupertonicTTS.kt`)
- Loads and manages ONNX models
- Implements the 5-step inference pipeline:
  1. Text preprocessing
  2. Duration prediction
  3. Text encoding
  4. Iterative denoising (diffusion)
  5. Vocoding (latent to audio)
- Handles text chunking for long inputs

### 2. Text Processor (`tts/TextProcessor.kt`)
- Normalizes Unicode text
- Converts characters to model indices
- Creates attention masks

### 3. Voice Style Loader (`tts/VoiceStyleLoader.kt`)
- Scans available voice styles
- Loads voice embeddings from JSON
- Provides voice style data to TTS engine

### 4. Audio Player (`audio/AudioPlayer.kt`)
- Uses Android AudioTrack API
- Plays generated audio in real-time
- Handles audio lifecycle

### 5. ViewModel (`viewmodel/TTSViewModel.kt`)
- Manages UI state
- Coordinates TTS generation
- Handles async operations with coroutines
- Provides reactive state updates

### 6. UI (`MainActivity.kt`)
- Jetpack Compose UI
- Material 3 design
- Reactive UI based on ViewModel state
- Input controls for text, voice, speed, steps

## Data Flow

```
User Input (Text, Voice, Settings)
    ↓
TTSViewModel
    ↓
SupertonicTTS.generateSpeech()
    ↓
1. TextProcessor → text_ids, text_mask
2. DurationPredictor → duration
3. TextEncoder → text_embeddings
4. VectorEstimator (loop) → denoised_latent
5. Vocoder → audio_waveform
    ↓
AudioPlayer.play()
    ↓
Audio Output
```

## Dependencies

### Core
- **ONNX Runtime Android** (1.20.1): ML inference engine
- **Gson** (2.10.1): JSON parsing for configs and voice styles
- **Kotlin Coroutines** (1.7.3): Async operations

### UI
- **Jetpack Compose**: Modern declarative UI
- **Material 3**: Material Design components
- **ViewModel**: UI state management
- **Lifecycle**: Android lifecycle handling

## Build Configuration

### Gradle Files
- `build.gradle.kts` (project): Plugin versions
- `build.gradle.kts` (app): Dependencies, SDK versions, build types
- `libs.versions.toml`: Centralized version management

### SDK Versions
- **minSdk**: 26 (Android 8.0)
- **targetSdk**: 35 (Android 15)
- **compileSdk**: 35

### Supported ABIs
- armeabi-v7a (32-bit ARM)
- arm64-v8a (64-bit ARM)
- x86 (32-bit Intel)
- x86_64 (64-bit Intel)

## Asset Management

All assets are bundled in the APK at build time:
- ONNX models: ~200 MB
- Voice styles: ~50 MB each
- Total APK size: ~250-300 MB

Assets are accessed via `context.assets.open("path/to/file")`

## Performance Considerations

### Memory
- Models loaded once at startup
- Kept in memory for fast inference
- ~500 MB RAM usage during generation

### Speed
- First generation: 10-20 seconds (model loading)
- Subsequent: 5-15 seconds (inference only)
- Depends on: text length, denoising steps, device CPU

### Optimization
- Text chunking prevents memory issues
- Reuses ONNX sessions
- Async processing with coroutines
- Efficient tensor operations

## Testing

Run diagnostics:
```bash
gradlew test
```

Run on device:
```bash
gradlew installDebug
```

## Future Enhancements

Potential improvements:
- [ ] Save generated audio to file
- [ ] Batch processing
- [ ] GPU acceleration (if available)
- [ ] Custom voice style creation
- [ ] Audio effects (reverb, pitch shift)
- [ ] Background generation service
- [ ] Share audio functionality
