package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.data.CarModel
import com.example.data.GameRepository
import com.example.game.GameEngine
import com.example.game.GameMode
import com.example.game.SoundManager
import com.example.game.WeatherTheme
import com.example.ui.dialogs.SettingsDialog
import com.example.ui.screens.GarageScreen
import com.example.ui.screens.LeaderboardScreen
import com.example.ui.screens.MainMenuScreen
import com.example.ui.screens.RacingScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.util.AppLanguage
import com.example.util.LocalAppStrings
import com.example.util.resolveStrings

enum class AppScreen {
    MAIN_MENU,
    RACING,
    GARAGE,
    LEADERBOARD
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val repository = GameRepository(applicationContext)
        val soundManager = SoundManager(applicationContext)

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF090D16)
                ) {
                    TurboRacerApp(
                        repository = repository,
                        soundManager = soundManager
                    )
                }
            }
        }
    }
}

@Composable
fun TurboRacerApp(
    repository: GameRepository,
    soundManager: SoundManager
) {
    var currentScreen by remember { mutableStateOf(AppScreen.MAIN_MENU) }
    var selectedMode by remember { mutableStateOf(GameMode.ENDLESS) }
    var weatherTheme by remember { mutableStateOf(WeatherTheme.NEON_NIGHT) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    val coins by repository.coinsFlow.collectAsState()
    val selectedCarId by repository.selectedCarIdFlow.collectAsState()
    val highScore by repository.getHighScore().collectAsState(initial = 0)
    val totalDistance by repository.getTotalDistance().collectAsState(initial = 0)
    val topRecords by repository.getTopRecords().collectAsState(initial = emptyList())

    var soundEnabled by remember { mutableStateOf(repository.getSoundEnabled()) }
    var hapticEnabled by remember { mutableStateOf(repository.getHapticEnabled()) }
    var controlMode by remember { mutableStateOf(repository.getControlMode()) }
    var appLanguage by remember { mutableStateOf(repository.getLanguage()) }

    val strings = resolveStrings(appLanguage)

    soundManager.isSoundEnabled = soundEnabled
    soundManager.isHapticEnabled = hapticEnabled

    // Current car model with upgrades
    val baseCar = CarModel.ALL_CARS.find { it.id == selectedCarId } ?: CarModel.ALL_CARS[0]
    val upgrades = repository.getCarUpgradeLevels(baseCar.id)
    val activeCar = baseCar.copy(
        speedLevel = upgrades.first,
        handlingLevel = upgrades.second,
        nitroLevel = upgrades.third
    )

    // Game Engine instance
    val gameEngine = remember(selectedCarId, selectedMode, weatherTheme) {
        GameEngine(
            car = activeCar,
            mode = selectedMode,
            weatherTheme = weatherTheme,
            soundManager = soundManager
        )
    }

    // Hardware Back Navigation Handling
    BackHandler(enabled = currentScreen != AppScreen.MAIN_MENU) {
        if (currentScreen == AppScreen.RACING) {
            if (!gameEngine.isPaused && !gameEngine.isGameOver) {
                gameEngine.togglePause()
            } else {
                currentScreen = AppScreen.MAIN_MENU
            }
        } else {
            currentScreen = AppScreen.MAIN_MENU
        }
    }

    CompositionLocalProvider(LocalAppStrings provides strings) {
        Crossfade(targetState = currentScreen, label = "ScreenTransition") { screen ->
            when (screen) {
                AppScreen.MAIN_MENU -> {
                    MainMenuScreen(
                        currentCoins = coins,
                        highScore = highScore ?: 0,
                        selectedCar = activeCar,
                        selectedMode = selectedMode,
                        onModeSelected = { selectedMode = it },
                        onStartGame = {
                            gameEngine.car = activeCar
                            gameEngine.mode = selectedMode
                            gameEngine.weatherTheme = weatherTheme
                            gameEngine.resetGame()
                            currentScreen = AppScreen.RACING
                        },
                        onOpenGarage = { currentScreen = AppScreen.GARAGE },
                        onOpenLeaderboard = { currentScreen = AppScreen.LEADERBOARD },
                        onOpenSettings = { showSettingsDialog = true }
                    )
                }

                AppScreen.RACING -> {
                    RacingScreen(
                        engine = gameEngine,
                        repository = repository,
                        currentHighScore = highScore ?: 0,
                        controlMode = controlMode,
                        onHome = { currentScreen = AppScreen.MAIN_MENU },
                        onGarage = { currentScreen = AppScreen.GARAGE }
                    )
                }

                AppScreen.GARAGE -> {
                    GarageScreen(
                        repository = repository,
                        currentCoins = coins,
                        selectedCarId = selectedCarId,
                        onBack = { currentScreen = AppScreen.MAIN_MENU }
                    )
                }

                AppScreen.LEADERBOARD -> {
                    LeaderboardScreen(
                        records = topRecords,
                        highScore = highScore ?: 0,
                        totalDistanceMeters = totalDistance ?: 0,
                        onBack = { currentScreen = AppScreen.MAIN_MENU }
                    )
                }
            }
        }

        if (showSettingsDialog) {
            SettingsDialog(
                soundEnabled = soundEnabled,
                onSoundChanged = {
                    soundEnabled = it
                    repository.setSoundEnabled(it)
                    soundManager.isSoundEnabled = it
                },
                hapticEnabled = hapticEnabled,
                onHapticChanged = {
                    hapticEnabled = it
                    repository.setHapticEnabled(it)
                    soundManager.isHapticEnabled = it
                },
                controlMode = controlMode,
                onControlModeChanged = {
                    controlMode = it
                    repository.setControlMode(it)
                },
                currentTheme = weatherTheme,
                onThemeChanged = {
                    weatherTheme = it
                },
                currentLanguage = appLanguage,
                onLanguageChanged = {
                    appLanguage = it
                    repository.setLanguage(it)
                },
                onClose = { showSettingsDialog = false }
            )
        }
    }
}
