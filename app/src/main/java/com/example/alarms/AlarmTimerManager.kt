package com.example.alarms

import android.content.Context
import android.content.Intent
import android.provider.AlarmClock
import java.util.Calendar

class AlarmTimerManager(private val context: Context) {

    fun setTimer(minutes: Int, label: String = "JARVIS Timer"): Boolean {
        return try {
            val lengthInSeconds = (minutes * 60).coerceAtLeast(1)
            val intent = Intent(AlarmClock.ACTION_SET_TIMER).apply {
                putExtra(AlarmClock.EXTRA_LENGTH, lengthInSeconds)
                putExtra(AlarmClock.EXTRA_MESSAGE, label)
                putExtra(AlarmClock.EXTRA_SKIP_UI, false)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun setAlarm(timeString: String, label: String = "JARVIS Alarm"): Boolean {
        val (hour, minute) = parseHourAndMinute(timeString)
        return try {
            val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
                putExtra(AlarmClock.EXTRA_HOUR, hour)
                putExtra(AlarmClock.EXTRA_MINUTES, minute)
                putExtra(AlarmClock.EXTRA_MESSAGE, label)
                putExtra(AlarmClock.EXTRA_SKIP_UI, false)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun parseHourAndMinute(raw: String): Pair<Int, Int> {
        val clean = raw.lowercase()
        val isPm = clean.contains("pm") || clean.contains("shaam") || clean.contains("raat")
        val isAm = clean.contains("am") || clean.contains("subah")

        val digits = Regex("(\\d{1,2})(?::(\\d{2}))?").find(clean)
        var hour = digits?.groupValues?.getOrNull(1)?.toIntOrNull() ?: 7
        val minute = digits?.groupValues?.getOrNull(2)?.toIntOrNull() ?: 0

        if (isPm && hour < 12) hour += 12
        if (isAm && hour == 12) hour = 0

        return Pair(hour.coerceIn(0, 23), minute.coerceIn(0, 59))
    }
}
