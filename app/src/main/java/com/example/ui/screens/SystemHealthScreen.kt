package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.example.core.performance.DeviceSpecs
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisDeepBg
import com.example.ui.theme.JarvisGold
import com.example.ui.theme.JarvisGreen
import com.example.ui.theme.JarvisRed
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class HealthCheckItem(
    val title: String,
    val description: String,
    val isPassing: Boolean,
    val details: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SystemHealthScreen(
    deviceSpecs: DeviceSpecs,
    isMicGranted: Boolean,
    isContactsGranted: Boolean,
    isPhoneGranted: Boolean,
    isLocationGranted: Boolean,
    isOnline: Boolean,
    batteryLevel: Int,
    geminiStatus: String,
    openAiStatus: String,
    elevenLabsStatus: String,
    onRunDiagnostic: suspend () -> Unit,
    onBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var isDiagnosing by remember { mutableStateOf(false) }

    val checks = listOf(
        HealthCheckItem("Hardware & Processor", "${deviceSpecs.modelName} • ${deviceSpecs.cpuCores} Cores", true, "${deviceSpecs.androidVersion} (${deviceSpecs.suggestedMode.name} Mode)"),
        HealthCheckItem("RAM Memory", "${deviceSpecs.availableRamMb} MB Free / ${deviceSpecs.totalRamMb} MB Total", deviceSpecs.availableRamMb > 200, if (deviceSpecs.isLowRamDevice) "Low-RAM mode active" else "Normal memory profile"),
        HealthCheckItem("Internal Storage", "${deviceSpecs.availableInternalStorageMb} MB Available", deviceSpecs.availableInternalStorageMb > 500, "Cache and models operational"),
        HealthCheckItem("Network Link", if (isOnline) "Internet Connected" else "Offline", isOnline, if (isOnline) "Cloud APIs reachable" else "Operating in local device mode"),
        HealthCheckItem("Microphone Hardware", if (isMicGranted) "Permission Granted" else "Permission Missing", isMicGranted, "Voice capture ready"),
        HealthCheckItem("Contacts Integration", if (isContactsGranted) "Ready" else "Permission Missing", isContactsGranted, "Direct contact resolution"),
        HealthCheckItem("Phone Calling Service", if (isPhoneGranted) "Direct Calling Ready" else "Dialer Handoff", true, if (isPhoneGranted) "Direct hands-free call" else "Dialer fallback"),
        HealthCheckItem("Location Telemetry", if (isLocationGranted) "Active" else "Default Coordinates", true, "Used for live weather"),
        HealthCheckItem("Battery Management", "$batteryLevel% Remaining", batteryLevel > 15, "Power throttling inactive"),
        HealthCheckItem("Gemini AI Engine", geminiStatus, geminiStatus.contains("Connected", ignoreCase = true) || geminiStatus == "CONNECTED", "Primary intelligence router"),
        HealthCheckItem("OpenAI Engine", openAiStatus, openAiStatus.contains("Connected", ignoreCase = true) || openAiStatus == "CONNECTED", "Failover brain"),
        HealthCheckItem("ElevenLabs Voice", elevenLabsStatus, elevenLabsStatus.contains("Connected", ignoreCase = true), "Natural voice synthesis")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(JarvisDeepBg)
    ) {
        TopAppBar(
            title = {
                Text(
                    text = "SYSTEM HEALTH & DIAGNOSTICS",
                    color = JarvisCyan,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
            },
            navigationIcon = {
                IconButton(onClick = onBack, modifier = Modifier.testTag("health_back_button")) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0F172A))
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0C4A6E)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.HealthAndSafety,
                                contentDescription = null,
                                tint = JarvisCyan,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "CORE SYSTEM INTEGRITY",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Optimized for low-end hardware (${deviceSpecs.suggestedMode.name} mode active). Cloud inference offloads heavy compute from mobile CPU.",
                            color = Color(0xFFBAE6FD),
                            fontSize = 12.sp
                        )
                    }
                }
            }

            item {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            isDiagnosing = true
                            onRunDiagnostic()
                            delay(800)
                            isDiagnosing = false
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("run_diagnostic_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = JarvisCyan,
                        contentColor = Color(0xFF00363A)
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    if (isDiagnosing) {
                        CircularProgressIndicator(
                            color = Color(0xFF00363A),
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.5.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("DIAGNOSING SYSTEM...", fontWeight = FontWeight.Bold)
                    } else {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("RUN FULL DIAGNOSTIC", fontWeight = FontWeight.Bold)
                    }
                }
            }

            items(checks) { check ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = check.title,
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = check.description,
                                color = Color(0xFF94A3B8),
                                fontSize = 12.sp
                            )
                            Text(
                                text = check.details,
                                color = Color(0xFF64748B),
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Icon(
                            imageVector = if (check.isPassing) Icons.Default.CheckCircle else Icons.Default.Warning,
                            contentDescription = if (check.isPassing) "Passing" else "Attention",
                            tint = if (check.isPassing) JarvisGreen else JarvisGold,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
