package com.timeguess.data.local

import android.content.Context
import android.content.SharedPreferences
import com.timeguess.model.DurationMode

data class GameSettings(
    val baseTimeSeconds: Int = 10,
    val totalRounds: Int = 1,
    val durationMode: DurationMode = DurationMode.NORMAL,
    val incrementSeconds: Int = 5,
    val minTimeSeconds: Int = 1,
    val maxTimeSeconds: Int = 60,
    val customTimeInput: Int = 10
)

class SettingsDataStore(context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME, Context.MODE_PRIVATE
    )

    private fun removeKeyQuietly(key: String) {
        try {
            prefs.edit().remove(key).apply()
        } catch (_: Exception) {
        }
    }

    private fun safeGetInt(key: String, default: Int): Int {
        return try {
            prefs.getInt(key, default)
        } catch (_: Exception) {
            removeKeyQuietly(key)
            default
        }
    }

    private fun safeGetString(key: String, default: String): String {
        return try {
            prefs.getString(key, default) ?: default
        } catch (_: Exception) {
            removeKeyQuietly(key)
            default
        }
    }
    
    fun saveSettings(settings: GameSettings) {
        try {
            prefs.edit().apply {
                putInt(KEY_BASE_TIME, settings.baseTimeSeconds)
                putInt(KEY_TOTAL_ROUNDS, settings.totalRounds)
                putString(KEY_DURATION_MODE, settings.durationMode.name)
                putInt(KEY_INCREMENT, settings.incrementSeconds)
                putInt(KEY_MIN_TIME, settings.minTimeSeconds)
                putInt(KEY_MAX_TIME, settings.maxTimeSeconds)
                putInt(KEY_CUSTOM_TIME, settings.customTimeInput)
                apply()
            }
        } catch (_: Exception) {
        }
    }
    
    fun loadSettings(): GameSettings {
        return try {
            val baseTimeSeconds = safeGetInt(KEY_BASE_TIME, 10).coerceIn(1, 1800)
            val totalRounds = safeGetInt(KEY_TOTAL_ROUNDS, 1).coerceIn(1, 100)
            val durationMode = DurationMode.fromString(safeGetString(KEY_DURATION_MODE, "NORMAL"))
            val incrementSeconds = safeGetInt(KEY_INCREMENT, 5).coerceIn(1, 60)
            val minTimeSeconds = safeGetInt(KEY_MIN_TIME, 1).coerceIn(1, 1800)
            val maxTimeSeconds = safeGetInt(KEY_MAX_TIME, 60).coerceIn(minTimeSeconds, 1800)
            val customTimeInput = safeGetInt(KEY_CUSTOM_TIME, 10).coerceIn(1, 1800)

            GameSettings(
                baseTimeSeconds = baseTimeSeconds,
                totalRounds = totalRounds,
                durationMode = durationMode,
                incrementSeconds = incrementSeconds,
                minTimeSeconds = minTimeSeconds,
                maxTimeSeconds = maxTimeSeconds,
                customTimeInput = customTimeInput
            )
        } catch (_: Exception) {
            GameSettings()
        }
    }
    
    fun hasSettings(): Boolean {
        return try {
            prefs.contains(KEY_BASE_TIME)
        } catch (_: Exception) {
            false
        }
    }
    
    fun clearAllSettings() {
        try {
            prefs.edit().clear().apply()
        } catch (_: Exception) {}
    }
    
    fun saveBaseTime(seconds: Int) {
        try {
            prefs.edit().putInt(KEY_BASE_TIME, seconds.coerceIn(1, 1800)).apply()
        } catch (_: Exception) {}
    }
    
    fun saveDurationMode(mode: DurationMode) {
        try {
            prefs.edit().putString(KEY_DURATION_MODE, mode.name).apply()
        } catch (_: Exception) {}
    }
    
    fun saveTotalRounds(rounds: Int) {
        try {
            prefs.edit().putInt(KEY_TOTAL_ROUNDS, rounds.coerceIn(1, 100)).apply()
        } catch (_: Exception) {}
    }
    
    
    fun saveIncrement(seconds: Int) {
        try {
            prefs.edit().putInt(KEY_INCREMENT, seconds.coerceIn(1, 60)).apply()
        } catch (_: Exception) {}
    }
    
    fun saveMinTime(seconds: Int) {
        try {
            prefs.edit().putInt(KEY_MIN_TIME, seconds.coerceIn(1, 1800)).apply()
        } catch (_: Exception) {}
    }
    
    fun saveMaxTime(seconds: Int) {
        try {
            prefs.edit().putInt(KEY_MAX_TIME, seconds.coerceIn(1, 1800)).apply()
        } catch (_: Exception) {}
    }
    
    companion object {
        private const val PREFS_NAME = "game_settings_prefs"
        private const val KEY_BASE_TIME = "base_time"
        private const val KEY_TOTAL_ROUNDS = "total_rounds"
        private const val KEY_DURATION_MODE = "duration_mode"
        private const val KEY_INCREMENT = "increment"
        private const val KEY_MIN_TIME = "min_time"
        private const val KEY_MAX_TIME = "max_time"
        private const val KEY_CUSTOM_TIME = "custom_time"
    }
}
