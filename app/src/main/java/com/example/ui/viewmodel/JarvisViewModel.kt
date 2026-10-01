package com.example.ui.viewmodel

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.AIProviderManager
import com.example.ai.GeminiProvider
import com.example.ai.OpenAIProvider
import com.example.ai.models.AIRequest
import com.example.alarms.AlarmTimerManager
import com.example.contacts.CallManager
import com.example.contacts.ContactManager
import com.example.core.performance.DevicePerformanceProfile
import com.example.core.performance.DeviceSpecs
import com.example.core.performance.PerformanceMode
import com.example.database.JarvisDatabase
import com.example.database.entities.ActionLog
import com.example.database.entities.ConversationMessage
import com.example.database.entities.MemoryItem
import com.example.instagram.InstagramManager
import com.example.location.DeviceLocationManager
import com.example.memory.MemoryRepository
import com.example.music.MusicManager
import com.example.permissions.PermissionManager
import com.example.phone.PhoneControlManager
import com.example.planner.CommandRouter
import com.example.planner.TaskPlanner
import com.example.planner.models.IntentType
import com.example.planner.models.TaskPlan
import com.example.planner.models.TaskStep
import com.example.planner.models.VerificationStatus
import com.example.playstore.PlayStoreManager
import com.example.screen.ScreenUnderstandingEngine
import com.example.screen.UIAutomationEngine
import com.example.security.SecurityManager
import com.example.services.BatteryMonitorService
import com.example.services.JarvisFloatingService
import com.example.settings.JarvisPreferences
import com.example.sms.SmsManager
import com.example.verification.VerificationEngine
import com.example.voice.ElevenLabsProvider
import com.example.voice.VoiceManager
import com.example.weather.WeatherInfo
import com.example.weather.WeatherManager
import com.example.whatsapp.WhatsAppAutomationManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class AssistantState {
    DEACTIVATED,
    IDLE,
    ACTIVE,
    LISTENING,
    PROCESSING,
    PLANNING,
    EXECUTING,
    VERIFYING,
    RESPONDING,
    ERROR
}

class JarvisViewModel(application: Application) : AndroidViewModel(application) {

    val prefs = JarvisPreferences(application)
    private val database = JarvisDatabase.getDatabase(application)
    val memoryRepo = MemoryRepository(database)

    val deviceSpecs: DeviceSpecs = DevicePerformanceProfile.detectSpecs(application)

    private val _performanceMode = MutableStateFlow(prefs.performanceMode)
    val performanceMode: StateFlow<PerformanceMode> = _performanceMode.asStateFlow()

    val isLowBatteryModeActive: StateFlow<Boolean> = BatteryMonitorService.isLowBatteryModeActive

    val geminiProvider = GeminiProvider { prefs.geminiApiKey }
    val openAIProvider = OpenAIProvider { prefs.openAiApiKey }
    val elevenLabsProvider = ElevenLabsProvider { prefs.elevenLabsApiKey }
    val aiProviderManager = AIProviderManager(geminiProvider, openAIProvider)

    val permissionManager = PermissionManager(application)
    val securityManager = SecurityManager()
    val phoneControl = PhoneControlManager(application)
    val contactManager = ContactManager(application)
    val callManager = CallManager(application)
    val smsManager = SmsManager(application)
    val whatsAppManager = WhatsAppAutomationManager(application)
    val instagramManager = InstagramManager(application)
    val musicManager = MusicManager(application)
    val playStoreManager = PlayStoreManager(application)
    val alarmTimerManager = AlarmTimerManager(application)
    val locationManager = DeviceLocationManager(application)
    val weatherManager = WeatherManager()
    val screenEngine = ScreenUnderstandingEngine()
    val uiAutomation = UIAutomationEngine()
    val verificationEngine = VerificationEngine()
    val voiceManager = VoiceManager(application, elevenLabsProvider)

    // UI States
    private val _assistantState = MutableStateFlow(AssistantState.ACTIVE)
    val assistantState: StateFlow<AssistantState> = _assistantState.asStateFlow()

    private val _isMuted = MutableStateFlow(prefs.isMuted)
    val isMuted: StateFlow<Boolean> = _isMuted.asStateFlow()

    private val _isVoiceMode = MutableStateFlow(true)
    val isVoiceMode: StateFlow<Boolean> = _isVoiceMode.asStateFlow()

    private val _currentPlan = MutableStateFlow<TaskPlan?>(null)
    val currentPlan: StateFlow<TaskPlan?> = _currentPlan.asStateFlow()

    private val _lastUserTranscript = MutableStateFlow("")
    val lastUserTranscript: StateFlow<String> = _lastUserTranscript.asStateFlow()

    private val _currentStatusMessage = MutableStateFlow("All systems online, Sir.")
    val currentStatusMessage: StateFlow<String> = _currentStatusMessage.asStateFlow()

    private val _pendingConfirmationStep = MutableStateFlow<TaskStep?>(null)
    val pendingConfirmationStep: StateFlow<TaskStep?> = _pendingConfirmationStep.asStateFlow()

    private val _clarificationQuery = MutableStateFlow<String?>(null)
    val clarificationQuery: StateFlow<String?> = _clarificationQuery.asStateFlow()

    private val _batteryPercentage = MutableStateFlow(85)
    val batteryPercentage: StateFlow<Int> = _batteryPercentage.asStateFlow()

    private val _isCharging = MutableStateFlow(false)
    val isCharging: StateFlow<Boolean> = _isCharging.asStateFlow()

    private val _networkStatus = MutableStateFlow("ONLINE")
    val networkStatus: StateFlow<String> = _networkStatus.asStateFlow()

    private val _locationText = MutableStateFlow("New Delhi, India")
    val locationText: StateFlow<String> = _locationText.asStateFlow()

    private val _weather = MutableStateFlow<WeatherInfo?>(null)
    val weather: StateFlow<WeatherInfo?> = _weather.asStateFlow()

    private val _isScreenShareActive = MutableStateFlow(prefs.isScreenShareActive)
    val isScreenShareActive: StateFlow<Boolean> = _isScreenShareActive.asStateFlow()

    private val _startupFinished = MutableStateFlow(!prefs.isStartupAnimationEnabled)
    val startupFinished: StateFlow<Boolean> = _startupFinished.asStateFlow()

    val conversations: StateFlow<List<ConversationMessage>> = memoryRepo.allMessages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val memories: StateFlow<List<MemoryItem>> = memoryRepo.allMemories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val actionLogs: StateFlow<List<ActionLog>> = memoryRepo.allLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val rmsDb: StateFlow<Float> = voiceManager.rmsDb

    init {
        setupVoiceCallbacks()
        monitorDeviceTelemetry()
        refreshWeatherAndLocation()
        applyPreferences()

        BatteryMonitorService.start(application)
        viewModelScope.launch {
            BatteryMonitorService.performanceModeState.collect { mode ->
                _performanceMode.value = mode
            }
        }
    }

    private fun setupVoiceCallbacks() {
        voiceManager.onUserCommand = { command, sttEndTime ->
            processInput(command, sttEndTime)
        }
        voiceManager.onInterruption = {
            cancelPlan("Voice interruption received. Listening...")
            _assistantState.value = AssistantState.LISTENING
            voiceManager.startListening()
        }
        voiceManager.onClapTriggered = {
            if (prefs.isClapDeactivationEnabled && _assistantState.value != AssistantState.DEACTIVATED) {
                _assistantState.value = AssistantState.DEACTIVATED
                voiceManager.isDeactivated = true
                voiceManager.stopListening()
                voiceManager.stopSpeaking()
                _currentStatusMessage.value = "Deactivated via clap trigger. Say 'Wake up Jarvis' to activate."
            }
        }
        voiceManager.onWakeUpTriggered = { remainingText ->
            _assistantState.value = AssistantState.ACTIVE
            voiceManager.isDeactivated = false
            val greeting = voiceManager.wakeWordManager.getWakeAcknowledgment(prefs.wakeAcknowledgmentMode == "FIXED")
            _currentStatusMessage.value = greeting
            speakAndDisplay(greeting) {
                if (remainingText.isNotBlank()) {
                    processInput(remainingText)
                } else {
                    _assistantState.value = AssistantState.LISTENING
                    voiceManager.startListening()
                }
            }
        }
    }

    fun applyPreferences() {
        voiceManager.wakeWordManager.wakeMode = when (prefs.assistantIdentity) {
            "JARVIS_ONLY" -> com.example.voice.WakeMode.JARVIS_ONLY
            "MYRAA_ONLY" -> com.example.voice.WakeMode.MYRAA_ONLY
            "CUSTOM" -> com.example.voice.WakeMode.CUSTOM
            else -> com.example.voice.WakeMode.BOTH
        }
        voiceManager.wakeWordManager.customWakePhrase = prefs.customWakeName
        voiceManager.setMute(prefs.isMuted)
        voiceManager.sttManager.isContinuousListening = prefs.isContinuousListening
        voiceManager.clapDetector.isEnabled = prefs.isClapDeactivationEnabled
        voiceManager.clapDetector.sensitivity = prefs.clapSensitivity

        voiceManager.ttsManager.configureVoiceProfiles(
            identity = prefs.activeVoiceIdentity,
            rate = prefs.speechSpeed,
            jConfig = com.example.voice.VoiceProfileConfig(
                voiceId = prefs.elevenLabsJarvisVoiceId,
                modelId = prefs.elevenLabsTtsModel,
                stability = prefs.jarvisStability,
                similarityBoost = prefs.jarvisSimilarity,
                style = prefs.jarvisStyle,
                useSpeakerBoost = prefs.speakerBoost
            ),
            mConfig = com.example.voice.VoiceProfileConfig(
                voiceId = prefs.elevenLabsMyraaVoiceId,
                modelId = prefs.elevenLabsTtsModel,
                stability = prefs.myraaStability,
                similarityBoost = prefs.myraaSimilarity,
                style = prefs.myraaStyle,
                useSpeakerBoost = prefs.speakerBoost
            )
        )
    }

    fun finishStartup() {
        _startupFinished.value = true
        prefs.isFirstLaunchDone = true
        val welcomeSpeech = "Welcome back, ${prefs.userName}. All primary systems are active and standing by."
        speakAndDisplay(welcomeSpeech)
    }

    fun toggleActiveState() {
        if (_assistantState.value == AssistantState.DEACTIVATED) {
            _assistantState.value = AssistantState.ACTIVE
            _currentStatusMessage.value = "Systems active. Standing by."
            speakAndDisplay("JARVIS online, Sir.")
            if (prefs.isFloatingIconEnabled && permissionManager.getPermissionsState().any { it.id == "OVERLAY" && it.isGranted }) {
                JarvisFloatingService.start(getApplication())
            }
        } else {
            _assistantState.value = AssistantState.DEACTIVATED
            voiceManager.stopListening()
            voiceManager.stopSpeaking()
            _currentStatusMessage.value = "Assistant paused."
            JarvisFloatingService.stop(getApplication())
        }
    }

    fun toggleMute() {
        val newMute = !_isMuted.value
        _isMuted.value = newMute
        prefs.isMuted = newMute
        voiceManager.setMute(newMute)
        _currentStatusMessage.value = if (newMute) "Voice muted" else "Voice active"
    }

    fun toggleVoiceMode() {
        _isVoiceMode.value = !_isVoiceMode.value
    }

    fun toggleScreenShare() {
        val newState = !_isScreenShareActive.value
        _isScreenShareActive.value = newState
        prefs.isScreenShareActive = newState
        val msg = if (newState) "Screen sharing activated. Vision engine standing by." else "Screen sharing stopped."
        speakAndDisplay(msg)
    }

    fun onMicClicked() {
        if (_assistantState.value == AssistantState.DEACTIVATED) {
            toggleActiveState()
        }
        if (voiceManager.isListening.value) {
            voiceManager.stopListening()
            _assistantState.value = AssistantState.IDLE
        } else {
            _assistantState.value = AssistantState.LISTENING
            _currentStatusMessage.value = "Listening to you, Sir..."
            voiceManager.startListening()
        }
    }

    fun processInput(text: String, sttEndTime: Long = 0L) {
        if (text.isBlank()) return
        if (_assistantState.value == AssistantState.DEACTIVATED) {
            _assistantState.value = AssistantState.ACTIVE
        }

        _lastUserTranscript.value = text

        viewModelScope.launch(Dispatchers.IO) {
            val senderName = prefs.activeVoiceIdentity

            // Save user message to memory
            memoryRepo.recordMessage("USER", text)

            _assistantState.value = AssistantState.PLANNING
            _currentStatusMessage.value = "Analyzing command..."

            val plan = TaskPlanner.plan(text)
            _currentPlan.value = plan

            executePlan(plan, senderName, sttEndTime)
        }
    }

    private suspend fun executePlan(plan: TaskPlan, assistantName: String, sttEndTime: Long = 0L) {
        for (i in plan.steps.indices) {
            if (plan.isCancelled) break
            plan.currentStepIndex = i
            val step = plan.steps[i]

            _assistantState.value = AssistantState.EXECUTING
            _currentStatusMessage.value = step.title
            step.status = VerificationStatus.EXECUTING

            // Security & Risk Assessment
            if (securityManager.needsUserConfirmation(step.intent)) {
                _pendingConfirmationStep.value = step
                _assistantState.value = AssistantState.VERIFYING
                speakAndDisplay("Sir, authorization required for ${step.title}. Shall I proceed?")
                return
            }

            executeStep(step, assistantName, plan.planId, sttEndTime)
        }

        if (!plan.isCancelled) {
            _assistantState.value = AssistantState.IDLE
            _currentPlan.value = null
        }
    }

    fun confirmPendingAction(approved: Boolean) {
        val step = _pendingConfirmationStep.value ?: return
        _pendingConfirmationStep.value = null
        val plan = _currentPlan.value ?: return

        viewModelScope.launch(Dispatchers.IO) {
            val assistantName = prefs.activeVoiceIdentity
            if (approved) {
                _assistantState.value = AssistantState.EXECUTING
                executeStep(step, assistantName, plan.planId)

                // Continue remaining steps in plan
                val nextIndex = plan.currentStepIndex + 1
                for (i in nextIndex until plan.steps.size) {
                    if (plan.isCancelled) break
                    plan.currentStepIndex = i
                    val nextStep = plan.steps[i]
                    if (securityManager.needsUserConfirmation(nextStep.intent)) {
                        _pendingConfirmationStep.value = nextStep
                        speakAndDisplay("Confirmation required for ${nextStep.title}.")
                        return@launch
                    }
                    executeStep(nextStep, assistantName, plan.planId)
                }
                _assistantState.value = AssistantState.IDLE
                _currentPlan.value = null
            } else {
                step.status = VerificationStatus.FAILED
                step.resultMessage = "User cancelled"
                speakAndDisplay("Action cancelled, Sir.")
                _assistantState.value = AssistantState.IDLE
                _currentPlan.value = null
            }
        }
    }

    private suspend fun executeStep(step: TaskStep, assistantName: String, planId: String? = null, sttEndTime: Long = 0L) {
        val intent = step.intent
        var success = false
        var extraMsg: String? = null

        when (intent.type) {
            IntentType.CREATOR_INFO -> {
                success = true
                extraMsg = "Mujhe ${prefs.creatorName} ne create kiya hai."
            }

            IntentType.MUTE_CONTROL -> {
                val isUnmute = intent.target == "UNMUTE"
                _isMuted.value = !isUnmute
                voiceManager.setMute(!isUnmute)
                success = true
                extraMsg = if (isUnmute) "Voice unmuted." else "Voice muted."
            }

            IntentType.BATTERY -> {
                success = true
                extraMsg = "Battery ${_batteryPercentage.value}% hai${if (_isCharging.value) " aur phone charge par hai" else ""}."
            }

            IntentType.CALL_CONTACT -> {
                val contactName = intent.target ?: "Rahul"
                val matchedContacts = contactManager.searchContacts(contactName)
                if (matchedContacts.size > 1) {
                    _clarificationQuery.value = "Found ${matchedContacts.size} contacts for $contactName (${matchedContacts.joinToString { it.name }}). Kaunsa call karun?"
                    speakAndDisplay(_clarificationQuery.value.orEmpty())
                    step.status = VerificationStatus.REQUIRES_USER
                    return
                }
                val phone = matchedContacts.firstOrNull()?.phoneNumber ?: "+919876543210"
                success = callManager.initiateCall(phone)
            }

            IntentType.SEND_WHATSAPP -> {
                val contactName = intent.target ?: "Rahul"
                val matchedContacts = contactManager.searchContacts(contactName)
                val phone = matchedContacts.firstOrNull()?.phoneNumber
                val msg = intent.payload ?: "Main thodi der me aa raha hoon."
                success = whatsAppManager.sendMessage(phone, msg)
            }

            IntentType.SEND_SMS -> {
                val contactName = intent.target ?: "Rahul"
                val matchedContacts = contactManager.searchContacts(contactName)
                val phone = matchedContacts.firstOrNull()?.phoneNumber ?: "+919876543210"
                val msg = intent.payload ?: "Hello"
                success = smsManager.sendSms(phone, msg)
            }

            IntentType.PLAY_MUSIC -> {
                val query = intent.target ?: "Arijit Singh"
                val app = intent.payload ?: "Default"
                success = musicManager.playMusic(query, app)
            }

            IntentType.SET_TIMER -> {
                val minutes = intent.payload?.toIntOrNull() ?: 5
                success = alarmTimerManager.setTimer(minutes)
            }

            IntentType.SET_ALARM -> {
                val time = intent.payload ?: "07:15"
                success = alarmTimerManager.setAlarm(time)
            }

            IntentType.OPEN_APP -> {
                val app = intent.target ?: "Instagram"
                success = phoneControl.launchAppByName(app)
            }

            IntentType.SEARCH_PLAY_STORE -> {
                val app = intent.target ?: "WhatsApp"
                success = playStoreManager.searchPlayStore(app)
            }

            IntentType.DEVICE_SETTING -> {
                val setting = intent.target ?: "WIFI"
                val isTurnOn = intent.payload == "ON"
                if (setting == "FLASHLIGHT") {
                    success = phoneControl.setFlashlight(isTurnOn)
                } else if (setting == "VOLUME") {
                    success = phoneControl.adjustVolume(isTurnOn)
                } else {
                    success = phoneControl.openSettings(setting)
                }
            }

            IntentType.READ_SCREEN -> {
                val inspection = screenEngine.inspectCurrentScreen()
                success = inspection.isAccessibilityActive
                extraMsg = if (_isScreenShareActive.value) {
                    // Combine screen text with vision analysis
                    if (inspection.visibleText.isNotBlank()) {
                        "Screen contains: ${inspection.visibleText.take(200)}"
                    } else {
                        "Screen shared. Analyzing visual elements."
                    }
                } else {
                    inspection.summary
                }
            }

            IntentType.SCROLL_SCREEN -> {
                val directionDown = intent.target != "UP"
                success = uiAutomation.scroll(directionDown)
            }

            IntentType.TAP_SCREEN -> {
                val targetText = intent.target ?: "Search"
                success = uiAutomation.clickTarget(targetText)
            }

            IntentType.LOCATION -> {
                success = true
                extraMsg = "Aapki current location ${_locationText.value} hai."
            }

            IntentType.WEATHER -> {
                val w = _weather.value
                success = true
                extraMsg = if (w != null) {
                    "Currently ${w.temperatureCelsius}°C with ${w.condition} in ${w.locationName}."
                } else {
                    "Weather is pleasant at 26°C."
                }
            }

            IntentType.GENERAL_AI -> {
                // Determine if question asks for device time
                val qLower = intent.rawQuery.lowercase(Locale.ROOT)
                if (qLower.contains("time") || qLower.contains("samay")) {
                    val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
                    val timeStr = sdf.format(Date())
                    success = true
                    extraMsg = "Abhi $timeStr hai."
                } else {
                    val aiResponse = aiProviderManager.executeWithFailover(
                        request = AIRequest(
                            prompt = intent.rawQuery,
                            systemInstruction = "You are ${prefs.activeVoiceIdentity}, created by ${prefs.creatorName}. Tone: ${prefs.personality.name}. Respond naturally in Hindi/Hinglish/English depending on user language. Keep response concise, friendly, and human-like without repetitive filler."
                        ),
                        primaryProviderId = prefs.primaryAIProvider,
                        backupProviderId = prefs.backupAIProvider,
                        primaryModel = prefs.geminiModel
                    )
                    success = aiResponse.isSuccess
                    extraMsg = aiResponse.text
                }
            }

            else -> {
                success = true
                extraMsg = "Done, Sir."
            }
        }

        // Verification phase
        _assistantState.value = AssistantState.VERIFYING
        val report = verificationEngine.verify(intent, success, extraMsg)
        step.status = report.status
        step.resultMessage = report.userFriendlyMessage

        // Personality Polish
        val finalMessage = if (report.status == VerificationStatus.SUCCESS &&
            intent.type != IntentType.CREATOR_INFO && intent.type != IntentType.GENERAL_AI && intent.type != IntentType.BATTERY) {
            "${report.userFriendlyMessage} ${prefs.personality.completionAffirmation}"
        } else {
            report.userFriendlyMessage
        }

        // Memory logging
        memoryRepo.recordMessage(assistantName, finalMessage, intent.type.name, planId)
        memoryRepo.logAction(step.title, intent.type.name, finalMessage, report.status.name)

        speakAndDisplay(finalMessage, sttEndTime)
    }

    private fun speakAndDisplay(text: String, sttEndTime: Long = 0L, onDone: (() -> Unit)? = null) {
        _currentStatusMessage.value = text
        _assistantState.value = AssistantState.RESPONDING
        voiceManager.speak(text, sttEndTime) {
            onDone?.invoke()
            if (_assistantState.value == AssistantState.RESPONDING) {
                _assistantState.value = AssistantState.IDLE
            }
        }
    }

    fun cancelPlan(reason: String = "Task cancelled") {
        _currentPlan.value?.let { it.isCancelled = true }
        _currentPlan.value = null
        _pendingConfirmationStep.value = null
        _clarificationQuery.value = null
        voiceManager.stopSpeaking()
        _assistantState.value = AssistantState.IDLE
    }

    fun clearHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            memoryRepo.clearConversation()
            memoryRepo.clearLogs()
        }
    }

    suspend fun runFullSystemDiagnostic() {
        val (geminiOk, gemMsg) = geminiProvider.testConnection(prefs.geminiApiKey, prefs.geminiModel)
        prefs.geminiStatus = gemMsg

        if (prefs.openAiApiKey.isNotBlank()) {
            val (_, oMsg) = openAIProvider.testConnection(prefs.openAiApiKey, prefs.openAiModel)
            prefs.openAiStatus = oMsg
        }

        if (prefs.elevenLabsApiKey.isNotBlank()) {
            val (_, elMsg) = elevenLabsProvider.testConnection(prefs.elevenLabsApiKey)
            prefs.elevenLabsStatus = elMsg
        }

        refreshWeatherAndLocation()
    }

    private fun refreshWeatherAndLocation() {
        viewModelScope.launch(Dispatchers.IO) {
            val loc = locationManager.getCurrentLocation()
            if (loc != null) {
                _locationText.value = loc.address
                val w = weatherManager.fetchWeather(loc.latitude, loc.longitude, loc.address)
                _weather.value = w
            } else {
                val w = weatherManager.fetchWeather()
                _weather.value = w
            }
        }
    }

    private fun monitorDeviceTelemetry() {
        val context = getApplication<Application>()
        try {
            val batteryFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            context.registerReceiver(object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
                    val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
                    if (level != -1 && scale != -1) {
                        _batteryPercentage.value = ((level / scale.toFloat()) * 100).toInt()
                    }
                    val status = intent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
                    _isCharging.value = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL
                }
            }, batteryFilter)

            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            val network = cm?.activeNetwork
            val capabilities = cm?.getNetworkCapabilities(network)
            val isOnline = capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
            _networkStatus.value = if (isOnline) "ONLINE" else "OFFLINE"
        } catch (e: Exception) {}
    }

    override fun onCleared() {
        super.onCleared()
        voiceManager.shutdown()
    }
}
