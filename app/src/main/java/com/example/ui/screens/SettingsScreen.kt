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
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
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
import com.example.ai.GeminiProvider
import com.example.ai.OpenAIProvider
import com.example.core.performance.PerformanceMode
import com.example.settings.JarvisPreferences
import com.example.settings.Personality
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisDeepBg
import com.example.ui.theme.JarvisGold
import com.example.ui.theme.JarvisGreen
import com.example.ui.theme.JarvisRed
import com.example.voice.ElevenLabsProvider
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    prefs: JarvisPreferences,
    geminiProvider: GeminiProvider,
    openAIProvider: OpenAIProvider,
    elevenLabsProvider: ElevenLabsProvider,
    onOpenPermissionCenter: () -> Unit,
    onOpenSystemHealth: () -> Unit,
    onOpenVoiceStudio: () -> Unit,
    onBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    var userName by remember { mutableStateOf(prefs.userName) }
    var assistantIdentity by remember { mutableStateOf(prefs.assistantIdentity) }
    var activeVoiceIdentity by remember { mutableStateOf(prefs.activeVoiceIdentity) }
    var customWakeName by remember { mutableStateOf(prefs.customWakeName) }
    var continuousListening by remember { mutableStateOf(prefs.isContinuousListening) }
    var clapDeactivation by remember { mutableStateOf(prefs.isClapDeactivationEnabled) }
    var floatingIcon by remember { mutableStateOf(prefs.isFloatingIconEnabled) }
    var speechSpeed by remember { mutableStateOf(prefs.speechSpeed) }
    var personality by remember { mutableStateOf(prefs.personality) }
    var performanceMode by remember { mutableStateOf(prefs.performanceMode) }

    // Gemini states
    var geminiKey by remember { mutableStateOf(prefs.geminiApiKey) }
    var geminiModel by remember { mutableStateOf(prefs.geminiModel) }
    var geminiStatus by remember { mutableStateOf(prefs.geminiStatus) }
    var isTestingGemini by remember { mutableStateOf(false) }

    // OpenAI states
    var openAiKey by remember { mutableStateOf(prefs.openAiApiKey) }
    var openAiModel by remember { mutableStateOf(prefs.openAiModel) }
    var openAiStatus by remember { mutableStateOf(prefs.openAiStatus) }
    var isTestingOpenAi by remember { mutableStateOf(false) }

    // ElevenLabs states
    var elevenKey by remember { mutableStateOf(prefs.elevenLabsApiKey) }
    var jarvisVid by remember { mutableStateOf(prefs.elevenLabsJarvisVoiceId) }
    var myraaVid by remember { mutableStateOf(prefs.elevenLabsMyraaVoiceId) }
    var elevenStatus by remember { mutableStateOf(prefs.elevenLabsStatus) }
    var isTestingEleven by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(JarvisDeepBg)
    ) {
        TopAppBar(
            title = {
                Text(
                    text = "SETTINGS & CONTROL CENTER",
                    color = JarvisCyan,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
            },
            navigationIcon = {
                IconButton(onClick = onBack, modifier = Modifier.testTag("settings_back_button")) {
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
            // VOICE STUDIO PROMINENT BANNER
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenVoiceStudio() }
                    .testTag("voice_studio_card"),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1B4B)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.GraphicEq, contentDescription = null, tint = Color(0xFFA78BFA), modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("VOICE STUDIO", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp, fontFamily = FontFamily.Monospace)
                            Text("Test & tune JARVIS & MYRAA voices, speed, stability", color = Color(0xFFC7D2FE), fontSize = 11.sp)
                        }
                    }
                    Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFFA78BFA))
                }
            }

            // SYSTEM HEALTH & PERMISSIONS QUICK NAVIGATION
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onOpenSystemHealth() }
                        .testTag("system_health_card"),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0C4A6E)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.HealthAndSafety, contentDescription = null, tint = JarvisCyan, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("HEALTH", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Run full diagnostic", color = Color(0xFFBAE6FD), fontSize = 11.sp)
                    }
                }

                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onOpenPermissionCenter() }
                        .testTag("permission_center_card"),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = JarvisGreen, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("PERMISSIONS", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Mic, phone, screen", color = Color(0xFF94A3B8), fontSize = 11.sp)
                    }
                }
            }

            // USER PROFILE & ONBOARDING NAME
            SettingsCard(title = "USER IDENTITY", icon = Icons.Default.Person) {
                OutlinedTextField(
                    value = userName,
                    onValueChange = {
                        userName = it
                        prefs.userName = it
                    },
                    label = { Text("What should I call you?", color = JarvisCyan) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = JarvisCyan,
                        unfocusedBorderColor = Color(0xFF334155),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // ASSISTANT IDENTITY & ACTIVE VOICE
            SettingsCard(title = "ASSISTANT IDENTITY & VOICE", icon = Icons.Default.SmartToy) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Active Assistant Voice Identity:", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = {
                                activeVoiceIdentity = "JARVIS"
                                prefs.activeVoiceIdentity = "JARVIS"
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (activeVoiceIdentity == "JARVIS") JarvisCyan else Color(0xFF1E293B),
                                contentColor = if (activeVoiceIdentity == "JARVIS") Color(0xFF00363A) else Color.White
                            )
                        ) {
                            Text("JARVIS (Deep/Calm)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                activeVoiceIdentity = "MYRAA"
                                prefs.activeVoiceIdentity = "MYRAA"
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (activeVoiceIdentity == "MYRAA") Color(0xFFF472B6) else Color(0xFF1E293B),
                                contentColor = if (activeVoiceIdentity == "MYRAA") Color(0xFF4C0519) else Color.White
                            )
                        ) {
                            Text("MYRAA (Sweet/Warm)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Wake Word Mode:", color = Color(0xFF94A3B8), fontSize = 12.sp)
                    listOf("BOTH" to "Both ('JARVIS' and 'MYRAA')", "JARVIS_ONLY" to "JARVIS only", "MYRAA_ONLY" to "MYRAA only", "CUSTOM" to "Custom Wake Name").forEach { (key, label) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    assistantIdentity = key
                                    prefs.assistantIdentity = key
                                }
                                .padding(vertical = 2.dp)
                        ) {
                            RadioButton(
                                selected = assistantIdentity == key,
                                onClick = {
                                    assistantIdentity = key
                                    prefs.assistantIdentity = key
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = JarvisCyan)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = label, color = Color.White, fontSize = 13.sp)
                        }
                    }

                    if (assistantIdentity == "CUSTOM") {
                        OutlinedTextField(
                            value = customWakeName,
                            onValueChange = {
                                customWakeName = it
                                prefs.customWakeName = it
                            },
                            label = { Text("Custom Wake Name (e.g. Friday)", color = JarvisCyan) },
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = JarvisCyan, focusedTextColor = Color.White),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Speech Rate: ${"%.2f".format(speechSpeed)}x", color = Color(0xFF94A3B8), fontSize = 12.sp)
                    Slider(
                        value = speechSpeed,
                        onValueChange = {
                            speechSpeed = it
                            prefs.speechSpeed = it
                        },
                        valueRange = 0.75f..1.5f,
                        colors = SliderDefaults.colors(thumbColor = JarvisCyan, activeTrackColor = JarvisCyan)
                    )
                }
            }

            // GEMINI API CONFIGURATION
            SettingsCard(title = "GOOGLE GEMINI CONFIGURATION", icon = Icons.Default.Psychology) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = geminiKey,
                        onValueChange = {
                            geminiKey = it
                            prefs.geminiApiKey = it
                        },
                        label = { Text("Gemini API Key", color = JarvisCyan) },
                        placeholder = { Text("AIzaSy...", color = Color(0xFF64748B)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = JarvisCyan,
                            unfocusedBorderColor = Color(0xFF334155),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Model: $geminiModel", color = Color(0xFF94A3B8), fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                        Text(
                            text = geminiStatus,
                            color = if (geminiStatus.contains("Connected", true)) JarvisGreen else JarvisGold,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    isTestingGemini = true
                                    val (success, msg) = geminiProvider.testConnection(geminiKey, geminiModel)
                                    geminiStatus = msg
                                    prefs.geminiStatus = msg
                                    if (success) {
                                        prefs.geminiApiKey = geminiKey
                                        prefs.primaryAIProvider = "gemini"
                                    }
                                    isTestingGemini = false
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = JarvisCyan, contentColor = Color(0xFF00363A))
                        ) {
                            if (isTestingGemini) {
                                CircularProgressIndicator(color = Color(0xFF00363A), modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            } else {
                                Text("TEST & SAVE", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }

                        OutlinedButton(
                            onClick = {
                                geminiKey = ""
                                prefs.geminiApiKey = ""
                                geminiStatus = "KEY_REMOVED"
                                prefs.geminiStatus = "KEY_REMOVED"
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = JarvisRed)
                        ) {
                            Text("REMOVE", fontSize = 12.sp)
                        }
                    }
                }
            }

            // OPENAI / CHATGPT CONFIGURATION
            SettingsCard(title = "OPENAI / CHATGPT (FAILOVER)", icon = Icons.Default.Psychology) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = openAiKey,
                        onValueChange = {
                            openAiKey = it
                            prefs.openAiApiKey = it
                        },
                        label = { Text("OpenAI API Key (sk-...)", color = JarvisCyan) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = JarvisCyan,
                            unfocusedBorderColor = Color(0xFF334155),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Model: $openAiModel", color = Color(0xFF94A3B8), fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                        Text(
                            text = openAiStatus,
                            color = if (openAiStatus.contains("Connected", true)) JarvisGreen else JarvisGold,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    isTestingOpenAi = true
                                    val (success, msg) = openAIProvider.testConnection(openAiKey, openAiModel)
                                    openAiStatus = msg
                                    prefs.openAiStatus = msg
                                    if (success) {
                                        prefs.openAiApiKey = openAiKey
                                    }
                                    isTestingOpenAi = false
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981), contentColor = Color.White)
                        ) {
                            if (isTestingOpenAi) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            } else {
                                Text("TEST & SAVE", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }

                        OutlinedButton(
                            onClick = {
                                openAiKey = ""
                                prefs.openAiApiKey = ""
                                openAiStatus = "KEY_REMOVED"
                                prefs.openAiStatus = "KEY_REMOVED"
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = JarvisRed)
                        ) {
                            Text("REMOVE", fontSize = 12.sp)
                        }
                    }
                }
            }

            // ELEVENLABS NATURAL VOICE CONFIGURATION
            SettingsCard(title = "ELEVENLABS VOICE ENGINE", icon = Icons.AutoMirrored.Filled.VolumeUp) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = elevenKey,
                        onValueChange = {
                            elevenKey = it
                            prefs.elevenLabsApiKey = it
                        },
                        label = { Text("ElevenLabs API Key", color = JarvisCyan) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = JarvisCyan,
                            unfocusedBorderColor = Color(0xFF334155),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = jarvisVid,
                        onValueChange = {
                            jarvisVid = it
                            prefs.elevenLabsJarvisVoiceId = it
                        },
                        label = { Text("JARVIS Voice ID", color = Color(0xFF94A3B8)) },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = JarvisCyan, focusedTextColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = myraaVid,
                        onValueChange = {
                            myraaVid = it
                            prefs.elevenLabsMyraaVoiceId = it
                        },
                        label = { Text("MYRAA Voice ID", color = Color(0xFF94A3B8)) },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = JarvisCyan, focusedTextColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = elevenStatus,
                        color = if (elevenStatus.contains("Connected", true)) JarvisGreen else JarvisGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    isTestingEleven = true
                                    val (success, msg) = elevenLabsProvider.testConnection(elevenKey)
                                    elevenStatus = msg
                                    prefs.elevenLabsStatus = msg
                                    if (success) {
                                        prefs.elevenLabsApiKey = elevenKey
                                    }
                                    isTestingEleven = false
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6), contentColor = Color.White)
                        ) {
                            if (isTestingEleven) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            } else {
                                Text("TEST & SAVE", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }

                        OutlinedButton(
                            onClick = {
                                elevenKey = ""
                                prefs.elevenLabsApiKey = ""
                                elevenStatus = "KEY_REMOVED"
                                prefs.elevenLabsStatus = "KEY_REMOVED"
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = JarvisRed)
                        ) {
                            Text("REMOVE", fontSize = 12.sp)
                        }
                    }
                }
            }

            // HARDWARE & PERFORMANCE PROFILE (GALAXY J7 PRIME & OLD DEVICES)
            SettingsCard(title = "ADAPTIVE PERFORMANCE (HARDWARE)", icon = Icons.Default.Speed) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Optimizes rendering & frame sampling for low-end chipsets:", color = Color(0xFF94A3B8), fontSize = 12.sp)
                    listOf(
                        PerformanceMode.PERFORMANCE to "Performance Mode (Low-End / Galaxy J7 / Exynos)",
                        PerformanceMode.BALANCED to "Balanced Mode (Standard Phones)",
                        PerformanceMode.QUALITY to "Quality Mode (Flagship High-FPS Galaxy)"
                    ).forEach { (mode, label) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    performanceMode = mode
                                    prefs.performanceMode = mode
                                }
                                .padding(vertical = 3.dp)
                        ) {
                            RadioButton(
                                selected = performanceMode == mode,
                                onClick = {
                                    performanceMode = mode
                                    prefs.performanceMode = mode
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = JarvisCyan)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = label, color = Color.White, fontSize = 13.sp)
                        }
                    }
                }
            }

            // PERSONALITY MODES
            SettingsCard(title = "ASSISTANT PERSONALITY", icon = Icons.Default.Tune) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Personality.values().forEach { p ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    personality = p
                                    prefs.personality = p
                                }
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = personality == p,
                                onClick = {
                                    personality = p
                                    prefs.personality = p
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = JarvisCyan)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(text = p.title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text(text = p.description, color = Color(0xFF94A3B8), fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            // CONTINUOUS LISTENING & FLOATING
            SettingsCard(title = "AUTOMATION & ASSISTANT BEHAVIOR", icon = Icons.Default.Widgets) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SettingToggle(
                        title = "Continuous Conversation Mode",
                        subtitle = "Assistant automatically listens again after answering",
                        checked = continuousListening,
                        onCheckedChange = {
                            continuousListening = it
                            prefs.isContinuousListening = it
                        }
                    )

                    SettingToggle(
                        title = "Clap Deactivation Trigger",
                        subtitle = "Quickly deactivate assistant with a sharp hand clap",
                        checked = clapDeactivation,
                        onCheckedChange = {
                            clapDeactivation = it
                            prefs.isClapDeactivationEnabled = it
                        }
                    )

                    SettingToggle(
                        title = "Floating Assistant Bubble",
                        subtitle = "Display small draggable assistant node over other applications",
                        checked = floatingIcon,
                        onCheckedChange = {
                            floatingIcon = it
                            prefs.isFloatingIconEnabled = it
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun SettingsCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = JarvisCyan,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    color = JarvisCyan,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
fun SettingToggle(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Text(text = subtitle, color = Color(0xFF94A3B8), fontSize = 12.sp)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = JarvisCyan,
                checkedTrackColor = Color(0xFF0C4A6E),
                uncheckedThumbColor = Color(0xFF64748B),
                uncheckedTrackColor = Color(0xFF1E293B)
            )
        )
    }
}
