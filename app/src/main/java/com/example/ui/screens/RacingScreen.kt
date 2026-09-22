package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.data.GameRepository
import com.example.data.RaceRecord
import com.example.game.GameEngine
import com.example.game.GameLoop
import com.example.ui.components.RacingCanvas
import com.example.ui.components.RacingHud
import com.example.ui.dialogs.GameOverDialog
import com.example.ui.dialogs.PauseDialog
import kotlinx.coroutines.launch

@Composable
fun RacingScreen(
    engine: GameEngine,
    repository: GameRepository,
    currentHighScore: Int,
    controlMode: String,
    onHome: () -> Unit,
    onGarage: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    var isNewHighScore by remember { mutableStateOf(false) }
    var hasSavedResult by remember { mutableStateOf(false) }

    // Recompose when engine updates
    val engineTicks by engine.engineState.collectAsState()

    // Ensure engine is started
    LaunchedEffect(engine.isStarted) {
        if (!engine.isStarted) {
            engine.start()
        }
    }

    // Jetpack Compose Frame-by-Frame Game Loop
    GameLoop(
        isRunning = engine.isStarted && !engine.isGameOver,
        isPaused = engine.isPaused,
        maxDeltaTimeSec = 0.05f
    ) { deltaTimeSec ->
        engine.update(deltaTimeSec)
    }

    // Save results on game over
    LaunchedEffect(engine.isGameOver) {
        if (engine.isGameOver && !hasSavedResult) {
            hasSavedResult = true
            val finalScore = engine.score.toInt()
            if (finalScore > currentHighScore) {
                isNewHighScore = true
            }
            // Add coins to repository
            repository.addCoins(engine.coinsEarned)

            // Save record to Room Database
            coroutineScope.launch {
                repository.saveRecord(
                    RaceRecord(
                        score = finalScore,
                        distanceMeters = engine.distanceMeters.toInt(),
                        maxSpeedKmh = engine.maxSpeedReachedKmh,
                        coinsEarned = engine.coinsEarned,
                        nearMisses = engine.nearMissesCount,
                        carName = engine.car.name,
                        gameMode = engine.mode.title
                    )
                )
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Highway & Racing Canvas
        RacingCanvas(
            engine = engine,
            controlMode = controlMode
        )

        // Racing HUD Controls & Stats
        RacingHud(
            engine = engine,
            controlMode = controlMode,
            onPauseClick = { engine.togglePause() }
        )

        // Pause Menu Dialog
        if (engine.isPaused && !engine.isGameOver) {
            PauseDialog(
                onResume = { engine.togglePause() },
                onRestart = {
                    hasSavedResult = false
                    isNewHighScore = false
                    engine.resetGame()
                },
                onGarage = onGarage,
                onHome = onHome
            )
        }

        // Game Over Dialog
        if (engine.isGameOver) {
            GameOverDialog(
                engine = engine,
                isNewHighScore = isNewHighScore,
                onRestart = {
                    hasSavedResult = false
                    isNewHighScore = false
                    engine.resetGame()
                },
                onGarage = onGarage,
                onHome = onHome
            )
        }
    }
}
