package com.example.game

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos

/**
 * State holder for the Jetpack Compose Game Loop.
 * Tracks FPS, frame count, elapsed game time, and loop status.
 */
@Stable
class GameLoopState {
    var isRunning by mutableStateOf(false)
        internal set

    var isPaused by mutableStateOf(false)
        internal set

    var currentFps by mutableIntStateOf(60)
        internal set

    var frameCount by mutableLongStateOf(0L)
        internal set

    var totalTimeSeconds by mutableFloatStateOf(0f)
        internal set

    fun start() {
        isRunning = true
        isPaused = false
    }

    fun pause() {
        isPaused = true
    }

    fun resume() {
        isPaused = false
    }

    fun stop() {
        isRunning = false
        isPaused = false
    }
}

/**
 * Creates and remembers a [GameLoopState] instance across recompositions.
 */
@Composable
fun rememberGameLoopState(
    autoStart: Boolean = true
): GameLoopState {
    return remember {
        GameLoopState().apply {
            if (autoStart) start()
        }
    }
}

/**
 * Core Jetpack Compose Game Loop Composable.
 *
 * Uses [withFrameNanos] synchronized with the Android Choreographer / display VSYNC.
 * Features:
 * - Real-time delta time calculation (dt in seconds)
 * - Delta time clamping to prevent physics explosion/tunneling after lag or backgrounding
 * - Pause/Resume safety (avoids huge time jump when unpausing)
 * - Smooth FPS calculation
 *
 * @param isRunning Controls whether the loop is actively scheduling frames.
 * @param isPaused When true, pauses dispatching frame updates while keeping loop active.
 * @param maxDeltaTimeSec Upper bound on delta time (prevents physics explosion on hitch/stall).
 * @param minDeltaTimeSec Lower bound on delta time.
 * @param state Optional [GameLoopState] to update with FPS and frame telemetry.
 * @param onUpdate Lambda invoked on each VSYNC frame with elapsed delta time in seconds.
 */
@Composable
fun GameLoop(
    isRunning: Boolean = true,
    isPaused: Boolean = false,
    maxDeltaTimeSec: Float = 0.05f,
    minDeltaTimeSec: Float = 0.001f,
    state: GameLoopState? = null,
    onUpdate: (deltaTimeSec: Float) -> Unit
) {
    val currentOnUpdate by rememberUpdatedState(onUpdate)

    LaunchedEffect(isRunning, isPaused) {
        state?.isRunning = isRunning
        state?.isPaused = isPaused

        if (!isRunning || isPaused) return@LaunchedEffect

        var lastFrameTimeNanos = 0L
        var fpsTimerNanos = 0L
        var framesInCurrentSecond = 0

        while (true) {
            withFrameNanos { frameTimeNanos ->
                if (lastFrameTimeNanos == 0L) {
                    // First frame after starting or resuming
                    lastFrameTimeNanos = frameTimeNanos
                    fpsTimerNanos = frameTimeNanos
                    return@withFrameNanos
                }

                val elapsedNanos = frameTimeNanos - lastFrameTimeNanos
                lastFrameTimeNanos = frameTimeNanos

                // Calculate delta time in seconds
                val rawDeltaTime = elapsedNanos / 1_000_000_000f
                // Clamp delta time to avoid physics anomalies if app is suspended
                val deltaTimeSec = rawDeltaTime.coerceIn(minDeltaTimeSec, maxDeltaTimeSec)

                // Dispatch game update for car movement, traffic, road scroll, collisions
                currentOnUpdate(deltaTimeSec)

                // Telemetry & FPS calculation
                framesInCurrentSecond++
                val fpsElapsed = frameTimeNanos - fpsTimerNanos
                if (fpsElapsed >= 1_000_000_000L) {
                    state?.currentFps = framesInCurrentSecond
                    framesInCurrentSecond = 0
                    fpsTimerNanos = frameTimeNanos
                }

                state?.let {
                    it.frameCount++
                    it.totalTimeSeconds += deltaTimeSec
                }
            }
        }
    }
}

/**
 * Advanced Fixed Timestep Game Loop with Accumulator.
 *
 * Ideal for physics-heavy car racers:
 * - Executes physics simulation in fixed time slices (e.g. exactly 1/60s = 0.0166s),
 *   ensuring deterministic collision detection and vehicle handling.
 * - Supports interpolation fraction for ultra-smooth rendering.
 *
 * @param isRunning Whether loop is active
 * @param isPaused Whether loop is paused
 * @param fixedTimeStepSec Fixed physics step (default: 60Hz = 0.0166667f)
 * @param maxAccumulatedTimeSec Maximum time to simulate per frame to prevent spiral of death
 * @param onFixedUpdate Physics update called 0 or more times per frame with exact [fixedTimeStepSec]
 * @param onRender Optional render callback called once per frame with interpolation alpha (0f..1f)
 */
@Composable
fun FixedTimestepGameLoop(
    isRunning: Boolean = true,
    isPaused: Boolean = false,
    fixedTimeStepSec: Float = 1f / 60f,
    maxAccumulatedTimeSec: Float = 0.1f,
    onFixedUpdate: (fixedDt: Float) -> Unit,
    onRender: ((interpolationAlpha: Float) -> Unit)? = null
) {
    val currentOnFixedUpdate by rememberUpdatedState(onFixedUpdate)
    val currentOnRender by rememberUpdatedState(onRender)

    LaunchedEffect(isRunning, isPaused) {
        if (!isRunning || isPaused) return@LaunchedEffect

        var lastFrameTimeNanos = 0L
        var accumulator = 0f

        while (true) {
            withFrameNanos { frameTimeNanos ->
                if (lastFrameTimeNanos == 0L) {
                    lastFrameTimeNanos = frameTimeNanos
                    return@withFrameNanos
                }

                val elapsedNanos = frameTimeNanos - lastFrameTimeNanos
                lastFrameTimeNanos = frameTimeNanos

                var frameTimeSec = elapsedNanos / 1_000_000_000f
                if (frameTimeSec > maxAccumulatedTimeSec) {
                    frameTimeSec = maxAccumulatedTimeSec
                }

                accumulator += frameTimeSec

                // Consume accumulated time in fixed discrete steps
                while (accumulator >= fixedTimeStepSec) {
                    currentOnFixedUpdate(fixedTimeStepSec)
                    accumulator -= fixedTimeStepSec
                }

                // Fraction remaining for rendering interpolation (between 0.0 and 1.0)
                val alpha = accumulator / fixedTimeStepSec
                currentOnRender?.invoke(alpha.coerceIn(0f, 1f))
            }
        }
    }
}
