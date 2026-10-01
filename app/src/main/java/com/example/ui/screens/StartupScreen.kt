package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.VoiceVisualizer
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisDeepBg
import com.example.ui.theme.JarvisGold
import com.example.ui.viewmodel.AssistantState
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun StartupScreen(
    batteryLevel: Int,
    userName: String,
    isFirstLaunch: Boolean,
    onSaveName: (String) -> Unit,
    onFinish: () -> Unit
) {
    var step by remember { mutableIntStateOf(0) }
    var currentTime by remember { mutableStateOf("") }
    var inputName by remember { mutableStateOf(userName) }

    val logoScale by animateFloatAsState(
        targetValue = if (step >= 1) 1.0f else 0.4f,
        animationSpec = tween(700, easing = FastOutSlowInEasing),
        label = "logo_scale"
    )

    LaunchedEffect(Unit) {
        val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        currentTime = sdf.format(Date())

        delay(300)
        step = 1 // Logo animation
        delay(600)
        step = 2 // Welcome text
        delay(600)
        step = 3 // User name & telemetry

        if (!isFirstLaunch) {
            delay(1200)
            onFinish()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(JarvisDeepBg),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            // Arc Reactor / Assistant Logo
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .scale(logoScale),
                contentAlignment = Alignment.Center
            ) {
                VoiceVisualizer(
                    assistantState = AssistantState.ACTIVE,
                    rmsDb = 4f,
                    modifier = Modifier.fillMaxSize()
                )
                Image(
                    painter = painterResource(id = R.drawable.ic_jarvis_logo),
                    contentDescription = "JARVIS Logo",
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Step 2: "WELCOME BACK, SIR"
            AnimatedVisibility(
                visible = step >= 2,
                enter = fadeIn(tween(400)) + slideInVertically(initialOffsetY = { 40 })
            ) {
                Text(
                    text = "WELCOME BACK, SIR",
                    color = JarvisCyan,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 2.sp
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Step 3: User Name or First-Time Name Prompt
            AnimatedVisibility(
                visible = step >= 3,
                enter = fadeIn(tween(400)) + slideInVertically(initialOffsetY = { 40 })
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (isFirstLaunch) {
                        Text(
                            text = "What should I call you?",
                            color = JarvisGold,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = inputName,
                            onValueChange = { inputName = it },
                            placeholder = { Text("Your name (e.g. Nitin)", color = Color(0xFF64748B)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = JarvisCyan,
                                unfocusedBorderColor = Color(0xFF334155),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.width(260.dp)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = {
                                if (inputName.isNotBlank()) onSaveName(inputName)
                                onFinish()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = JarvisCyan, contentColor = Color(0xFF00363A))
                        ) {
                            Text("INITIALIZE JARVIS", fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Text(
                            text = userName.uppercase(),
                            color = JarvisGold,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 2.sp
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Live Telemetry bar (Real time clock + battery)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .background(Color(0xFF0F172A), RoundedCornerShape(12.dp))
                                .border(1.dp, JarvisCyan.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = "Clock",
                                tint = JarvisCyan,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = currentTime,
                                color = Color.White,
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Monospace
                            )

                            Spacer(modifier = Modifier.width(20.dp))

                            Icon(
                                imageVector = Icons.Default.BatteryChargingFull,
                                contentDescription = "Battery",
                                tint = JarvisGold,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "$batteryLevel%",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Quick skip button
                        Button(
                            onClick = onFinish,
                            modifier = Modifier.testTag("skip_startup_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B))
                        ) {
                            Text(
                                text = "ENTER ASSISTANT",
                                color = JarvisCyan,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
