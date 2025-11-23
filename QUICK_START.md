# Quick Start Guide

## Build Your APK in 3 Steps

### Step 1: Open in Android Studio
1. Launch Android Studio
2. Select "Open" from the welcome screen
3. Navigate to this project folder and click "OK"
4. Wait for Gradle sync (this may take a few minutes on first run)

### Step 2: Build the APK
Click the menu: **Build → Build Bundle(s) / APK(s) → Build APK(s)**

Wait for the build to complete (you'll see a notification in the bottom right)

### Step 3: Get Your APK
Click "locate" in the notification, or find it at:
```
app/build/outputs/apk/debug/app-debug.apk
```

## Install on Your Phone
1. Copy `app-debug.apk` to your Android phone
2. Open the file on your phone
3. Allow installation from unknown sources if prompted
4. Tap "Install"

## Using the App
1. Open "Supertonic TTS" on your phone
2. Wait for "Ready" status (first launch takes ~10 seconds to load models)
3. Enter text or use the default text
4. Select a voice (F1, F2, M1, or M2)
5. Adjust speed and quality if desired
6. Tap "Generate Speech"
7. Audio will play automatically when ready

## Tips
- **Faster generation**: Use 2-3 denoising steps
- **Better quality**: Use 5-10 denoising steps
- **Speed control**: 1.0 = normal, 1.5 = faster, 0.7 = slower
- **Long text**: The app automatically splits long text into chunks

## Troubleshooting
**"Error initializing"**: Make sure your phone has Android 8.0 or higher

**Build fails**: In Android Studio, try:
- File → Invalidate Caches / Restart
- Then rebuild

**App crashes**: Your device may need more RAM. Try:
- Closing other apps
- Using shorter text
- Reducing denoising steps to 2-3

## What's Included
✅ All ONNX models (bundled in APK)
✅ 4 voice styles (F1, F2, M1, M2)
✅ No internet required
✅ Complete privacy - everything runs on your device
✅ Fast generation (typically 5-15 seconds)

Enjoy your on-device TTS! 🎉
