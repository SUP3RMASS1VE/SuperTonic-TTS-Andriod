# App User Guide

## What You'll See

### App Launch
When you first open the app, you'll see:
```
┌─────────────────────────────────┐
│  Supertonic TTS                 │
│  Generate speech from text      │
│  using ONNX Runtime             │
│                                 │
│  Status: Initializing TTS       │
│  engine...                      │
└─────────────────────────────────┘
```

After ~10 seconds:
```
┌─────────────────────────────────┐
│  Status: Ready                  │
└─────────────────────────────────┘
```

### Main Screen Layout

```
┌─────────────────────────────────────────┐
│  Supertonic TTS                         │
│  Generate speech from text using ONNX   │
│  Runtime                                │
│                                         │
│  ┌───────────────────────────────────┐ │
│  │ Input Text                        │ │
│  │                                   │ │
│  │ This morning, I took a walk in    │ │
│  │ the park, and the sound of the    │ │
│  │ birds and the breeze was so       │ │
│  │ pleasant that I stopped for a     │ │
│  │ long time just to listen.         │ │
│  └───────────────────────────────────┘ │
│                                         │
│  ┌───────────────────────────────────┐ │
│  │ Voice Style          [F1.json ▼] │ │
│  └───────────────────────────────────┘ │
│                                         │
│  Speed: 1.05                            │
│  ├─────────●─────────────────────────┤ │
│  0.5                              2.0  │
│                                         │
│  Denoising Steps: 5                     │
│  ├──────────●──────────────────────┤   │
│  1                                 20   │
│                                         │
│  ┌───────────────────────────────────┐ │
│  │      Generate Speech              │ │
│  └───────────────────────────────────┘ │
│                                         │
│  ┌───────────────────────────────────┐ │
│  │ Status: Ready                     │ │
│  └───────────────────────────────────┘ │
└─────────────────────────────────────────┘
```

## How to Use

### 1. Enter Text
- Tap the "Input Text" field
- Type or paste your text
- Or use the default sample text
- Maximum recommended: ~500 characters

### 2. Select Voice
Tap the "Voice Style" dropdown to see:
```
┌─────────────────┐
│ F1.json         │  ← Female voice 1
│ F2.json         │  ← Female voice 2
│ M1.json         │  ← Male voice 1
│ M2.json         │  ← Male voice 2
└─────────────────┘
```

### 3. Adjust Speed (Optional)
Drag the speed slider:
- **0.5** = Very slow, deliberate
- **1.0** = Normal speaking pace
- **1.05** = Slightly faster (default)
- **1.5** = Fast
- **2.0** = Very fast

### 4. Adjust Quality (Optional)
Drag the denoising steps slider:
- **1-2 steps** = Fast, lower quality
- **3-5 steps** = Balanced (recommended)
- **6-10 steps** = Slower, better quality
- **11-20 steps** = Slowest, best quality

### 5. Generate Speech
Tap the "Generate Speech" button

You'll see:
```
┌───────────────────────────────────┐
│      Generating...                │
└───────────────────────────────────┘

Status: Generating speech...
```

Wait 5-15 seconds...

```
Status: Playing audio...
```

Audio plays automatically!

### 6. Stop Audio (If Needed)
If audio is playing, you'll see:
```
┌───────────────────────────────────┐
│      Stop Audio                   │
└───────────────────────────────────┘
```

Tap to stop playback.

## Status Messages

### Normal Operation
- **"Initializing TTS engine..."** - Loading models (first launch)
- **"Ready"** - Ready to generate speech
- **"Generating speech..."** - Creating audio
- **"Playing audio..."** - Audio is playing

### Errors
- **"Please enter some text"** - Text field is empty
- **"Please select a voice style"** - No voice selected
- **"Error: [message]"** - Something went wrong

## Tips & Tricks

### For Faster Generation
1. Use 2-3 denoising steps
2. Increase speed to 1.3-1.5x
3. Keep text under 200 characters
4. Use simple sentences

### For Better Quality
1. Use 8-10 denoising steps
2. Keep speed at 1.0x
3. Use proper punctuation
4. Break long text into paragraphs

### Voice Characteristics
- **F1** - Clear female voice, neutral tone
- **F2** - Softer female voice, warm tone
- **M1** - Deep male voice, authoritative
- **M2** - Lighter male voice, friendly

### Text Formatting
The app handles:
- ✅ Numbers: "123" → "one hundred twenty three"
- ✅ Punctuation: Pauses at periods, commas
- ✅ Long text: Automatically splits into chunks
- ✅ Multiple sentences: Natural pauses between

### Performance Tips
- **First generation**: Takes longer (models loading)
- **Subsequent**: Much faster (models in memory)
- **Long text**: Automatically chunked with pauses
- **Background apps**: Close them for better performance

## Example Use Cases

### 1. Quick Test
```
Text: "Hello, this is a test."
Voice: F1.json
Speed: 1.05
Steps: 5
Time: ~5 seconds
```

### 2. Reading Article
```
Text: [Paste article paragraph]
Voice: M1.json
Speed: 1.2
Steps: 5
Time: ~10-15 seconds per paragraph
```

### 3. High Quality
```
Text: "Important announcement text"
Voice: F2.json
Speed: 1.0
Steps: 10
Time: ~15-20 seconds
```

### 4. Fast Preview
```
Text: "Quick preview of voice"
Voice: M2.json
Speed: 1.5
Steps: 2
Time: ~3-5 seconds
```

## Troubleshooting

### App Won't Generate
- Check "Status" message for errors
- Ensure text is entered
- Verify voice is selected
- Wait for "Ready" status

### Audio Not Playing
- Check device volume
- Try different voice style
- Restart the app
- Check if other apps can play audio

### Generation Takes Too Long
- Reduce denoising steps to 2-3
- Shorten text
- Close background apps
- Restart device

### App Crashes
- Device may need more RAM
- Try shorter text
- Reduce denoising steps
- Restart app

## Keyboard Shortcuts

When text field is focused:
- **Enter** - New line
- **Backspace** - Delete
- **Select All** - Long press text

## Accessibility

The app supports:
- Large text (system settings)
- Screen readers (TalkBack)
- High contrast mode
- Touch targets (44dp minimum)

## Privacy

✅ **No internet required**
✅ **No data collection**
✅ **No analytics**
✅ **No permissions needed**
✅ **Everything runs locally**

Your text and audio never leave your device!

## Battery Usage

- **Idle**: Minimal battery use
- **Generating**: Moderate CPU usage
- **Playing**: Low battery use
- **Tip**: Generate in batches to save battery

## Storage

- **App size**: ~250-300 MB
- **Runtime**: ~500 MB RAM
- **No cache**: Audio not saved (unless you add that feature)

## Updates

To update the app:
1. Build new APK
2. Install over existing app
3. Your settings are preserved

---

Enjoy your on-device text-to-speech! 🎉
