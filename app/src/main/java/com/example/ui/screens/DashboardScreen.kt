package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.ScreenShare
import androidx.compose.material.icons.automirrored.filled.StopScreenShare
import androidx.compose.material.icons.automirrored.filled.VolumeMute
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.core.performance.PerformanceMode
import com.example.planner.models.TaskPlan
import com.example.planner.models.VerificationStatus
import com.example.ui.components.VoiceVisualizer
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisDeepBg
import com.example.ui.theme.JarvisGold
import com.example.ui.theme.JarvisGreen
import com.example.ui.theme.JarvisRed
import com.example.ui.viewmodel.AssistantState
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    assistantState: AssistantState,
    isMuted: Boolean,
    statusMessage: String,
    lastUserTranscript: String,
    batteryPercentage: Int,
    isCharging: Boolean,
    networkStatus: String,
    rmsDb: Float,
    currentPlan: TaskPlan?,
    assistantIdentity: String,
    userName: String,
    isScreenShareActive: Boolean,
    performanceMode: PerformanceMode,
    onToggleActive: () -> Unit,
    onToggleMute: () -> Unit,
    onMicClick: () -> Unit,
    onToggleScreenShare: () -> Unit,
    onCancelPlan: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenChat: () -> Unit
) {
    var currentTime by remember { mutableStateOf("") }
    val isMyraa = assistantIdentity.equals("MYRAA", ignoreCase = true) || assistantIdentity.equals("MYRAA_ONLY", ignoreCase = true)

    LaunchedEffect(Unit) {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        while (true) {
            currentTime = sdf.format(Date())
            delay(1000)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(JarvisDeepBg)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // TOP SECTION: BRANDING & CONTROLS
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_jarvis_logo),
                        contentDescription = "Logo",
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (isMyraa) "MYRAA" else "JARVIS",
                            color = JarvisCyan,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "$userName • ONLINE",
                            color = Color(0xFF64748B),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Screen Share Quick Toggle
                    IconButton(
                        onClick = onToggleScreenShare,
                        modifier = Modifier.testTag("nav_screen_share_button")
                    ) {
                        Icon(
                            imageVector = if (isScreenShareActive) Icons.AutoMirrored.Filled.ScreenShare else Icons.AutoMirrored.Filled.StopScreenShare,
                            contentDescription = "Screen Share",
                            tint = if (isScreenShareActive) JarvisGreen else Color(0xFF94A3B8)
                        )
                    }

                    // Terminal Chat Switcher
                    IconButton(
                        onClick = onOpenChat,
                        modifier = Modifier.testTag("nav_chat_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Chat,
                            contentDescription = "Terminal Mode",
                            tint = JarvisCyan
                        )
                    }

                    // Settings
                    IconButton(
                        onClick = onOpenSettings,
                        modifier = Modifier.testTag("nav_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = Color(0xFF94A3B8)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // STATUS & TELEMETRY ROW
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0F172A), RoundedCornerShape(12.dp))
                    .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(12.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Live Status Badge
                val (badgeBg, badgeText, badgeColor) = when (assistantState) {
                    AssistantState.DEACTIVATED -> Triple(Color(0xFF334155), "PAUSED", Color(0xFF94A3B8))
                    AssistantState.LISTENING -> Triple(JarvisGreen.copy(alpha = 0.2f), "LISTENING", JarvisGreen)
                    AssistantState.PLANNING -> Triple(JarvisCyan.copy(alpha = 0.2f), "THINKING", JarvisCyan)
                    AssistantState.EXECUTING -> Triple(JarvisCyan.copy(alpha = 0.2f), "EXECUTING", JarvisCyan)
                    AssistantState.VERIFYING -> Triple(Color(0xFF8B5CF6).copy(alpha = 0.2f), "VERIFYING", Color(0xFFA78BFA))
                    AssistantState.RESPONDING -> Triple(JarvisCyan.copy(alpha = 0.2f), "SPEAKING", JarvisCyan)
                    AssistantState.ERROR -> Triple(JarvisRed.copy(alpha = 0.2f), "ALERT", JarvisRed)
                    else -> Triple(JarvisGreen.copy(alpha = 0.2f), "ACTIVE", JarvisGreen)
                }

                Box(
                    modifier = Modifier
                        .background(badgeBg, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = badgeText,
                        color = badgeColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Text(
                    text = currentTime,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Medium
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.BatteryChargingFull,
                        contentDescription = "Battery",
                        tint = if (batteryPercentage > 20) JarvisGold else JarvisRed,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$batteryPercentage%${if (isCharging) "+" else ""}",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Wifi,
                        contentDescription = "Network",
                        tint = if (networkStatus == "ONLINE") JarvisCyan else JarvisRed,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = networkStatus,
                        color = if (networkStatus == "ONLINE") JarvisCyan else JarvisRed,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // SCREEN SHARE ACTIVE BANNER (When screen understanding is engaged)
            AnimatedVisibility(visible = isScreenShareActive) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(JarvisGreen.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                        .border(1.dp, JarvisGreen.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ScreenShare,
                        contentDescription = null,
                        tint = JarvisGreen,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "LIVE SCREEN SHARING ACTIVE",
                        color = JarvisGreen,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // CENTER: ANIMATED AI CORE (GALAXY / ENERGY WAVEFORM)
        Box(
            modifier = Modifier
                .size(270.dp)
                .clickable { onMicClick() }
                .testTag("central_voice_hub"),
            contentAlignment = Alignment.Center
        ) {
            VoiceVisualizer(
                assistantState = assistantState,
                rmsDb = rmsDb,
                isMyraa = isMyraa,
                performanceMode = performanceMode,
                modifier = Modifier.fillMaxSize()
            )

            // Center interactive microphone node
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .background(Color(0xFF0F172A), CircleShape)
                    .border(2.dp, if (assistantState == AssistantState.LISTENING) JarvisGreen else JarvisCyan, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (assistantState == AssistantState.LISTENING) Icons.Default.Mic else Icons.Default.MicOff,
                    contentDescription = "Microphone",
                    tint = if (assistantState == AssistantState.LISTENING) JarvisGreen else JarvisCyan,
                    modifier = Modifier.size(34.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // BOTTOM: TRANSCRIPT, RESPONSE & TASK GRAPH
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Live User Speech Transcript
            AnimatedVisibility(visible = lastUserTranscript.isNotBlank()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0C4A6E)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "YOU: ",
                            color = JarvisCyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = lastUserTranscript,
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            // Current Assistant Spoken Message Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = statusMessage,
                        color = Color(0xFFF1F5F9),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 21.sp
                    )
                }
            }

            // MULTI-TASK PROGRESS TRACKER (When complex multi-step plan is active)
            AnimatedVisibility(visible = currentPlan != null) {
                currentPlan?.let { plan ->
                    Spacer(modifier = Modifier.height(10.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF131D33)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "MULTI-TASK ORCHESTRATION",
                                    color = JarvisCyan,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                IconButton(onClick = onCancelPlan, modifier = Modifier.size(24.dp)) {
                                    Icon(
                                        imageVector = Icons.Default.Cancel,
                                        contentDescription = "Cancel Plan",
                                        tint = JarvisRed
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            plan.steps.forEach { step ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val (icon, tint) = when (step.status) {
                                        VerificationStatus.SUCCESS -> Pair(Icons.Default.CheckCircle, JarvisGreen)
                                        VerificationStatus.EXECUTING -> Pair(Icons.Default.Refresh, JarvisCyan)
                                        VerificationStatus.FAILED -> Pair(Icons.Default.Cancel, JarvisRed)
                                        else -> Pair(Icons.Default.CheckCircle, Color(0xFF64748B))
                                    }

                                    Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "${step.stepNumber}/${step.totalSteps}: ${step.title}",
                                        color = if (step.status == VerificationStatus.EXECUTING) Color.White else Color(0xFF94A3B8),
                                        fontSize = 12.sp,
                                        fontWeight = if (step.status == VerificationStatus.EXECUTING) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // BOTTOM MINIMAL CONTROLS (ACTIVE / PAUSE & MUTE)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val isActive = assistantState != AssistantState.DEACTIVATED
                Button(
                    onClick = onToggleActive,
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                        .testTag("active_deactivate_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isActive) JarvisGreen.copy(alpha = 0.2f) else JarvisRed.copy(alpha = 0.2f),
                        contentColor = if (isActive) JarvisGreen else JarvisRed
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = if (isActive) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Toggle Active",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isActive) "ACTIVE" else "RESUME",
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp
                    )
                }

                Button(
                    onClick = onToggleMute,
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                        .testTag("mute_toggle_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isMuted) JarvisRed.copy(alpha = 0.2f) else Color(0xFF1E293B),
                        contentColor = if (isMuted) JarvisRed else Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = if (isMuted) Icons.AutoMirrored.Filled.VolumeMute else Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = "Mute",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isMuted) "MUTED" else "VOICE ON",
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
