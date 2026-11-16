package com.timeguess.data.local

import android.content.Context
import android.content.SharedPreferences

class LanguagePreferences(context: Context) {
    
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

    private fun safeGetString(key: String, default: String): String {
        return try {
            prefs.getString(key, default) ?: default
        } catch (_: Exception) {
            removeKeyQuietly(key)
            default
        }
    }
    
    fun isFirstLaunch(): Boolean {
        return safeGetBoolean(KEY_FIRST_LAUNCH, true)
    }
    
    fun setFirstLaunchComplete() {
        try {
            prefs.edit().putBoolean(KEY_FIRST_LAUNCH, false).apply()
        } catch (_: Exception) {
        }
    }
    
    fun getLanguageCode(): String {
        return safeGetString(KEY_LANGUAGE, "en")
    }
    
    fun setLanguageCode(code: String) {
        try {
            prefs.edit().putString(KEY_LANGUAGE, code).apply()
        } catch (_: Exception) {
        }
    }
    
    companion object {
        private const val PREFS_NAME = "language_prefs"
        private const val KEY_FIRST_LAUNCH = "first_launch"
        private const val KEY_LANGUAGE = "language_code"
    }
}
