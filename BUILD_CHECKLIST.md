# Build Checklist ✅

Use this checklist to build your APK successfully.

## Pre-Build Verification

- [x] ✅ ONNX models copied to `app/src/main/assets/onnx/`
  - [x] duration_predictor.onnx
  - [x] text_encoder.onnx
  - [x] vector_estimator.onnx
  - [x] vocoder.onnx
  - [x] tts.json
  - [x] unicode_indexer.json

- [x] ✅ Voice styles copied to `app/src/main/assets/voice_styles/`
  - [x] F1.json
  - [x] F2.json
  - [x] M1.json
  - [x] M2.json

- [x] ✅ Dependencies configured
  - [x] ONNX Runtime Android 1.20.1
  - [x] Gson 2.10.1
  - [x] Kotlin Coroutines 1.7.3

- [x] ✅ Code files created
  - [x] MainActivity.kt
  - [x] SupertonicTTS.kt
  - [x] TextProcessor.kt
  - [x] VoiceStyleLoader.kt
  - [x] AudioPlayer.kt
  - [x] TTSViewModel.kt
  - [x] Data models

- [x] ✅ Build configuration
  - [x] build.gradle.kts updated
  - [x] libs.versions.toml updated
  - [x] AndroidManifest.xml configured

## Build Steps

### Step 1: Open Project
- [ ] Launch Android Studio
- [ ] Open this project folder
- [ ] Wait for Gradle sync to complete (may take 2-5 minutes)
- [ ] Check for any sync errors in the "Build" tab

### Step 2: Verify Setup
- [ ] Check that `app/src/main/assets/` folder exists
- [ ] Verify ONNX models are present (7 files)
- [ ] Verify voice styles are present (4 files)
- [ ] No red underlines in code files

### Step 3: Build APK
- [ ] Click: Build → Build Bundle(s) / APK(s) → Build APK(s)
- [ ] Wait for build to complete (may take 3-10 minutes first time)
- [ ] Look for "BUILD SUCCESSFUL" message
- [ ] Click "locate" to find your APK

### Step 4: Locate APK
- [ ] APK location: `app/build/outputs/apk/debug/app-debug.apk`
- [ ] APK size should be ~250-300 MB
- [ ] Note the file path for installation

## Installation Steps

### On Physical Device
- [ ] Enable Developer Options on your Android device
- [ ] Enable USB Debugging
- [ ] Connect device via USB
- [ ] Run: `adb install app/build/outputs/apk/debug/app-debug.apk`
- [ ] Or copy APK to device and install manually

### On Emulator
- [ ] Start Android emulator (API 26+)
- [ ] Drag and drop APK onto emulator
- [ ] Wait for installation to complete

## First Launch Test

- [ ] Open "Supertonic TTS" app
- [ ] Wait for "Initializing TTS engine..." message
- [ ] Wait for "Ready" status (~10 seconds)
- [ ] Verify voice styles appear in dropdown (F1, F2, M1, M2)
- [ ] Keep default text or enter your own
- [ ] Select a voice style
- [ ] Tap "Generate Speech"
- [ ] Wait for generation (~5-15 seconds)
- [ ] Verify audio plays automatically
- [ ] Test "Stop Audio" button

## Troubleshooting

### Build Fails
- [ ] Check Android SDK is installed (API 26-35)
- [ ] Try: File → Invalidate Caches / Restart
- [ ] Try: `gradlew clean` then rebuild
- [ ] Check internet connection (for downloading dependencies)

### Gradle Sync Fails
- [ ] Check `gradle.properties` exists
- [ ] Verify Java/JDK is installed
- [ ] Try: File → Sync Project with Gradle Files

### APK Won't Install
- [ ] Check device has Android 8.0+
- [ ] Enable "Install from Unknown Sources"
- [ ] Check device has 500+ MB free storage
- [ ] Try uninstalling old version first

### App Crashes on Launch
- [ ] Check device has 2GB+ RAM
- [ ] Verify all assets are in APK (check APK size ~250-300 MB)
- [ ] Check logcat for error messages
- [ ] Try on different device/emulator

### "Error initializing" Message
- [ ] Assets may be missing - rebuild APK
- [ ] Device may not support ONNX Runtime
- [ ] Check logcat for specific error

### No Audio Output
- [ ] Check device volume is up
- [ ] Verify audio permissions
- [ ] Try different voice style
- [ ] Check if other apps can play audio

## Success Indicators

✅ Build completes without errors
✅ APK is ~250-300 MB in size
✅ App installs successfully
✅ App shows "Ready" status after ~10 seconds
✅ All 4 voice styles appear in dropdown
✅ Speech generation completes in 5-15 seconds
✅ Audio plays automatically
✅ Can generate multiple times without crashing

## Performance Benchmarks

Expected performance on modern device:
- **Model loading**: 5-10 seconds (first time only)
- **Short text (50 chars)**: 3-5 seconds
- **Medium text (150 chars)**: 5-10 seconds
- **Long text (300 chars)**: 10-15 seconds

Settings for faster generation:
- Speed: 1.5x
- Denoising steps: 2-3

Settings for better quality:
- Speed: 1.0x
- Denoising steps: 8-10

## Command Line Alternative

If you prefer command line:

```bash
# Clean build
gradlew.bat clean

# Build debug APK
gradlew.bat assembleDebug

# Install on connected device
gradlew.bat installDebug

# Build and install
gradlew.bat clean assembleDebug installDebug
```

## Final Checklist

- [ ] APK built successfully
- [ ] APK installed on device
- [ ] App launches without crashing
- [ ] Models load successfully
- [ ] Speech generation works
- [ ] Audio playback works
- [ ] All voice styles available
- [ ] UI is responsive

## Next Steps After Success

1. **Share**: Copy APK to share with others
2. **Test**: Try different texts and voices
3. **Optimize**: Adjust speed and steps for your preference
4. **Customize**: Modify code to add features

## Support

If you encounter issues:
1. Check logcat output in Android Studio
2. Review error messages carefully
3. Verify all assets are present
4. Try on different device
5. Check documentation files

---

**Ready to build?** Start with Step 1 above! 🚀
