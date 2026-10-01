package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.core.performance.PerformanceMode
import com.example.ui.components.ConfirmationDialog
import com.example.ui.screens.ConversationScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.PermissionCenterScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.StartupScreen
import com.example.ui.screens.SystemHealthScreen
import com.example.ui.screens.VoiceStudioScreen
import com.example.ui.theme.JarvisDeepBg
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.JarvisViewModel

enum class CurrentScreen {
    DASHBOARD,
    CONVERSATION,
    SETTINGS,
    PERMISSION_CENTER,
    SYSTEM_HEALTH,
    VOICE_STUDIO
}

class MainActivity : ComponentActivity() {

    private val viewModel: JarvisViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                val startupFinished by viewModel.startupFinished.collectAsState()
                val batteryLevel by viewModel.batteryPercentage.collectAsState()
                val isCharging by viewModel.isCharging.collectAsState()
                val networkStatus by viewModel.networkStatus.collectAsState()
                val assistantState by viewModel.assistantState.collectAsState()
                val isMuted by viewModel.isMuted.collectAsState()
                val statusMessage by viewModel.currentStatusMessage.collectAsState()
                val lastUserTranscript by viewModel.lastUserTranscript.collectAsState()
                val rmsDb by viewModel.rmsDb.collectAsState()
                val currentPlan by viewModel.currentPlan.collectAsState()
                val conversations by viewModel.conversations.collectAsState()
                val isScreenShareActive by viewModel.isScreenShareActive.collectAsState()
                val pendingConfirmationStep by viewModel.pendingConfirmationStep.collectAsState()
                val performanceMode by viewModel.performanceMode.collectAsState()

                var currentScreen by remember { mutableStateOf(CurrentScreen.DASHBOARD) }

                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) {
                    // Refreshes permission states
                }

                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(JarvisDeepBg)
                        .statusBarsPadding()
                        .navigationBarsPadding(),
                    containerColor = JarvisDeepBg
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        if (!startupFinished) {
                            StartupScreen(
                                batteryLevel = batteryLevel,
                                userName = viewModel.prefs.userName,
                                isFirstLaunch = !viewModel.prefs.isFirstLaunchDone,
                                onSaveName = { newName -> viewModel.prefs.userName = newName },
                                onFinish = { viewModel.finishStartup() }
                            )
                        } else {
                            when (currentScreen) {
                                CurrentScreen.DASHBOARD -> {
                                    DashboardScreen(
                                        assistantState = assistantState,
                                        isMuted = isMuted,
                                        statusMessage = statusMessage,
                                        lastUserTranscript = lastUserTranscript,
                                        batteryPercentage = batteryLevel,
                                        isCharging = isCharging,
                                        networkStatus = networkStatus,
                                        rmsDb = rmsDb,
                                        currentPlan = currentPlan,
                                        assistantIdentity = viewModel.prefs.assistantIdentity,
                                        userName = viewModel.prefs.userName,
                                        isScreenShareActive = isScreenShareActive,
                                        performanceMode = performanceMode,
                                        onToggleActive = { viewModel.toggleActiveState() },
                                        onToggleMute = { viewModel.toggleMute() },
                                        onMicClick = { viewModel.onMicClicked() },
                                        onToggleScreenShare = { viewModel.toggleScreenShare() },
                                        onCancelPlan = { viewModel.cancelPlan() },
                                        onOpenSettings = { currentScreen = CurrentScreen.SETTINGS },
                                        onOpenChat = { currentScreen = CurrentScreen.CONVERSATION }
                                    )
                                }

                                CurrentScreen.CONVERSATION -> {
                                    BackHandler { currentScreen = CurrentScreen.DASHBOARD }
                                    ConversationScreen(
                                        messages = conversations,
                                        onSendMessage = { text -> viewModel.processInput(text) },
                                        onMicClick = { viewModel.onMicClicked() },
                                        onClearHistory = { viewModel.clearHistory() },
                                        onBack = { currentScreen = CurrentScreen.DASHBOARD }
                                    )
                                }

                                CurrentScreen.SETTINGS -> {
                                    BackHandler {
                                        viewModel.applyPreferences()
                                        currentScreen = CurrentScreen.DASHBOARD
                                    }
                                    SettingsScreen(
                                        prefs = viewModel.prefs,
                                        geminiProvider = viewModel.geminiProvider,
                                        openAIProvider = viewModel.openAIProvider,
                                        elevenLabsProvider = viewModel.elevenLabsProvider,
                                        onOpenPermissionCenter = { currentScreen = CurrentScreen.PERMISSION_CENTER },
                                        onOpenSystemHealth = { currentScreen = CurrentScreen.SYSTEM_HEALTH },
                                        onOpenVoiceStudio = { currentScreen = CurrentScreen.VOICE_STUDIO },
                                        onBack = {
                                            viewModel.applyPreferences()
                                            currentScreen = CurrentScreen.DASHBOARD
                                        }
                                    )
                                }

                                CurrentScreen.VOICE_STUDIO -> {
                                    BackHandler { currentScreen = CurrentScreen.SETTINGS }
                                    VoiceStudioScreen(
                                        prefs = viewModel.prefs,
                                        ttsManager = viewModel.voiceManager.ttsManager,
                                        onBack = {
                                            viewModel.applyPreferences()
                                            currentScreen = CurrentScreen.SETTINGS
                                        }
                                    )
                                }

                                CurrentScreen.PERMISSION_CENTER -> {
                                    BackHandler { currentScreen = CurrentScreen.SETTINGS }
                                    PermissionCenterScreen(
                                        permissionManager = viewModel.permissionManager,
                                        onRequestAndroidPermission = { perm -> permissionLauncher.launch(perm) },
                                        onBack = { currentScreen = CurrentScreen.SETTINGS }
                                    )
                                }

                                CurrentScreen.SYSTEM_HEALTH -> {
                                    BackHandler { currentScreen = CurrentScreen.SETTINGS }
                                    val perms = viewModel.permissionManager.getPermissionsState()
                                    SystemHealthScreen(
                                        deviceSpecs = viewModel.deviceSpecs,
                                        isMicGranted = perms.firstOrNull { it.id == "MIC" }?.isGranted == true,
                                        isContactsGranted = perms.firstOrNull { it.id == "CONTACTS" }?.isGranted == true,
                                        isPhoneGranted = perms.firstOrNull { it.id == "CALLS" }?.isGranted == true,
                                        isLocationGranted = perms.firstOrNull { it.id == "LOCATION" }?.isGranted == true,
                                        isOnline = networkStatus == "ONLINE",
                                        batteryLevel = batteryLevel,
                                        geminiStatus = viewModel.prefs.geminiStatus,
                                        openAiStatus = viewModel.prefs.openAiStatus,
                                        elevenLabsStatus = viewModel.prefs.elevenLabsStatus,
                                        onRunDiagnostic = { viewModel.runFullSystemDiagnostic() },
                                        onBack = { currentScreen = CurrentScreen.SETTINGS }
                                    )
                                }
                            }
                        }

                        // Risk Confirmation Modal
                        pendingConfirmationStep?.let { step ->
                            ConfirmationDialog(
                                step = step,
                                onConfirm = { viewModel.confirmPendingAction(true) },
                                onDismiss = { viewModel.confirmPendingAction(false) }
                            )
                        }
                    }
                }
            }
        }
    }
}
