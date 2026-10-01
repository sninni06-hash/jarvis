package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.core.performance.DevicePerformanceProfile
import com.example.core.performance.PerformanceMode
import com.example.ui.theme.JarvisBlue
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisGold
import com.example.ui.theme.JarvisGreen
import com.example.ui.theme.JarvisRed
import com.example.ui.viewmodel.AssistantState
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private data class GalaxyStar(
    val initialAngleRad: Double,
    val orbitRatio: Float,
    val speedFactor: Float,
    val sizePx: Float,
    val alphaOffset: Float
)

@Composable
fun VoiceVisualizer(
    assistantState: AssistantState,
    rmsDb: Float,
    isMyraa: Boolean = false,
    performanceMode: PerformanceMode = PerformanceMode.BALANCED,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ai_core_galaxy")

    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.88f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(9000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    val isListening = assistantState == AssistantState.LISTENING
    val isThinking = assistantState == AssistantState.PLANNING || assistantState == AssistantState.PROCESSING
    val isSpeaking = assistantState == AssistantState.RESPONDING
    val isExecuting = assistantState == AssistantState.EXECUTING
    val isError = assistantState == AssistantState.ERROR
    val isDeactivated = assistantState == AssistantState.DEACTIVATED

    // Colors per state
    val coreColor = when {
        isDeactivated -> Color(0xFF475569)
        isListening -> JarvisGreen // Green stars and waveform for LISTENING
        isThinking -> JarvisCyan  // Cyan / electric blue rotating energy rings
        isSpeaking -> if (isMyraa) Color(0xFFF472B6) else JarvisCyan // Deep cyan galaxy (or warm rose for MYRAA)
        isExecuting -> JarvisBlue
        isError -> JarvisRed
        else -> if (isMyraa) Color(0xFF38BDF8) else JarvisCyan
    }

    val particleCount = DevicePerformanceProfile.getParticleCount(performanceMode)
    val enableMultiGlow = DevicePerformanceProfile.enableMultiLayerGlow(performanceMode)

    // Pre-calculate deterministic galaxy particle layout so recomposition is lightweight
    val stars = remember(particleCount) {
        val rand = Random(42)
        List(particleCount) {
            GalaxyStar(
                initialAngleRad = rand.nextDouble(0.0, Math.PI * 2),
                orbitRatio = rand.nextFloat() * 0.45f + 0.85f,
                speedFactor = rand.nextFloat() * 1.5f + 0.5f,
                sizePx = rand.nextFloat() * 3.5f + 2.0f,
                alphaOffset = rand.nextFloat() * 0.4f + 0.6f
            )
        }
    }

    Box(
        modifier = modifier.size(260.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val baseRadius = size.minDimension / 2.7f
            val audioDynamic = (rmsDb / 9f).coerceIn(0f, 1.4f)
            val currentRadius = baseRadius * (if (isListening || isSpeaking) (1f + audioDynamic * 0.35f) else pulse)

            // 1. OUTER AMBIENT GALAXY GLOW (Simplified on low-end devices)
            if (enableMultiGlow) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(coreColor.copy(alpha = 0.28f), Color.Transparent),
                        center = center,
                        radius = currentRadius * 1.45f
                    ),
                    radius = currentRadius * 1.45f,
                    center = center
                )
            }

            // 2. ORBITING GALAXY STARS / QUANTUM PARTICLES
            for (star in stars) {
                val dir = if (isThinking) -1f else 1f // Swirl inward / reverse during thinking
                val angleRad = star.initialAngleRad + Math.toRadians((rotationAngle * star.speedFactor * dir).toDouble())
                val orbitDist = currentRadius * star.orbitRatio * (if (isSpeaking) (1f + audioDynamic * 0.15f) else 1f)

                val x = center.x + (orbitDist * cos(angleRad)).toFloat()
                val y = center.y + (orbitDist * sin(angleRad)).toFloat()

                val starAlpha = (star.alphaOffset * if (isDeactivated) 0.3f else 0.85f).coerceIn(0f, 1f)
                drawCircle(
                    color = coreColor.copy(alpha = starAlpha),
                    radius = star.sizePx * (if (isListening || isSpeaking) 1.25f else 1.0f),
                    center = Offset(x, y)
                )
            }

            // 3. ENERGY ORBIT RINGS
            drawCircle(
                color = coreColor.copy(alpha = if (isThinking) 0.85f else 0.45f),
                radius = currentRadius * 1.15f,
                center = center,
                style = Stroke(width = if (isThinking) 3.dp.toPx() else 1.5.dp.toPx())
            )

            // Inner Ring
            drawCircle(
                color = coreColor.copy(alpha = if (isListening) 0.9f else 0.6f),
                radius = currentRadius * 0.88f,
                center = center,
                style = Stroke(width = 2.dp.toPx())
            )

            // 4. RADIAL WAVEFORM TICKS (Microphone reactive)
            val tickCount = if (performanceMode == PerformanceMode.PERFORMANCE) 16 else 28
            for (i in 0 until tickCount) {
                val tickAngle = (i * (360f / tickCount)) + rotationAngle
                val tickRad = Math.toRadians(tickAngle.toDouble())
                val innerR = currentRadius * 0.92f
                val lengthMod = if (isListening || isSpeaking) (audioDynamic * 0.22f) else 0f
                val outerR = currentRadius * (1.10f + lengthMod)

                val start = Offset(
                    x = center.x + (innerR * cos(tickRad)).toFloat(),
                    y = center.y + (innerR * sin(tickRad)).toFloat()
                )
                val end = Offset(
                    x = center.x + (outerR * cos(tickRad)).toFloat(),
                    y = center.y + (outerR * sin(tickRad)).toFloat()
                )

                val tickAlpha = if (i % 2 == 0) 0.85f else 0.45f
                drawLine(
                    color = coreColor.copy(alpha = tickAlpha),
                    start = start,
                    end = end,
                    strokeWidth = 2.dp.toPx()
                )
            }

            // 5. INNER LUMINOUS CORE
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White,
                        coreColor,
                        coreColor.copy(alpha = 0.15f)
                    ),
                    center = center,
                    radius = currentRadius * 0.65f
                ),
                radius = currentRadius * 0.65f,
                center = center
            )
        }
    }
}
