package com.timeguess.data.local

import android.content.Context
import android.content.SharedPreferences

class ThemePreferences(context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME, Context.MODE_PRIVATE
    )

    private fun removeKeyQuietly(key: String) {
        try {
            prefs.edit().remove(key).apply()
        } catch (_: Exception) {
        }
    }

    private fun safeGetBoolean(key: String, default: Boolean): Boolean {
        return try {
            prefs.getBoolean(key, default)
        } catch (_: Exception) {
            removeKeyQuietly(key)
            default
        }
    }
    
    fun isDarkMode(): Boolean {
        return safeGetBoolean(KEY_DARK_MODE, true)
    }
    
    fun setDarkMode(isDark: Boolean) {
        try {
            prefs.edit().putBoolean(KEY_DARK_MODE, isDark).apply()
        } catch (_: Exception) {
        }
    }
    
    companion object {
        private const val PREFS_NAME = "theme_prefs"
        private const val KEY_DARK_MODE = "dark_mode"
    }
}
