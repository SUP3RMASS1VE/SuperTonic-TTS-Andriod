package com.sup3rmass1ve.supertonic

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import java.util.Locale
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sup3rmass1ve.supertonic.ui.theme.SupertonicTheme
import com.sup3rmass1ve.supertonic.viewmodel.TTSViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SupertonicTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    TTSScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

fun formatTime(samples: Int, sampleRate: Int = 22050): String {
    val seconds = samples / sampleRate
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return "%d:%02d".format(minutes, remainingSeconds)
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onTertiaryContainer
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TTSScreen(
    modifier: Modifier = Modifier,
    viewModel: TTSViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Colorful header with gradient
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    modifier = Modifier
                        .size(48.dp)
                        .padding(end = 16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Column {
                    Text(
                        text = "Supertonic TTS",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "AI-Powered Speech Generation",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
            }
        }
        
        // Input Text with colorful card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = "Input Text",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
                OutlinedTextField(
                    value = uiState.inputText,
                    onValueChange = { viewModel.updateText(it) },
                    placeholder = { Text("Enter text to convert to speech...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    enabled = uiState.isInitialized && !uiState.isGenerating,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.secondary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
                    )
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Voice Style Dropdown with colorful card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = "Voice Style",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
                
                var expanded by remember { mutableStateOf(false) }
                
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded && uiState.isInitialized }
                ) {
                    OutlinedTextField(
                        value = uiState.selectedVoiceStyle.removeSuffix(".json"),
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                        enabled = uiState.isInitialized && !uiState.isGenerating,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.tertiary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f)
                        )
                    )
                    
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        uiState.voiceStyles.forEach { style ->
                            DropdownMenuItem(
                                text = { Text(style.removeSuffix(".json")) },
                                onClick = {
                                    viewModel.updateVoiceStyle(style)
                                    expanded = false
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.tertiary
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Settings Card with colorful sliders
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = "Generation Settings",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Speed Slider
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = "Speed: ${"%.2f".format(Locale.US, uiState.speed)}x",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                Slider(
                    value = uiState.speed,
                    onValueChange = { viewModel.updateSpeed(it) },
                    valueRange = 0.5f..2.0f,
                    steps = 29,
                    enabled = uiState.isInitialized && !uiState.isGenerating,
                    modifier = Modifier.fillMaxWidth(),
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.secondary,
                        activeTrackColor = MaterialTheme.colorScheme.secondary
                    )
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Denoising Steps Slider
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = "Quality Steps: ${uiState.denoisingSteps}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
                Slider(
                    value = uiState.denoisingSteps.toFloat(),
                    onValueChange = { viewModel.updateSteps(it.toInt()) },
                    valueRange = 1f..20f,
                    steps = 18,
                    enabled = uiState.isInitialized && !uiState.isGenerating,
                    modifier = Modifier.fillMaxWidth(),
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.tertiary,
                        activeTrackColor = MaterialTheme.colorScheme.tertiary
                    )
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Seed Input
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = "Seed (optional, for reproducibility)",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                OutlinedTextField(
                    value = uiState.seed?.toString() ?: "",
                    onValueChange = { 
                        val seed = it.toLongOrNull()
                        viewModel.updateSeed(seed)
                    },
                    placeholder = { Text("Leave empty for random") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = uiState.isInitialized && !uiState.isGenerating,
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    )
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Generate Button with icon
        Button(
            onClick = { viewModel.generateSpeech() },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = uiState.isInitialized && !uiState.isGenerating,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = null,
                modifier = Modifier.padding(end = 8.dp)
            )
            Text(
                text = if (uiState.isGenerating) "Generating..." else "Generate Speech",
                style = MaterialTheme.typography.titleMedium
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Audio Player Controls
        if (uiState.generatedAudio != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = "Audio Player",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    // Playback time
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = formatTime(uiState.playbackPosition, uiState.sampleRate),
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = formatTime(uiState.playbackDuration, uiState.sampleRate),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    
                    // Seek bar
                    Slider(
                        value = if (uiState.playbackDuration > 0) {
                            uiState.playbackPosition.toFloat() / uiState.playbackDuration.toFloat()
                        } else 0f,
                        onValueChange = { progress ->
                            val newPosition = (progress * uiState.playbackDuration).toInt()
                            viewModel.seekTo(newPosition)
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Control buttons with icons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
                    ) {
                        FilledTonalButton(
                            onClick = { viewModel.playPauseAudio() },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = MaterialTheme.colorScheme.secondary
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (uiState.isPlaying) "Pause" else "Play")
                        }
                        
                        OutlinedButton(
                            onClick = { viewModel.stopAudio() },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.tertiary
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Stop")
                        }
                        
                        FilledTonalButton(
                            onClick = { viewModel.saveAudio() },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = MaterialTheme.colorScheme.tertiary
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Save")
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Generation Info Card
            uiState.generationInfo?.let { info ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text(
                                text = "Generation Info",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                        
                        // Info rows
                        InfoRow("Voice Style", info.voiceStyle)
                        InfoRow("Speed", "${"%.2f".format(Locale.US, info.speed)}x")
                        InfoRow("Quality Steps", "${info.denoisingSteps}")
                        InfoRow("Seed", "${info.seed}")
                        InfoRow("Audio Duration", "${"%.2f".format(Locale.US, info.audioDuration)}s")
                        InfoRow("Generation Time", "${"%.2f".format(Locale.US, info.generationTime)}s")
                        InfoRow("Sample Rate", "${info.sampleRate} Hz")
                        InfoRow("Audio Samples", "${info.audioSamples}")
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Status with icon
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(end = 12.dp)
                )
                Text(
                    text = uiState.status,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}
