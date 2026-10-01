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
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.permissions.PermissionItem
import com.example.permissions.PermissionManager
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisDeepBg
import com.example.ui.theme.JarvisGold
import com.example.ui.theme.JarvisGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionCenterScreen(
    permissionManager: PermissionManager,
    onRequestAndroidPermission: (String) -> Unit,
    onBack: () -> Unit
) {
    var permissions by remember { mutableStateOf(permissionManager.getPermissionsState()) }

    fun refresh() {
        permissions = permissionManager.getPermissionsState()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(JarvisDeepBg)
    ) {
        TopAppBar(
            title = {
                Text(
                    text = "PERMISSION CENTER",
                    color = JarvisCyan,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
            },
            navigationIcon = {
                IconButton(onClick = onBack, modifier = Modifier.testTag("perm_back_button")) {
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "All Android permissions are requested transparently and contextually. JARVIS never conducts hidden monitoring.",
                    color = Color(0xFF94A3B8),
                    fontSize = 13.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            items(permissions) { item ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = item.title,
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                if (item.isGranted) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Granted",
                                        tint = JarvisGreen,
                                        modifier = Modifier.size(16.dp)
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = "Required",
                                        tint = JarvisGold,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = item.description,
                                color = Color(0xFF94A3B8),
                                fontSize = 12.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        if (item.isGranted) {
                            Box(
                                modifier = Modifier
                                    .background(JarvisGreen.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "GRANTED",
                                    color = JarvisGreen,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        } else {
                            Button(
                                onClick = {
                                    when (item.id) {
                                        "OVERLAY" -> permissionManager.openOverlaySettings()
                                        "ACCESSIBILITY" -> permissionManager.openAccessibilitySettings()
                                        else -> {
                                            if (item.androidPermission != null) {
                                                onRequestAndroidPermission(item.androidPermission)
                                            } else {
                                                permissionManager.openAppSettings()
                                            }
                                        }
                                    }
                                    refresh()
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = JarvisCyan,
                                    contentColor = Color(0xFF00363A)
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "ENABLE",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { permissionManager.openAppSettings() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B))
                ) {
                    Text("OPEN APP SYSTEM SETTINGS", color = JarvisCyan, fontSize = 13.sp)
                }
            }
        }
    }
}
