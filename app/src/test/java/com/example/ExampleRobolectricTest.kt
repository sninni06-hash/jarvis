package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.core.performance.DevicePerformanceProfile
import com.example.core.performance.PerformanceMode
import com.example.planner.CommandRouter
import com.example.planner.TaskPlanner
import com.example.planner.models.ActionRisk
import com.example.planner.models.IntentType
import com.example.security.SecurityManager
import com.example.verification.VerificationEngine
import com.example.voice.WakeMode
import com.example.voice.WakeWordManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun testAppNameString() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("JARVIS", appName)
    }

    @Test
    fun testCreatorIntent() {
        val intent = CommandRouter.parseIntent("Who created you?")
        assertEquals(IntentType.CREATOR_INFO, intent.type)
        assertEquals("Nitin", intent.target)
    }

    @Test
    fun testHindiCallIntent() {
        val intent = CommandRouter.parseIntent("Vivek ko call kro")
        assertEquals(IntentType.CALL_CONTACT, intent.type)
        assertEquals("Vivek", intent.target)
        assertEquals(ActionRisk.MEDIUM, intent.risk)
    }

    @Test
    fun testMusicIntent() {
        val intent = CommandRouter.parseIntent("koi Arijit Singh ka song chala")
        assertEquals(IntentType.PLAY_MUSIC, intent.type)
        assertTrue(intent.target?.contains("Arijit", ignoreCase = true) == true)
    }

    @Test
    fun testTimerIntent() {
        val intent = CommandRouter.parseIntent("30 minute ka timer laga")
        assertEquals(IntentType.SET_TIMER, intent.type)
        assertEquals("30", intent.payload)
    }

    @Test
    fun testAlarmIntent() {
        val intent = CommandRouter.parseIntent("kal subah 7:15 ka alarm")
        assertEquals(IntentType.SET_ALARM, intent.type)
        assertTrue(intent.payload?.contains("7:15") == true)
    }

    @Test
    fun testMultiStepTaskPlanner() {
        val plan = TaskPlanner.plan("music chala do aur Vivek ko call karo")
        assertTrue(plan.steps.size >= 2)
        assertEquals(1, plan.steps[0].stepNumber)
        assertEquals(2, plan.steps[1].stepNumber)
    }

    @Test
    fun testWakeWordManager() {
        val wakeWordManager = WakeWordManager()
        wakeWordManager.wakeMode = WakeMode.BOTH

        val jarvisMatch = wakeWordManager.matchesWakeWord("JARVIS Vivek ko call kar")
        assertTrue(jarvisMatch.first)

        val myraaMatch = wakeWordManager.matchesWakeWord("MYRAA what is the weather")
        assertTrue(myraaMatch.first)

        wakeWordManager.wakeMode = WakeMode.CUSTOM
        wakeWordManager.customWakePhrase = "Friday"
        val fridayMatch = wakeWordManager.matchesWakeWord("Friday turn on flashlight")
        assertTrue(fridayMatch.first)
    }

    @Test
    fun testSecurityRiskAssessment() {
        val securityManager = SecurityManager()
        val callIntent = CommandRouter.parseIntent("Call Vivek")
        val risk = securityManager.assessRisk(callIntent)
        assertEquals(ActionRisk.MEDIUM, risk)
    }

    @Test
    fun testVerificationEngine() {
        val engine = VerificationEngine()
        val creatorIntent = CommandRouter.parseIntent("Who is your creator?")
        val report = engine.verify(creatorIntent, true)
        assertEquals("Mujhe Nitin ne create kiya hai.", report.userFriendlyMessage)
    }

    @Test
    fun testDevicePerformanceProfiler() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val specs = DevicePerformanceProfile.detectSpecs(context)
        assertNotNull(specs)
        assertTrue(specs.totalRamMb > 0)
        assertTrue(specs.cpuCores >= 1)

        val lowEndParticles = DevicePerformanceProfile.getParticleCount(PerformanceMode.PERFORMANCE)
        val qualityParticles = DevicePerformanceProfile.getParticleCount(PerformanceMode.QUALITY)
        assertTrue(lowEndParticles < qualityParticles)
        assertFalse(DevicePerformanceProfile.enableComplexBlur(PerformanceMode.PERFORMANCE))
    }

    @Test
    fun testBatteryMonitorService() {
        val controller = org.robolectric.Robolectric.buildService(com.example.services.BatteryMonitorService::class.java)
        val service = controller.create().startCommand(0, 0).get()

        val context = ApplicationProvider.getApplicationContext<Context>()

        // Broadcast ACTION_BATTERY_LOW
        val lowIntent = android.content.Intent(android.content.Intent.ACTION_BATTERY_LOW)
        context.sendBroadcast(lowIntent)

        // Verify that low battery mode toggles performance mode to minimal power drain
        assertEquals(PerformanceMode.PERFORMANCE, com.example.services.BatteryMonitorService.performanceModeState.value)

        // Broadcast ACTION_BATTERY_OKAY
        val okayIntent = android.content.Intent(android.content.Intent.ACTION_BATTERY_OKAY)
        context.sendBroadcast(okayIntent)

        controller.destroy()
    }

    @Test
    fun testSpokenResponseFormatter() {
        val rawAi = "Command received. Battery level is 62%.\n* Here is another line: `code block`."
        val cleaned = com.example.voice.SpokenResponseFormatter.formatForSpeech(rawAi)
        assertTrue(cleaned.contains("Battery 62 percent"))
        assertFalse(cleaned.contains("`code block`"))
        assertFalse(cleaned.contains("Command received."))

        val chunks = com.example.voice.SpokenResponseFormatter.splitIntoStreamingChunks(
            "Rahul ko call kar rahi hoon. Aur Spotify par gaana chala diya hai."
        )
        assertTrue(chunks.size >= 2)
        assertEquals("Rahul ko call kar rahi hoon.", chunks[0])
    }

    @Test
    fun testClapDetector() {
        val detector = com.example.voice.ClapDetector()
        detector.isEnabled = true
        detector.sensitivity = "MEDIUM"

        var clapFired = false
        detector.onClapDetected = { clapFired = true }

        // Normal sound
        detector.processRms(45f)
        assertFalse(clapFired)

        // Sharp transient jump from 45 to 80 dB
        detector.processRms(82f)
        assertTrue(clapFired)
    }

    @Test
    fun testWakeUpCommandAndGreetings() {
        val wakeWordManager = WakeWordManager()
        val (isWakeUp, remaining) = wakeWordManager.isWakeUpCommand("Wake up Jarvis")
        assertTrue(isWakeUp)

        val greeting = wakeWordManager.getWakeAcknowledgment(isFixed = true)
        assertEquals("Pranam Sir, kaise yaad kiya?", greeting)

        val variant = wakeWordManager.getWakeAcknowledgment(isFixed = false)
        assertTrue(variant.isNotBlank())
    }
}
