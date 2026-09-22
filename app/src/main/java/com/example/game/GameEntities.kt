package com.example.game

import androidx.compose.ui.graphics.Color
import kotlin.random.Random

enum class GameMode(val title: String, val description: String) {
    ENDLESS("Sonsuz Otoyol", "Trafikten kaç, en uzun mesafeyi kat et ve rekor kır!"),
    TIME_ATTACK("Zamana Karşı", "Süre bitmeden kontrol noktalarına ulaşarak süreyi uzat!"),
    POLICE_CHASE("Polis Takibi", "Peşindeki polis arabalarını geride bırak!")
}

enum class WeatherTheme(val displayName: String, val skyColor: Color, val roadColor: Color, val stripeColor: Color) {
    NEON_NIGHT("Neon Gece", Color(0xFF070B14), Color(0xFF131824), Color(0xFF00F0FF)),
    SUNSET_GLOW("Gün Batımı", Color(0xFF2C0B1E), Color(0xFF1E1728), Color(0xFFFFB703)),
    CYBER_STORM("Siber Fırtına", Color(0xFF05131E), Color(0xFF0E1A24), Color(0xFF00FF66))
}

enum class TrafficType(val lengthFactor: Float, val baseSpeedKmh: Int, val widthFactor: Float) {
    SEDAN(1.0f, 105, 1.0f),
    SPORTS(0.95f, 145, 0.95f),
    TRUCK(1.7f, 85, 1.15f),
    POLICE(1.05f, 160, 1.0f)
}

data class TrafficVehicle(
    val id: Long,
    var lane: Int,
    var x: Float,
    var y: Float,
    val type: TrafficType,
    val color: Color,
    val speedKmh: Float,
    var hasTriggeredNearMiss: Boolean = false
)

enum class PickupType {
    COIN,
    NITRO,
    SHIELD,
    MAGNET,
    REPAIR
}

data class Collectible(
    val id: Long,
    var lane: Int,
    var x: Float,
    var y: Float,
    val type: PickupType
)

data class Particle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    val color: Color,
    val size: Float,
    var alpha: Float = 1.0f,
    val lifeMax: Float = 1.0f,
    var life: Float = 1.0f
)

data class FloatingNotice(
    val text: String,
    val color: Color,
    var x: Float,
    var y: Float,
    var alpha: Float = 1.0f,
    var life: Float = 1.0f
)
