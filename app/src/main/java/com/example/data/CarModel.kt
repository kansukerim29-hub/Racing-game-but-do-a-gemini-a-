package com.example.data

import androidx.compose.ui.graphics.Color

data class CarModel(
    val id: String,
    val name: String,
    val subtitle: String,
    val price: Int,
    val baseSpeed: Int, // km/h
    val baseAccel: Float,
    val baseHandling: Float,
    val baseNitroCapacity: Float,
    val primaryColor: Color,
    val secondaryColor: Color,
    val accentColor: Color,
    val isPolice: Boolean = false,
    val specialPerk: String,
    val speedLevel: Int = 1,
    val handlingLevel: Int = 1,
    val nitroLevel: Int = 1
) {
    val topSpeedKmh: Int
        get() = baseSpeed + (speedLevel - 1) * 8

    val handlingFactor: Float
        get() = (baseHandling + (handlingLevel - 1) * 0.15f).coerceAtMost(2.5f)

    val nitroDurationSec: Float
        get() = baseNitroCapacity + (nitroLevel - 1) * 1.0f

    val upgradeCost: Int
        get() = 120 + (speedLevel + handlingLevel + nitroLevel) * 40

    companion object {
        val ALL_CARS = listOf(
            CarModel(
                id = "apex_gt",
                name = "Apex GT",
                subtitle = "Dengeli Spor Araç",
                price = 0,
                baseSpeed = 190,
                baseAccel = 1.0f,
                baseHandling = 1.0f,
                baseNitroCapacity = 4.0f,
                primaryColor = Color(0xFFE50914),
                secondaryColor = Color(0xFF1E1E1E),
                accentColor = Color(0xFFFFFFFF),
                specialPerk = "Başlangıç için mükemmel kontrol"
            ),
            CarModel(
                id = "thunder_v8",
                name = "Thunder V8",
                subtitle = "Amerikan Kas Arabası",
                price = 300,
                baseSpeed = 210,
                baseAccel = 1.2f,
                baseHandling = 0.9f,
                baseNitroCapacity = 5.0f,
                primaryColor = Color(0xFF1A1D20),
                secondaryColor = Color(0xFFFFB703),
                accentColor = Color(0xFFFFB703),
                specialPerk = "Yüksek hız ve güçlü motor torku"
            ),
            CarModel(
                id = "venom_rs",
                name = "Venom RS",
                subtitle = "Viraj Canavarı",
                price = 700,
                baseSpeed = 225,
                baseAccel = 1.3f,
                baseHandling = 1.4f,
                baseNitroCapacity = 5.5f,
                primaryColor = Color(0xFF00E676),
                secondaryColor = Color(0xFF111827),
                accentColor = Color(0xFF76FF03),
                specialPerk = "Mıknatıs süresi +%50 ve süper kıvraklık"
            ),
            CarModel(
                id = "cyber_phantom",
                name = "Cyber Phantom",
                subtitle = "Fütüristik Hiper Araç",
                price = 1400,
                baseSpeed = 250,
                baseAccel = 1.5f,
                baseHandling = 1.3f,
                baseNitroCapacity = 7.0f,
                primaryColor = Color(0xFF00F0FF),
                secondaryColor = Color(0xFF0F172A),
                accentColor = Color(0xFFFF007F),
                specialPerk = "Nitro dolumu 2 kat hızlı ve +20 km/h ekstra hız"
            ),
            CarModel(
                id = "police_interceptor",
                name = "Interceptor 911",
                subtitle = "Sirenli Polis Arabası",
                price = 2200,
                baseSpeed = 240,
                baseAccel = 1.4f,
                baseHandling = 1.2f,
                baseNitroCapacity = 6.0f,
                primaryColor = Color(0xFFFFFFFF),
                secondaryColor = Color(0xFF1565C0),
                accentColor = Color(0xFFD50000),
                isPolice = true,
                specialPerk = "Çakar sirenler & ekstra dayanıklı gövde"
            )
        )

        val DEFAULT_CAR: CarModel get() = ALL_CARS[0]
    }
}
