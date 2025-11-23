# Supertonic TTS - Android App

A native Android app for on-device text-to-speech using [Supertonic TTS](https://github.com/supertone-inc/supertonic) and ONNX Runtime.

## Features

🎯 **On-Device Processing** - No internet required, complete privacy  
🎤 **4 Voice Styles** - Choose from F1, F2, M1, M2 voices  
⚡ **Fast Generation** - Optimized ONNX inference (5-15 seconds)  
🎛️ **Customizable** - Adjust speed (0.5x-2.0x) and quality (1-20 steps)  
💾 **Save Audio** - Download generated speech as WAV files  
🔒 **Privacy First** - All processing happens locally on your device  

## Download

Download the latest APK from the [Releases](../../releases) page.

## Requirements

- Android 8.0 (API 26) or higher
- ~500 MB free storage
- 2GB+ RAM recommended

## Installation

1. Download the APK from releases
2. Open the APK file on your Android device
3. Allow installation from unknown sources if prompted
4. Install and enjoy!

## Usage

1. **Launch the app** - Wait ~10 seconds for models to load
2. **Enter text** - Type or use the default sample text
3. **Select voice** - Choose from F1, F2, M1, or M2
4. **Adjust settings** (optional):
   - Speed: 0.5x (slow) to 2.0x (fast)
   - Denoising steps: 1-20 (higher = better quality, slower)
5. **Generate Speech** - Tap the button and wait 5-15 seconds
6. **Save Audio** - Download the generated speech as a WAV file

## Voice Styles

- **F1** - Clear female voice, neutral tone
- **F2** - Softer female voice, warm tone
- **M1** - Deep male voice, authoritative
- **M2** - Lighter male voice, friendly

## Performance

- **First launch**: ~10 seconds (model loading)
- **Short text (50 chars)**: 3-5 seconds
- **Medium text (150 chars)**: 5-10 seconds
- **Long text (300 chars)**: 10-15 seconds

### Tips for Better Performance

**Faster generation:**
- Use 2-3 denoising steps
- Increase speed to 1.3-1.5x
- Keep text under 200 characters

**Better quality:**
- Use 8-10 denoising steps
- Keep speed at 1.0x
- Use proper punctuation

## What's Included

All models and voice styles are bundled in the APK (~250-300 MB):

**ONNX Models:**
- Duration Predictor (1.5 MB)
- Text Encoder (26.7 MB)
- Vector Estimator (126.3 MB)
- Vocoder (96.7 MB)

**Voice Styles:**
- F1.json - Female voice 1
- F2.json - Female voice 2
- M1.json - Male voice 1
- M2.json - Male voice 2

## Privacy

✅ No internet connection required  
✅ No data collection  
✅ No analytics  
✅ No permissions needed (except storage for saving files)  
✅ Everything runs locally on your device  

Your text and audio never leave your device!

## Technology

- **Language**: Kotlin
- **UI**: Jetpack Compose + Material 3
- **ML Runtime**: ONNX Runtime Android
- **Architecture**: MVVM with ViewModel
- **Models**: Supertonic TTS by Supertone Inc.

## Screenshots

*Coming soon*

## Troubleshooting

**App crashes on launch**
- Check your device has Android 8.0+
- Ensure you have 2GB+ RAM
- Try closing other apps

**"Error initializing"**
- Reinstall the app
- Check you have 500+ MB free storage

**No audio output**
- Check device volume
- Try a different voice style
- Restart the app

**Generation takes too long**
- Reduce denoising steps to 2-3
- Shorten your text
- Close background apps

## Credits

- **Supertonic TTS**: [Supertone Inc.](https://github.com/supertone-inc/supertonic)
- **ONNX Runtime**: [Microsoft](https://onnxruntime.ai/)
- **Android Implementation**: Native Kotlin app

## License

- App code: MIT License
- Supertonic models: OpenRAIL-M License
- ONNX Runtime: MIT License

## Support

If you encounter issues:
1. Check the troubleshooting section above
2. Make sure you're using the latest APK
3. Open an issue with details about your device and Android version

## Changelog

### v1.0.0 (Initial Release)
- On-device TTS with 4 voice styles
- Speed and quality controls
- Save audio as WAV files
- Material 3 UI
- Complete privacy (no internet required)

---

**Note**: This app requires significant storage (~300 MB) and RAM (~500 MB during generation) due to the bundled AI models. First generation takes longer as models are loaded into memory.

Made with ❤️ using Supertonic TTS
