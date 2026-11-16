package com.timeguess.data.local

import android.content.Context
import android.content.SharedPreferences

class RecentNamesStore(context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME, Context.MODE_PRIVATE
    )

    private fun removeKeyQuietly(key: String) {
        try {
            prefs.edit().remove(key).apply()
        } catch (_: Exception) {
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
    
    fun getRecentNames(): List<String> {
        val namesString = safeGetString(KEY_RECENT_NAMES, "")
        return if (namesString.isEmpty()) {
            emptyList()
        } else {
            namesString.split(SEPARATOR).filter { it.isNotBlank() }
        }
    }
    
    fun addNames(names: List<String>) {
        val currentNames = getRecentNames().toMutableList()
        val uniqueNewNames = names.filter { it.isNotBlank() && !it.startsWith("Player ") }
        
        uniqueNewNames.forEach { name ->
            currentNames.remove(name)
            currentNames.add(0, name)
        }
        
        val trimmedList = currentNames.take(MAX_RECENT_NAMES)

        try {
            prefs.edit()
                .putString(KEY_RECENT_NAMES, trimmedList.joinToString(SEPARATOR))
                .apply()
        } catch (_: Exception) {
        }
    }
    
    fun clearNames() {
        removeKeyQuietly(KEY_RECENT_NAMES)
    }
    
    companion object {
        private const val PREFS_NAME = "recent_names_prefs"
        private const val KEY_RECENT_NAMES = "recent_names"
        private const val SEPARATOR = "|||"
        private const val MAX_RECENT_NAMES = 20
    }
}
