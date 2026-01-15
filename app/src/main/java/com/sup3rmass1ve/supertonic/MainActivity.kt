package com.sup3rmass1ve.supertonic

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.Image
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.util.Locale
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sup3rmass1ve.supertonic.ui.theme.*
import com.sup3rmass1ve.supertonic.viewmodel.TTSViewModel
import com.sup3rmass1ve.supertonic.preferences.ThemePreferences
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            val themePreferences = remember { ThemePreferences(context) }
            val isDarkMode by themePreferences.isDarkMode.collectAsState(initial = true)
            val scope = rememberCoroutineScope()
            
            SupertonicTheme(darkTheme = isDarkMode) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    TTSScreen(
                        isDarkMode = isDarkMode,
                        onThemeToggle = {
                            scope.launch {
                                themePreferences.setDarkMode(!isDarkMode)
                            }
                        }
                    )
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
fun GlassCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.shadow(
            elevation = 8.dp,
            shape = RoundedCornerShape(24.dp),
            ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
            spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.05f)
                        )
                    ),
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(20.dp),
            content = content
        )
    }
}

@Composable
fun GradientButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    gradient: Brush = Brush.horizontalGradient(listOf(GradientPurpleStart, GradientPurpleEnd)),
    content: @Composable RowScope.() -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(56.dp),
        enabled = enabled,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        contentPadding = PaddingValues(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = if (enabled) gradient else Brush.horizontalGradient(
                        listOf(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.surfaceVariant)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                content = content
            )
        }
    }
}

@Composable
fun SectionHeader(icon: ImageVector, title: String, iconTint: Color = MaterialTheme.colorScheme.primary) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 16.dp)) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(color = iconTint.copy(alpha = 0.15f), shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(22.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
fun InfoChip(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TTSScreen(
    modifier: Modifier = Modifier, 
    viewModel: TTSViewModel = viewModel(),
    isDarkMode: Boolean = true,
    onThemeToggle: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
                        )
                    )
                )
        )
        
        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 48.dp, bottom = 24.dp)
        ) {
            // Hero Header
            Box(modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Image(
                                painter = painterResource(id = R.drawable.logo),
                                contentDescription = "Supertonic Logo",
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(RoundedCornerShape(12.dp))
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text("Supertonic v2", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                                Text("Multilingual AI Voice Synthesis", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
                            }
                        }
                        
                        // Theme Toggle Button
                        Surface(
                            onClick = onThemeToggle,
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = if (isDarkMode) Icons.Default.Star else Icons.Default.Refresh,
                                    contentDescription = null,
                                    tint = if (isDarkMode) Color(0xFFFFC107) else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = if (isDarkMode) "Light" else "Dark",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (uiState.isInitialized) AccentGreen.copy(alpha = 0.15f) else AccentOrange.copy(alpha = 0.15f)
                        ) {
                            Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(8.dp).background(if (uiState.isInitialized) AccentGreen else AccentOrange, CircleShape))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(if (uiState.isInitialized) "Ready" else "Initializing...", style = MaterialTheme.typography.labelMedium, color = if (uiState.isInitialized) AccentGreen else AccentOrange)
                            }
                        }
                        

                    }
                }
            }
            

            // Text Input Card
            GlassCard(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                SectionHeader(Icons.Default.Edit, "Your Text", MaterialTheme.colorScheme.primary)
                OutlinedTextField(
                    value = uiState.inputText,
                    onValueChange = { viewModel.updateText(it) },
                    placeholder = { Text("Type something to convert to speech...", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)) },
                    modifier = Modifier.fillMaxWidth().height(140.dp),
                    enabled = uiState.isInitialized && !uiState.isGenerating,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    )
                )
            }
            
            // Voice Selection Card
            GlassCard(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                SectionHeader(Icons.Default.Person, "Voice Style", MaterialTheme.colorScheme.secondary)
                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded && uiState.isInitialized }) {
                    val displayName = uiState.selectedVoiceStyle.removeSuffix(".json").replace("_", " ")
                    OutlinedTextField(
                        value = displayName,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { Icon(if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, null, tint = MaterialTheme.colorScheme.secondary) },
                        leadingIcon = { Icon(Icons.Default.Person, null, tint = MaterialTheme.colorScheme.secondary) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
                        enabled = uiState.isInitialized && !uiState.isGenerating,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.secondary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        )
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        uiState.voiceStyles.forEach { style ->
                            val displayName = style.removeSuffix(".json").replace("_", " ")
                            
                            DropdownMenuItem(
                                text = { Text(displayName, style = MaterialTheme.typography.bodyMedium) },
                                onClick = { viewModel.updateVoiceStyle(style); expanded = false },
                                leadingIcon = { 
                                    Icon(
                                        Icons.Default.Person, 
                                        null, 
                                        tint = MaterialTheme.colorScheme.secondary, 
                                        modifier = Modifier.size(20.dp)
                                    ) 
                                }
                            )
                        }
                    }
                }
            }
            
            // Language Selection Card
            GlassCard(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                SectionHeader(Icons.Default.Info, "Language", MaterialTheme.colorScheme.tertiary)
                var languageExpanded by remember { mutableStateOf(false) }
                val languages = mapOf(
                    "en" to "English",
                    "ko" to "Korean",
                    "es" to "Spanish",
                    "pt" to "Portuguese",
                    "fr" to "French"
                )
                ExposedDropdownMenuBox(expanded = languageExpanded, onExpandedChange = { languageExpanded = !languageExpanded && uiState.isInitialized }) {
                    OutlinedTextField(
                        value = languages[uiState.selectedLanguage] ?: "English",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { Icon(if (languageExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, null, tint = MaterialTheme.colorScheme.tertiary) },
                        leadingIcon = { Icon(Icons.Default.Info, null, tint = MaterialTheme.colorScheme.tertiary) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
                        enabled = uiState.isInitialized && !uiState.isGenerating,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.tertiary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        )
                    )
                    ExposedDropdownMenu(expanded = languageExpanded, onDismissRequest = { languageExpanded = false }) {
                        languages.forEach { (code, name) ->
                            DropdownMenuItem(
                                text = { Text(name, style = MaterialTheme.typography.bodyMedium) },
                                onClick = { viewModel.updateLanguage(code); languageExpanded = false },
                                leadingIcon = { 
                                    Icon(
                                        Icons.Default.Info, 
                                        null, 
                                        tint = MaterialTheme.colorScheme.tertiary, 
                                        modifier = Modifier.size(20.dp)
                                    ) 
                                }
                            )
                        }
                    }
                }
            }
            
            // Settings Card
            GlassCard(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                SectionHeader(Icons.Default.Settings, "Settings", MaterialTheme.colorScheme.tertiary)
                
                // Speed Control
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.PlayArrow, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Speed", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                    }
                    Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)) {
                        Text("${"%.2f".format(Locale.US, uiState.speed)}x", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp))
                    }
                }
                Slider(
                    value = uiState.speed,
                    onValueChange = { viewModel.updateSpeed(it) },
                    valueRange = 0.5f..2.0f,
                    steps = 29,
                    enabled = uiState.isInitialized && !uiState.isGenerating,
                    modifier = Modifier.fillMaxWidth(),
                    colors = SliderDefaults.colors(thumbColor = MaterialTheme.colorScheme.primary, activeTrackColor = MaterialTheme.colorScheme.primary, inactiveTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                )
                
                // Quality Steps Control
                Spacer(modifier = Modifier.height(20.dp))
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Quality", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                        }
                        Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)) {
                            Text("${uiState.denoisingSteps} steps", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary, modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp))
                        }
                    }
                    Slider(
                        value = uiState.denoisingSteps.toFloat(),
                        onValueChange = { viewModel.updateSteps(it.toInt()) },
                        valueRange = 1f..20f,
                        steps = 18,
                        enabled = uiState.isInitialized && !uiState.isGenerating,
                        modifier = Modifier.fillMaxWidth(),
                        colors = SliderDefaults.colors(thumbColor = MaterialTheme.colorScheme.secondary, activeTrackColor = MaterialTheme.colorScheme.secondary, inactiveTrackColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f))
                    )
                    
                    Spacer(modifier = Modifier.height(20.dp))
                
                // Seed Input
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 8.dp)) {
                        Icon(Icons.Default.Refresh, null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Seed (optional)", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                    }
                    OutlinedTextField(
                        value = uiState.seed?.toString() ?: "",
                        onValueChange = { viewModel.updateSeed(it.toLongOrNull()) },
                        placeholder = { Text("Random", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = uiState.isInitialized && !uiState.isGenerating,
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.tertiary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        )
                    )
            }
            
            // Generate Button
            GradientButton(
                onClick = { viewModel.generateSpeech() },
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                enabled = uiState.isInitialized && !uiState.isGenerating,
                gradient = Brush.horizontalGradient(listOf(GradientPurpleStart, GradientCyanStart))
            ) {
                if (uiState.isGenerating) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Generating...", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = Color.White)
                } else {
                    Icon(Icons.Default.PlayArrow, null, Modifier.size(24.dp), tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Generate Speech", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = Color.White)
                }
            }

            
            // Audio Player Card
            AnimatedVisibility(visible = uiState.generatedAudio != null, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
                GlassCard(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                    SectionHeader(Icons.Default.Notifications, "Audio Player", MaterialTheme.colorScheme.primary)
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        val progress = if (uiState.playbackDuration > 0) uiState.playbackPosition.toFloat() / uiState.playbackDuration.toFloat() else 0f
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(progress)
                                .background(brush = Brush.horizontalGradient(listOf(MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f))))
                        )
                        Row(
                            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(formatTime(uiState.playbackPosition, uiState.sampleRate), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Text(formatTime(uiState.playbackDuration, uiState.sampleRate), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    
                    Slider(
                        value = if (uiState.playbackDuration > 0) uiState.playbackPosition.toFloat() / uiState.playbackDuration.toFloat() else 0f,
                        onValueChange = { viewModel.seekTo((it * uiState.playbackDuration).toInt()) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = SliderDefaults.colors(thumbColor = MaterialTheme.colorScheme.primary, activeTrackColor = MaterialTheme.colorScheme.primary, inactiveTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(
                            onClick = { viewModel.playPauseAudio() },
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(if (uiState.isPlaying) Icons.Default.Clear else Icons.Default.PlayArrow, null, Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (uiState.isPlaying) "Pause" else "Play", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                        }
                        OutlinedButton(
                            onClick = { viewModel.stopAudio() },
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Close, null, Modifier.size(24.dp), tint = MaterialTheme.colorScheme.onSurface)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Stop", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    Text("Export Format", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        com.sup3rmass1ve.supertonic.audio.AudioFormat.entries.forEach { format ->
                            FilterChip(
                                selected = uiState.selectedAudioFormat == format,
                                onClick = { viewModel.updateAudioFormat(format) },
                                label = { Text(format.extension.uppercase(), fontWeight = if (uiState.selectedAudioFormat == format) FontWeight.Bold else FontWeight.Normal) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), selectedLabelColor = MaterialTheme.colorScheme.primary)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    GradientButton(
                        onClick = { viewModel.saveAudio() },
                        modifier = Modifier.fillMaxWidth(),
                        gradient = Brush.horizontalGradient(listOf(GradientOrangeStart, GradientPinkStart))
                    ) {
                        Icon(Icons.Default.Check, null, Modifier.size(20.dp), tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Save as ${uiState.selectedAudioFormat.extension.uppercase()}", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, color = Color.White)
                    }
                }
            }
            
            // Generation Info Card
            AnimatedVisibility(visible = uiState.generationInfo != null, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
                uiState.generationInfo?.let { info ->
                    GlassCard(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                        SectionHeader(Icons.Default.Info, "Generation Stats", MaterialTheme.colorScheme.tertiary)
                        
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            InfoChip("Duration", "${"%.1f".format(Locale.US, info.audioDuration)}s", Modifier.weight(1f))
                            InfoChip("Gen Time", "${"%.1f".format(Locale.US, info.generationTime)}s", Modifier.weight(1f))
                            InfoChip("Quality", "${info.denoisingSteps}", Modifier.weight(1f))
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            InfoChip("Speed", "${"%.1f".format(Locale.US, info.speed)}x", Modifier.weight(1f))
                            InfoChip("Sample Rate", "${info.sampleRate / 1000}kHz", Modifier.weight(1f))
                            InfoChip("Seed", "${info.seed}", Modifier.weight(1f))
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f), modifier = Modifier.fillMaxWidth()) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Person, null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(info.voiceStyle, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.tertiary)
                            }
                        }
                    }
                }
            }
            
            // Status Card
            Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(uiState.status, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
