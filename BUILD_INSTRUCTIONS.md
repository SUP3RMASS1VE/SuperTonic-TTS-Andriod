# Supertonic TTS Android App - Build Instructions

## Overview
This is a native Android app that runs Supertonic TTS (Text-to-Speech) entirely on-device using ONNX Runtime. All models and voice styles are bundled in the APK.

## Prerequisites
- Android Studio (latest version recommended)
- Android SDK 26 or higher
- Gradle 8.x

## Building the APK

### Method 1: Using Android Studio (Recommended)
1. Open Android Studio
2. Click "Open" and select this project folder
3. Wait for Gradle sync to complete
4. Click "Build" → "Build Bundle(s) / APK(s)" → "Build APK(s)"
5. Once complete, click "locate" to find your APK in `app/build/outputs/apk/debug/`

### Method 2: Using Command Line
```bash
# On Windows
gradlew.bat assembleDebug

# On Mac/Linux
./gradlew assembleDebug
```

The APK will be generated at: `app/build/outputs/apk/debug/app-debug.apk`

## Installing the APK

### On Physical Device
1. Enable "Developer Options" on your Android device
2. Enable "USB Debugging" in Developer Options
3. Connect your device via USB
4. Run: `adb install app/build/outputs/apk/debug/app-debug.apk`

### On Emulator
1. Start an Android emulator from Android Studio
2. Drag and drop the APK onto the emulator window

### Manual Installation
1. Transfer the APK to your device
2. Open the APK file on your device
3. Allow installation from unknown sources if prompted
4. Install the app

## App Features
- **On-Device TTS**: All processing happens locally, no internet required
- **Multiple Voice Styles**: Choose from F1, F2, M1, M2 voices
- **Adjustable Speed**: Control speech speed from 0.5x to 2.0x
- **Denoising Steps**: Adjust quality with 1-20 denoising steps
- **Text Chunking**: Automatically handles long text

## Included Assets
- ONNX Models (bundled in APK):
  - duration_predictor.onnx
  - text_encoder.onnx
  - vector_estimator.onnx
  - vocoder.onnx
  - Configuration files (tts.json, unicode_indexer.json)

- Voice Styles (bundled in APK):
  - F1.json (Female voice 1)
  - F2.json (Female voice 2)
  - M1.json (Male voice 1)
  - M2.json (Male voice 2)

## APK Size
The APK will be approximately 200-300 MB due to the bundled ONNX models and voice styles.

## Minimum Requirements
- Android 8.0 (API 26) or higher
- ~500 MB free storage
- Recommended: 2GB+ RAM for smooth performance

## Troubleshooting

### Build Errors
- Make sure you have the latest Android SDK installed
- Try "File" → "Invalidate Caches / Restart" in Android Studio
- Run `gradlew clean` before building

### Runtime Errors
- Ensure your device meets minimum requirements
- Check that all assets are properly copied to `app/src/main/assets/`

## Performance Notes
- First generation may take longer as models are loaded
- Subsequent generations will be faster
- Lower denoising steps = faster generation (but lower quality)
- Recommended: 5 steps for good balance of speed and quality

## License
This implementation uses:
- Supertonic models (OpenRAIL-M License)
- ONNX Runtime (MIT License)
- Sample code (MIT License)
