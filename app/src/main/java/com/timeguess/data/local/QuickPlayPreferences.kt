package com.timeguess.data.local

import android.content.Context
import android.content.SharedPreferences
import com.timeguess.model.DurationMode
import com.timeguess.model.PlayMode

data class QuickPlayConfig(
    val playerCount: Int = 1,
    val baseTimeSeconds: Int = 10,
    val totalRounds: Int = 1,
    val durationMode: DurationMode = DurationMode.NORMAL,
    val playMode: PlayMode = PlayMode.ONE_BY_ONE,
    val incrementSeconds: Int = 5,
    val minTimeSeconds: Int = 1,
    val maxTimeSeconds: Int = 60,
    val playerNames: List<String> = listOf("Player 1")
)

class QuickPlayPreferences(context: Context) {
    
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
    
    fun hasConfig(): Boolean {
        return try {
            prefs.contains(KEY_PLAYER_COUNT)
        } catch (_: Exception) {
            false
        }
    }
    
    fun saveConfig(config: QuickPlayConfig) {
        try {
            prefs.edit().apply {
                putInt(KEY_PLAYER_COUNT, config.playerCount)
                putInt(KEY_BASE_TIME, config.baseTimeSeconds)
                putInt(KEY_TOTAL_ROUNDS, config.totalRounds)
                putString(KEY_DURATION_MODE, config.durationMode.name)
                putString(KEY_PLAY_MODE, config.playMode.name)
                putInt(KEY_INCREMENT, config.incrementSeconds)
                putInt(KEY_MIN_TIME, config.minTimeSeconds)
                putInt(KEY_MAX_TIME, config.maxTimeSeconds)
                putString(KEY_PLAYER_NAMES, config.playerNames.joinToString(SEPARATOR))
                apply()
            }
        } catch (_: Exception) {
        }
    }
    
    fun loadConfig(): QuickPlayConfig {
        return try {
            val playerCount = safeGetInt(KEY_PLAYER_COUNT, 1).coerceIn(1, 10)
            val baseTimeSeconds = safeGetInt(KEY_BASE_TIME, 10).coerceIn(1, 1800)
            val totalRounds = safeGetInt(KEY_TOTAL_ROUNDS, 1).coerceIn(1, 100)
            val durationMode = DurationMode.fromString(safeGetString(KEY_DURATION_MODE, "NORMAL"))
            val playMode = PlayMode.fromString(safeGetString(KEY_PLAY_MODE, "ONE_BY_ONE"))
            val incrementSeconds = safeGetInt(KEY_INCREMENT, 5).coerceIn(1, 60)
            val minTimeSeconds = safeGetInt(KEY_MIN_TIME, 1).coerceIn(1, 1800)
            val maxTimeSeconds = safeGetInt(KEY_MAX_TIME, 60).coerceIn(minTimeSeconds, 1800)

            val namesString = safeGetString(KEY_PLAYER_NAMES, "")
            val rawNames = if (namesString.isEmpty()) emptyList() else namesString.split(SEPARATOR)
            val normalizedNames = (0 until playerCount).map { index ->
                rawNames.getOrNull(index) ?: ""
            }.ifEmpty { listOf("Player 1") }

            QuickPlayConfig(
                playerCount = playerCount,
                baseTimeSeconds = baseTimeSeconds,
                totalRounds = totalRounds,
                durationMode = durationMode,
                playMode = playMode,
                incrementSeconds = incrementSeconds,
                minTimeSeconds = minTimeSeconds,
                maxTimeSeconds = maxTimeSeconds,
                playerNames = normalizedNames
            )
        } catch (_: Exception) {
            QuickPlayConfig()
        }
    }
    
    companion object {
        private const val PREFS_NAME = "quick_play_prefs"
        private const val KEY_PLAYER_COUNT = "player_count"
        private const val KEY_BASE_TIME = "base_time"
        private const val KEY_TOTAL_ROUNDS = "total_rounds"
        private const val KEY_DURATION_MODE = "duration_mode"
        private const val KEY_PLAY_MODE = "play_mode"
        private const val KEY_INCREMENT = "increment"
        private const val KEY_MIN_TIME = "min_time"
        private const val KEY_MAX_TIME = "max_time"
        private const val KEY_PLAYER_NAMES = "player_names"
        private const val SEPARATOR = "|||"
    }
}
