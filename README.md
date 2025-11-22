# Supertonic TTS - Android App

A native Android app for on-device text-to-speech using Supertonic TTS and ONNX Runtime.

## Features

✨ **On-Device Processing** - No internet required, complete privacy
🎯 **Multiple Voices** - 4 voice styles (F1, F2, M1, M2)
⚡ **Fast Generation** - Optimized ONNX inference
🎛️ **Customizable** - Adjust speed (0.5x-2.0x) and quality (1-20 steps)
📱 **Native Android** - Built with Kotlin and Jetpack Compose
🔒 **Privacy First** - All data stays on your device

## Quick Start

### Build the APK

1. Open project in Android Studio
2. Build → Build Bundle(s) / APK(s) → Build APK(s)
3. Find APK at: `app/build/outputs/apk/debug/app-debug.apk`

Or use command line:
```bash
gradlew.bat assembleDebug
```

### Install

Transfer the APK to your Android device and install it.

**Requirements:**
- Android 8.0 (API 26) or higher
- ~500 MB free storage
- 2GB+ RAM recommended

## Usage

1. Launch the app
2. Wait for "Ready" status (~10 seconds on first launch)
3. Enter text or use the default sample
4. Select a voice style (F1, F2, M1, M2)
5. Adjust speed and denoising steps if desired
6. Tap "Generate Speech"
7. Audio plays automatically when ready

## What's Included

All models and voice styles are bundled in the APK:

**ONNX Models:**
- Duration Predictor
- Text Encoder
- Vector Estimator (Diffusion)
- Vocoder

**Voice Styles:**
- F1.json - Female voice 1
- F2.json - Female voice 2
- M1.json - Male voice 1
- M2.json - Male voice 2

## Documentation

- [Quick Start Guide](QUICK_START.md) - Get started in 3 steps
- [Build Instructions](BUILD_INSTRUCTIONS.md) - Detailed build guide
- [Project Structure](PROJECT_STRUCTURE.md) - Code architecture

## Technology Stack

- **Language**: Kotlin
- **UI**: Jetpack Compose + Material 3
- **ML Runtime**: ONNX Runtime Android
- **Architecture**: MVVM with ViewModel
- **Async**: Kotlin Coroutines

## Performance

- **Generation Time**: 5-15 seconds (after initial load)
- **Quality vs Speed**: Adjust denoising steps
  - 2-3 steps: Fast, good quality
  - 5 steps: Balanced (recommended)
  - 10+ steps: Slower, best quality

## Credits

Based on [Supertonic TTS](https://github.com/supertone-inc/supertonic) by Supertone Inc.

## License

- App code: MIT License
- Supertonic models: OpenRAIL-M License
- ONNX Runtime: MIT License

---

**Note**: First generation takes longer as models are loaded into memory. Subsequent generations are much faster.
