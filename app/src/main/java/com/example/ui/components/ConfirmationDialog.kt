package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.planner.models.ActionRisk
import com.example.planner.models.TaskStep
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisGold
import com.example.ui.theme.JarvisRed

@Composable
fun ConfirmationDialog(
    step: TaskStep,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0F172A), RoundedCornerShape(16.dp))
                .border(1.5.dp, JarvisCyan.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                .padding(20.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val iconColor = if (step.intent.risk == ActionRisk.HIGH) JarvisRed else JarvisGold
                    Icon(
                        imageVector = if (step.intent.risk == ActionRisk.HIGH) Icons.Default.Warning else Icons.Default.Security,
                        contentDescription = "Security Alert",
                        tint = iconColor,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "SECURITY AUTHORIZATION",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Sir, JARVIS requires explicit authorization to execute the following operation:",
                    color = Color(0xFFCBD5E1),
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1E293B), RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Text(
                            text = step.title,
                            color = JarvisCyan,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Target: ${step.intent.target ?: "System"}",
                            color = Color(0xFF94A3B8),
                            fontSize = 13.sp
                        )
                        if (!step.intent.payload.isNullOrBlank()) {
                            Text(
                                text = "Payload: ${step.intent.payload}",
                                color = Color(0xFF94A3B8),
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("dialog_abort_button"),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF94A3B8))
                    ) {
                        Text("ABORT")
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = onConfirm,
                        modifier = Modifier.testTag("dialog_authorize_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = JarvisCyan, contentColor = Color(0xFF00363A))
                    ) {
                        Text("AUTHORIZE", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
