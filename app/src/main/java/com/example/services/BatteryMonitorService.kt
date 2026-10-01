package com.example.services

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.IBinder
import com.example.core.performance.DevicePerformanceProfile
import com.example.core.performance.PerformanceMode
import com.example.settings.JarvisPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class BatteryMonitorService : Service() {

    private lateinit var prefs: JarvisPreferences
    private var batteryReceiver: BroadcastReceiver? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        prefs = JarvisPreferences(this)
        registerBatteryMonitor()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        checkCurrentBatteryState()
        return START_STICKY
    }

    private fun registerBatteryMonitor() {
        batteryReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                when (intent?.action) {
                    Intent.ACTION_BATTERY_LOW -> {
                        enableLowBatteryPerformanceMode()
                    }
                    Intent.ACTION_BATTERY_OKAY -> {
                        restoreStandardPerformanceMode()
                    }
                    Intent.ACTION_POWER_CONNECTED -> {
                        _isChargingState.value = true
                        checkCurrentBatteryState()
                    }
                    Intent.ACTION_POWER_DISCONNECTED -> {
                        _isChargingState.value = false
                        checkCurrentBatteryState()
                    }
                }
            }
        }

        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_BATTERY_LOW)
            addAction(Intent.ACTION_BATTERY_OKAY)
            addAction(Intent.ACTION_POWER_CONNECTED)
            addAction(Intent.ACTION_POWER_DISCONNECTED)
        }

        registerReceiver(batteryReceiver, filter)
    }

    private fun checkCurrentBatteryState() {
        val batteryStatusIntent = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = batteryStatusIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryStatusIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val status = batteryStatusIntent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1

        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL
        _isChargingState.value = isCharging

        if (level != -1 && scale != -1) {
            val percentage = ((level / scale.toFloat()) * 100).toInt()
            _batteryLevel.value = percentage

            // On low-end hardware, if battery is <= 15% and not charging, force minimal battery drain
            if (percentage <= 15 && !isCharging) {
                enableLowBatteryPerformanceMode()
            } else if (percentage > 20 && _isLowBatteryModeActive.value) {
                restoreStandardPerformanceMode()
            }
        }
    }

    private fun enableLowBatteryPerformanceMode() {
        _isLowBatteryModeActive.value = true
        prefs.performanceMode = PerformanceMode.PERFORMANCE
        _performanceModeState.value = PerformanceMode.PERFORMANCE
    }

    private fun restoreStandardPerformanceMode() {
        _isLowBatteryModeActive.value = false
        val restored = DevicePerformanceProfile.detectSpecs(this).suggestedMode
        prefs.performanceMode = restored
        _performanceModeState.value = restored
    }

    override fun onDestroy() {
        super.onDestroy()
        batteryReceiver?.let {
            try {
                unregisterReceiver(it)
            } catch (e: Exception) {}
        }
    }

    companion object {
        private val _isLowBatteryModeActive = MutableStateFlow(false)
        val isLowBatteryModeActive: StateFlow<Boolean> = _isLowBatteryModeActive.asStateFlow()

        private val _performanceModeState = MutableStateFlow(PerformanceMode.PERFORMANCE)
        val performanceModeState: StateFlow<PerformanceMode> = _performanceModeState.asStateFlow()

        private val _batteryLevel = MutableStateFlow(100)
        val batteryLevel: StateFlow<Int> = _batteryLevel.asStateFlow()

        private val _isChargingState = MutableStateFlow(false)
        val isChargingState: StateFlow<Boolean> = _isChargingState.asStateFlow()

        fun start(context: Context) {
            val intent = Intent(context, BatteryMonitorService::class.java)
            context.startService(intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, BatteryMonitorService::class.java)
            context.stopService(intent)
        }
    }
}
