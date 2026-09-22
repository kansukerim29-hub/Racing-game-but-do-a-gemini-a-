package com.example.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class GameRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("turbo_racer_prefs", Context.MODE_PRIVATE)
    private val db = AppDatabase.getDatabase(context)
    private val dao = db.raceRecordDao()

    private val _coinsFlow = MutableStateFlow(getCoins())
    val coinsFlow = _coinsFlow.asStateFlow()

    private val _selectedCarIdFlow = MutableStateFlow(getSelectedCarId())
    val selectedCarIdFlow = _selectedCarIdFlow.asStateFlow()

    fun getCoins(): Int = prefs.getInt(KEY_COINS, 100) // 100 initial bonus coins for players

    fun addCoins(amount: Int) {
        val current = getCoins()
        val updated = current + amount
        prefs.edit().putInt(KEY_COINS, updated).apply()
        _coinsFlow.value = updated
    }

    fun spendCoins(amount: Int): Boolean {
        val current = getCoins()
        if (current >= amount) {
            val updated = current - amount
            prefs.edit().putInt(KEY_COINS, updated).apply()
            _coinsFlow.value = updated
            return true
        }
        return false
    }

    fun getUnlockedCarIds(): Set<String> {
        val saved = prefs.getStringSet(KEY_UNLOCKED_CARS, null)
        return saved ?: setOf("apex_gt")
    }

    fun unlockCar(carId: String) {
        val current = getUnlockedCarIds().toMutableSet()
        current.add(carId)
        prefs.edit().putStringSet(KEY_UNLOCKED_CARS, current).apply()
    }

    fun getSelectedCarId(): String {
        return prefs.getString(KEY_SELECTED_CAR, "apex_gt") ?: "apex_gt"
    }

    fun setSelectedCarId(carId: String) {
        prefs.edit().putString(KEY_SELECTED_CAR, carId).apply()
        _selectedCarIdFlow.value = carId
    }

    fun getCarUpgradeLevels(carId: String): Triple<Int, Int, Int> {
        val speed = prefs.getInt("upgrade_${carId}_speed", 1)
        val handling = prefs.getInt("upgrade_${carId}_handling", 1)
        val nitro = prefs.getInt("upgrade_${carId}_nitro", 1)
        return Triple(speed, handling, nitro)
    }

    fun upgradeCarStat(carId: String, stat: String, cost: Int): Boolean {
        if (spendCoins(cost)) {
            val current = prefs.getInt("upgrade_${carId}_$stat", 1)
            prefs.edit().putInt("upgrade_${carId}_$stat", current + 1).apply()
            return true
        }
        return false
    }

    fun getSoundEnabled(): Boolean = prefs.getBoolean(KEY_SOUND, true)
    fun setSoundEnabled(enabled: Boolean) = prefs.edit().putBoolean(KEY_SOUND, enabled).apply()

    fun getHapticEnabled(): Boolean = prefs.getBoolean(KEY_HAPTIC, true)
    fun setHapticEnabled(enabled: Boolean) = prefs.edit().putBoolean(KEY_HAPTIC, enabled).apply()

    fun getControlMode(): String = prefs.getString(KEY_CONTROL_MODE, "BUTTONS") ?: "BUTTONS"
    fun setControlMode(mode: String) = prefs.edit().putString(KEY_CONTROL_MODE, mode).apply()

    fun getLanguage(): com.example.util.AppLanguage {
        val code = prefs.getString(KEY_LANGUAGE, com.example.util.AppLanguage.SYSTEM.code) ?: com.example.util.AppLanguage.SYSTEM.code
        return com.example.util.AppLanguage.fromCode(code)
    }

    fun setLanguage(language: com.example.util.AppLanguage) {
        prefs.edit().putString(KEY_LANGUAGE, language.code).apply()
    }

    // Database access
    fun getTopRecords(limit: Int = 10): Flow<List<RaceRecord>> = dao.getTopRecords(limit)
    fun getHighScore(): Flow<Int?> = dao.getHighScore()
    fun getTotalDistance(): Flow<Int?> = dao.getTotalDistanceDriven()

    suspend fun saveRecord(record: RaceRecord): Long {
        return dao.insertRecord(record)
    }

    companion object {
        private const val KEY_COINS = "coins"
        private const val KEY_UNLOCKED_CARS = "unlocked_cars"
        private const val KEY_SELECTED_CAR = "selected_car"
        private const val KEY_SOUND = "sound_enabled"
        private const val KEY_HAPTIC = "haptic_enabled"
        private const val KEY_CONTROL_MODE = "control_mode"
        private const val KEY_LANGUAGE = "language"
    }
}
