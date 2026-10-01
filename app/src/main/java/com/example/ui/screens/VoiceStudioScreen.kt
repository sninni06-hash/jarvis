package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.settings.JarvisPreferences
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisDeepBg
import com.example.ui.theme.JarvisGreen
import com.example.ui.theme.JarvisRed
import com.example.voice.TTSManager
import com.example.voice.VoiceProfileConfig
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceStudioScreen(
    prefs: JarvisPreferences,
    ttsManager: TTSManager,
    onBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    var activeTab by remember { mutableStateOf(prefs.activeVoiceIdentity) } // "JARVIS" or "MYRAA"
    var testSentence by remember { mutableStateOf("Pranam Sir, kaise yaad kiya? Bataiye, aaj kya karna hai.") }
    var isPlayingPreview by remember { mutableStateOf(false) }

    // Models: Low-latency conversational models
    val availableModels = listOf(
        "eleven_flash_v2_5" to "Eleven Flash v2.5 (Ultra-low latency ~75ms)",
        "eleven_turbo_v2_5" to "Eleven Turbo v2.5 (High conversational quality)",
        "eleven_multilingual_v2" to "Eleven Multilingual v2 (Rich emotion)"
    )
    var selectedModel by remember { mutableStateOf(prefs.elevenLabsTtsModel) }

    // JARVIS settings
    var jarvisVoiceId by remember { mutableStateOf(prefs.elevenLabsJarvisVoiceId) }
    var jarvisStability by remember { mutableStateOf(prefs.jarvisStability) }
    var jarvisSimilarity by remember { mutableStateOf(prefs.jarvisSimilarity) }
    var jarvisStyle by remember { mutableStateOf(prefs.jarvisStyle) }

    // MYRAA settings
    var myraaVoiceId by remember { mutableStateOf(prefs.elevenLabsMyraaVoiceId) }
    var myraaStability by remember { mutableStateOf(prefs.myraaStability) }
    var myraaSimilarity by remember { mutableStateOf(prefs.myraaSimilarity) }
    var myraaStyle by remember { mutableStateOf(prefs.myraaStyle) }

    var speakerBoost by remember { mutableStateOf(prefs.speakerBoost) }
    var speechSpeed by remember { mutableStateOf(prefs.speechSpeed) }

    fun saveAll() {
        prefs.activeVoiceIdentity = activeTab
        prefs.elevenLabsTtsModel = selectedModel
        prefs.elevenLabsJarvisVoiceId = jarvisVoiceId
        prefs.jarvisStability = jarvisStability
        prefs.jarvisSimilarity = jarvisSimilarity
        prefs.jarvisStyle = jarvisStyle

        prefs.elevenLabsMyraaVoiceId = myraaVoiceId
        prefs.myraaStability = myraaStability
        prefs.myraaSimilarity = myraaSimilarity
        prefs.myraaStyle = myraaStyle

        prefs.speakerBoost = speakerBoost
        prefs.speechSpeed = speechSpeed

        ttsManager.configureVoiceProfiles(
            identity = activeTab,
            rate = speechSpeed,
            jConfig = VoiceProfileConfig(
                voiceId = jarvisVoiceId,
                modelId = selectedModel,
                stability = jarvisStability,
                similarityBoost = jarvisSimilarity,
                style = jarvisStyle,
                useSpeakerBoost = speakerBoost
            ),
            mConfig = VoiceProfileConfig(
                voiceId = myraaVoiceId,
                modelId = selectedModel,
                stability = myraaStability,
                similarityBoost = myraaSimilarity,
                style = myraaStyle,
                useSpeakerBoost = speakerBoost
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(JarvisDeepBg)
    ) {
        TopAppBar(
            title = {
                Text(
                    text = "VOICE STUDIO",
                    color = JarvisCyan,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
            },
            navigationIcon = {
                IconButton(onClick = {
                    saveAll()
                    onBack()
                }, modifier = Modifier.testTag("voice_studio_back_button")) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0F172A))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // IDENTITY SELECTOR (JARVIS vs MYRAA)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        activeTab = "JARVIS"
                        saveAll()
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (activeTab == "JARVIS") JarvisCyan else Color(0xFF1E293B),
                        contentColor = if (activeTab == "JARVIS") Color(0xFF00363A) else Color.White
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("JARVIS (Deep / Calm)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                Button(
                    onClick = {
                        activeTab = "MYRAA"
                        saveAll()
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (activeTab == "MYRAA") Color(0xFFF472B6) else Color(0xFF1E293B),
                        contentColor = if (activeTab == "MYRAA") Color(0xFF4C0519) else Color.White
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("MYRAA (Sweet / Warm)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }

            // TEST PLAYBACK CARD
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0C4A6E)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.GraphicEq, contentDescription = null, tint = JarvisCyan, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "LIVE SPEECH PREVIEW (${activeTab})",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = testSentence,
                        onValueChange = { testSentence = it },
                        label = { Text("Sample Text", color = JarvisCyan) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = JarvisCyan,
                            unfocusedBorderColor = Color(0xFF334155),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                saveAll()
                                isPlayingPreview = true
                                ttsManager.speak(testSentence) {
                                    isPlayingPreview = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = JarvisCyan, contentColor = Color(0xFF00363A)),
                            modifier = Modifier.weight(1f).testTag("preview_voice_button")
                        ) {
                            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isPlayingPreview) "PLAYING..." else "TEST VOICE", fontWeight = FontWeight.Bold)
                        }

                        if (isPlayingPreview) {
                            Button(
                                onClick = {
                                    ttsManager.stop()
                                    isPlayingPreview = false
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = JarvisRed, contentColor = Color.White)
                            ) {
                                Icon(imageVector = Icons.Default.Stop, contentDescription = null)
                            }
                        }
                    }
                }
            }

            // MODEL SELECTOR
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "LOW-LATENCY TTS MODEL",
                        color = JarvisCyan,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    availableModels.forEach { (modelKey, label) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedModel = modelKey
                                    saveAll()
                                }
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = selectedModel == modelKey,
                                onClick = {
                                    selectedModel = modelKey
                                    saveAll()
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = JarvisCyan)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = label, color = Color.White, fontSize = 12.sp)
                        }
                    }
                }
            }

            // ACTIVE PROFILE CONTROLS
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "$activeTab VOICE CONTROLS",
                        color = JarvisCyan,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )

                    val currentVoiceId = if (activeTab == "JARVIS") jarvisVoiceId else myraaVoiceId
                    OutlinedTextField(
                        value = currentVoiceId,
                        onValueChange = {
                            if (activeTab == "JARVIS") jarvisVoiceId = it else myraaVoiceId = it
                            saveAll()
                        },
                        label = { Text("$activeTab ElevenLabs Voice ID", color = Color(0xFF94A3B8)) },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = JarvisCyan, focusedTextColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Stability
                    val currentStability = if (activeTab == "JARVIS") jarvisStability else myraaStability
                    Text("Stability: ${"%.2f".format(currentStability)} (lower = more expressive, higher = more consistent)", color = Color(0xFF94A3B8), fontSize = 11.sp)
                    Slider(
                        value = currentStability,
                        onValueChange = {
                            if (activeTab == "JARVIS") jarvisStability = it else myraaStability = it
                            saveAll()
                        },
                        valueRange = 0.1f..0.9f,
                        colors = SliderDefaults.colors(thumbColor = JarvisCyan, activeTrackColor = JarvisCyan)
                    )

                    // Similarity
                    val currentSimilarity = if (activeTab == "JARVIS") jarvisSimilarity else myraaSimilarity
                    Text("Similarity: ${"%.2f".format(currentSimilarity)} (voice accuracy)", color = Color(0xFF94A3B8), fontSize = 11.sp)
                    Slider(
                        value = currentSimilarity,
                        onValueChange = {
                            if (activeTab == "JARVIS") jarvisSimilarity = it else myraaSimilarity = it
                            saveAll()
                        },
                        valueRange = 0.3f..1.0f,
                        colors = SliderDefaults.colors(thumbColor = JarvisCyan, activeTrackColor = JarvisCyan)
                    )

                    // Style
                    val currentStyle = if (activeTab == "JARVIS") jarvisStyle else myraaStyle
                    Text("Style Exaggeration: ${"%.2f".format(currentStyle)}", color = Color(0xFF94A3B8), fontSize = 11.sp)
                    Slider(
                        value = currentStyle,
                        onValueChange = {
                            if (activeTab == "JARVIS") jarvisStyle = it else myraaStyle = it
                            saveAll()
                        },
                        valueRange = 0.0f..0.5f,
                        colors = SliderDefaults.colors(thumbColor = JarvisCyan, activeTrackColor = JarvisCyan)
                    )

                    // Speed
                    Text("Speech Speed: ${"%.2f".format(speechSpeed)}x", color = Color(0xFF94A3B8), fontSize = 11.sp)
                    Slider(
                        value = speechSpeed,
                        onValueChange = {
                            speechSpeed = it
                            saveAll()
                        },
                        valueRange = 0.8f..1.4f,
                        colors = SliderDefaults.colors(thumbColor = JarvisCyan, activeTrackColor = JarvisCyan)
                    )

                    // Speaker Boost
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Use Speaker Boost", color = Color.White, fontSize = 13.sp)
                        Switch(
                            checked = speakerBoost,
                            onCheckedChange = {
                                speakerBoost = it
                                saveAll()
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = JarvisCyan, checkedTrackColor = Color(0xFF0C4A6E))
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
