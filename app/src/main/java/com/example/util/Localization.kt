package com.example.util

import androidx.compose.runtime.compositionLocalOf
import java.util.Locale

enum class AppLanguage(val code: String, val displayNameEn: String, val displayNameTr: String) {
    SYSTEM("SYSTEM", "System Default", "Sistem Dili"),
    ENGLISH("EN", "English", "İngilizce"),
    TURKISH("TR", "Turkish", "Türkçe");

    fun getDisplayName(isTurkish: Boolean): String = if (isTurkish) displayNameTr else displayNameEn

    companion object {
        fun fromCode(code: String): AppLanguage {
            return entries.find { it.code.equals(code, ignoreCase = true) } ?: SYSTEM
        }
    }
}

/**
 * All localized string keys used throughout the game interface.
 */
data class Strings(
    // Common / Buttons
    val appName: String,
    val back: String,
    val close: String,
    val ok: String,
    val cancel: String,
    val select: String,
    val selected: String,
    val locked: String,
    val max: String,
    val lvl: String,
    val coins: String,
    val score: String,
    val distance: String,
    val speed: String,

    // Settings
    val settingsTitle: String,
    val soundEffects: String,
    val vibrationHaptics: String,
    val controlScheme: String,
    val controlButtons: String,
    val controlButtonsSubtitle: String,
    val controlDrag: String,
    val controlDragSubtitle: String,
    val languageSection: String,
    val trackAtmosphere: String,

    // Main Menu
    val highScore: String,
    val garageButton: String,
    val leaderboardButton: String,
    val selectGameMode: String,
    val startRace: String,
    val selectedCarHeader: String,
    val handling: String,
    val nitro: String,

    // Game Modes
    val modeEndlessTitle: String,
    val modeEndlessDesc: String,
    val modeTimeAttackTitle: String,
    val modeTimeAttackDesc: String,
    val modePoliceChaseTitle: String,
    val modePoliceChaseDesc: String,

    // Weather Themes
    val themeNeonNight: String,
    val themeSunsetHighway: String,
    val themeCyberRain: String,
    val themeDesertStorm: String,

    // Garage
    val garageTitle: String,
    val upgradeStat: String,
    val unlockCar: String,
    val specialPerk: String,
    val notEnoughCoins: String,
    val previous: String,
    val next: String,
    val unlocked: String,
    val perfAndUpgrades: String,
    val nitroDuration: String,
    val carEquipped: String,
    val equipCar: String,
    val buyCar: String,

    // Leaderboard
    val leaderboardTitle: String,
    val bestScore: String,
    val highestScore: String,
    val totalDistance: String,
    val topRaces: String,
    val noRecordsYet: String,
    val playFirstRace: String,
    val completeFirstRace: String,
    val rank: String,
    val pts: String,
    val km: String,
    val kmh: String,

    // HUD
    val tapToBoost: String,
    val brake: String,
    val boostReady: String,
    val timeRemaining: String,
    val nearMissCombo: String,
    val dragToSteer: String,

    // Pause Dialog
    val gamePaused: String,
    val resume: String,
    val restart: String,
    val garage: String,
    val mainMenu: String,

    // Game Over Dialog
    val crashed: String,
    val timesUp: String,
    val newHighScoreAlert: String,
    val raceSummary: String,
    val finalScore: String,
    val distanceCovered: String,
    val coinsCollected: String,
    val maxSpeed: String,
    val nearMisses: String,
    val playAgain: String
)

val EnglishStrings = Strings(
    appName = "Turbo Racer",
    back = "Back",
    close = "Close",
    ok = "OK",
    cancel = "Cancel",
    select = "SELECT",
    selected = "SELECTED",
    locked = "LOCKED",
    max = "MAX",
    lvl = "LVL",
    coins = "Coins",
    score = "Score",
    distance = "Distance",
    speed = "Speed",

    settingsTitle = "SETTINGS",
    soundEffects = "Game Sounds",
    vibrationHaptics = "Vibration & Haptics",
    controlScheme = "CONTROL SCHEME",
    controlButtons = "Buttons",
    controlButtonsSubtitle = "On-screen keys",
    controlDrag = "Drag",
    controlDragSubtitle = "Touch & steer",
    languageSection = "LANGUAGE",
    trackAtmosphere = "TRACK ATMOSPHERE",

    highScore = "HIGH SCORE",
    garageButton = "GARAGE",
    leaderboardButton = "LEADERBOARD",
    selectGameMode = "SELECT GAME MODE",
    startRace = "START RACE",
    selectedCarHeader = "SELECTED CAR",
    handling = "Handling",
    nitro = "Nitro",

    modeEndlessTitle = "Endless Highway",
    modeEndlessDesc = "No time limit, survive dense highway traffic",
    modeTimeAttackTitle = "Time Attack",
    modeTimeAttackDesc = "Survive & cover maximum distance within 60 seconds",
    modePoliceChaseTitle = "Police Chase",
    modePoliceChaseDesc = "Sirens blazing, evade & overtake runaway traffic",

    themeNeonNight = "Neon Night",
    themeSunsetHighway = "Sunset Highway",
    themeCyberRain = "Cyber Rain",
    themeDesertStorm = "Desert Storm",

    garageTitle = "VEHICLE GARAGE",
    upgradeStat = "UPGRADE",
    unlockCar = "UNLOCK",
    specialPerk = "Perk",
    notEnoughCoins = "Not enough coins!",
    previous = "Previous",
    next = "Next",
    unlocked = "UNLOCKED",
    perfAndUpgrades = "PERFORMANCE & UPGRADES",
    nitroDuration = "Nitro Duration",
    carEquipped = "CAR EQUIPPED",
    equipCar = "SELECT THIS CAR",
    buyCar = "BUY (%d COINS)",

    leaderboardTitle = "HALL OF FAME",
    bestScore = "BEST SCORE",
    highestScore = "Highest Score",
    totalDistance = "TOTAL DISTANCE",
    topRaces = "TOP RACES",
    noRecordsYet = "No race records yet",
    playFirstRace = "Complete your first race to record high scores!",
    completeFirstRace = "Complete your first race and set a record!",
    rank = "RANK",
    pts = "pts",
    km = "km",
    kmh = "km/h",

    tapToBoost = "TAP TO BOOST",
    brake = "BRAKE",
    boostReady = "NITRO READY",
    timeRemaining = "TIME",
    nearMissCombo = "COMBO",
    dragToSteer = "Drag finger to steer car",

    gamePaused = "GAME PAUSED",
    resume = "RESUME",
    restart = "RESTART",
    garage = "GARAGE",
    mainMenu = "MAIN MENU",

    crashed = "CRASHED!",
    timesUp = "TIME'S UP!",
    newHighScoreAlert = "NEW HIGH SCORE!",
    raceSummary = "RACE SUMMARY",
    finalScore = "Final Score",
    distanceCovered = "Distance",
    coinsCollected = "Coins Earned",
    maxSpeed = "Max Speed",
    nearMisses = "Near Misses",
    playAgain = "PLAY AGAIN"
)

val TurkishStrings = Strings(
    appName = "Turbo Racer",
    back = "Geri",
    close = "Kapat",
    ok = "TAMAM",
    cancel = "İptal",
    select = "SEÇ",
    selected = "SEÇİLDİ",
    locked = "KİLİTLİ",
    max = "MAKS",
    lvl = "SEVİYE",
    coins = "Altın",
    score = "Skor",
    distance = "Mesafe",
    speed = "Hız",

    settingsTitle = "AYARLAR",
    soundEffects = "Oyun Sesleri",
    vibrationHaptics = "Titreşim & Hissiyat",
    controlScheme = "KONTROL TİPİ",
    controlButtons = "Düğmeler",
    controlButtonsSubtitle = "Ekran tuşları",
    controlDrag = "Sürükle",
    controlDragSubtitle = "Parmağınla sür",
    languageSection = "DİL SEÇENEĞİ",
    trackAtmosphere = "PİST ATMOSFERİ",

    highScore = "YÜKSEK SKOR",
    garageButton = "GARAJ",
    leaderboardButton = "LİDERLİK",
    selectGameMode = "MOD SEÇİNİZ",
    startRace = "YARIŞA BAŞLA",
    selectedCarHeader = "SEÇİLEN ARAÇ",
    handling = "Yol Tutuş",
    nitro = "Nitro",

    modeEndlessTitle = "Sonsuz Otoyol",
    modeEndlessDesc = "Süre limiti yok, yoğun trafikte hayatta kal",
    modeTimeAttackTitle = "Zamana Karşı",
    modeTimeAttackDesc = "60 saniyede en yüksek mesafeyi katet",
    modePoliceChaseTitle = "Polis Takibi",
    modePoliceChaseDesc = "Polis sirenleri açık, kaçak araçları solla",

    themeNeonNight = "Neon Gece",
    themeSunsetHighway = "Günbatımı Otoyolu",
    themeCyberRain = "Siber Yağmur",
    themeDesertStorm = "Çöl Fırtınası",

    garageTitle = "ARAÇ GARAJI",
    upgradeStat = "GELİŞTİR",
    unlockCar = "AÇ",
    specialPerk = "Özel Güç",
    notEnoughCoins = "Yetersiz altın!",
    previous = "Önceki",
    next = "Sonraki",
    unlocked = "AÇIK",
    perfAndUpgrades = "PERFORMANS VE GELİŞTİRMELER",
    nitroDuration = "Nitro Süresi",
    carEquipped = "BU ARAÇ KULLANILIYOR",
    equipCar = "BU ARACI SEÇ",
    buyCar = "SATIN AL (%d COIN)",

    leaderboardTitle = "ŞAMPİYONLAR TABLOSU",
    bestScore = "EN YÜKSEK SKOR",
    highestScore = "En Yüksek Skor",
    totalDistance = "TOPLAM MESAFE",
    topRaces = "EN İYİ YARIŞLAR",
    noRecordsYet = "Henüz yarış kaydı bulunmuyor",
    playFirstRace = "Skor tablosunda yer almak için ilk yarışını tamamla!",
    completeFirstRace = "İlk yarışını tamamla ve rekor kır!",
    rank = "SIRA",
    pts = "puan",
    km = "km",
    kmh = "km/s",

    tapToBoost = "NİTRO ATEŞLE",
    brake = "FREN",
    boostReady = "NİTRO HAZIR",
    timeRemaining = "SÜRE",
    nearMissCombo = "KOMBO",
    dragToSteer = "Parmağını kaydırarak aracı yönlendir",

    gamePaused = "OYUN DURAKLATILDI",
    resume = "DEVAM ET",
    restart = "YENİDEN BAŞLAT",
    garage = "GARAJ",
    mainMenu = "ANA MENÜ",

    crashed = "KAZA YAPTIN!",
    timesUp = "SÜRE DOLDU!",
    newHighScoreAlert = "YENİ REKOR!",
    raceSummary = "YARIŞ RAPORU",
    finalScore = "Toplam Skor",
    distanceCovered = "Mesafe",
    coinsCollected = "Kazanılan Altın",
    maxSpeed = "Maksimum Hız",
    nearMisses = "Teğet Geçişler",
    playAgain = "TEKRAR OYNA"
)

/**
 * Resolves the effective [Strings] based on user preference and device system language.
 */
fun resolveStrings(language: AppLanguage): Strings {
    val isTurkish = when (language) {
        AppLanguage.SYSTEM -> {
            val systemLang = Locale.getDefault().language.lowercase()
            systemLang.startsWith("tr")
        }
        AppLanguage.TURKISH -> true
        AppLanguage.ENGLISH -> false
    }
    return if (isTurkish) TurkishStrings else EnglishStrings
}

val LocalAppStrings = compositionLocalOf { EnglishStrings }
