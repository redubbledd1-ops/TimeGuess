package com.timeguess

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.DialogNavigator
import com.timeguess.data.local.LanguagePreferences
import com.timeguess.data.local.ThemePreferences
import com.timeguess.navigation.AppNavHost
import com.timeguess.ui.components.SUPPORTED_LANGUAGES
import com.timeguess.ui.theme.TimeGuessTheme
import com.timeguess.viewmodel.GameViewModel
import java.util.Locale

class MainActivity : ComponentActivity() {
    
    private lateinit var themePreferences: ThemePreferences
    private lateinit var languagePreferences: LanguagePreferences
    
    private fun clearAllAppData() {
        try {
            val prefsDir = applicationInfo.dataDir + "/shared_prefs"
            val dir = java.io.File(prefsDir)
            if (dir.exists() && dir.isDirectory) {
                dir.listFiles()?.forEach { it.delete() }
            }
        } catch (_: Exception) {}
        try {
            deleteDatabase("timeguess_database")
        } catch (_: Exception) {}
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(null)
        
        val initialLanguageCode = try {
            themePreferences = ThemePreferences(this)
            languagePreferences = LanguagePreferences(this)
            detectAndApplyLanguage(this, languagePreferences)
        } catch (_: Exception) {
            clearAllAppData()
            themePreferences = ThemePreferences(this)
            languagePreferences = LanguagePreferences(this)
            detectAndApplyLanguage(this, languagePreferences)
        }
        applyLanguage(initialLanguageCode)

        val dark = themePreferences.isDarkMode()
        enableEdgeToEdge(
            statusBarStyle = if (dark) SystemBarStyle.dark(Color.BLACK)
            else SystemBarStyle.light(Color.WHITE, Color.WHITE),
            navigationBarStyle = if (dark) SystemBarStyle.dark(Color.BLACK)
            else SystemBarStyle.light(Color.WHITE, Color.WHITE)
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }
        setContent {
            var isDarkMode by remember { mutableStateOf(themePreferences.isDarkMode()) }
            var currentLanguageCode by remember { mutableStateOf(languagePreferences.getLanguageCode()) }
            val navController = remember { NavHostController(this@MainActivity).apply {
                navigatorProvider.addNavigator(ComposeNavigator())
                navigatorProvider.addNavigator(DialogNavigator())
            } }
            val gameViewModel: GameViewModel = viewModel()
            
            TimeGuessTheme(darkMode = isDarkMode) {
                AppNavHost(
                    navController = navController,
                    viewModel = gameViewModel,
                    isDarkMode = isDarkMode,
                    onToggleTheme = { 
                        isDarkMode = !isDarkMode
                        themePreferences.setDarkMode(isDarkMode)
                    },
                    currentLanguageCode = currentLanguageCode,
                    onLanguageSelected = { languageCode ->
                        languagePreferences.setLanguageCode(languageCode)
                        currentLanguageCode = languageCode
                        applyLanguage(languageCode)
                        recreate()
                    }
                )
            }
        }
    }
    
    private fun applyLanguage(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = Configuration(resources.configuration)
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
    }

    private fun detectAndApplyLanguage(context: Context, prefs: LanguagePreferences): String {
        if (!prefs.isFirstLaunch()) {
            return prefs.getLanguageCode()
        }

        val supportedLanguages = SUPPORTED_LANGUAGES.map { it.code }.toSet()
        val normalizedSystemLang = Locale.getDefault().language.lowercase(Locale.ROOT)
        val languageToStore = if (normalizedSystemLang in supportedLanguages) {
            normalizedSystemLang
        } else {
            "en"
        }

        prefs.setLanguageCode(languageToStore)
        prefs.setFirstLaunchComplete()
        return languageToStore
    }
}
