package com.trial.playlistmaker.tools

import android.app.Application
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate

const val DARK_THEME_SHARED_PREFERENCES = "app_dark_theme_preference"
const val SETTING_THEME = "state_of_dark_theme"

class App : Application() {
    var nightTheme: Boolean = false
    lateinit var settingsSharedPrefs: SharedPreferences

    override fun onCreate() {
        super.onCreate()

        settingsSharedPrefs = getSharedPreferences(DARK_THEME_SHARED_PREFERENCES, MODE_PRIVATE)
        nightTheme = settingsSharedPrefs.getBoolean(SETTING_THEME, false)
        switchNightTheme(nightTheme)
    }

    fun switchNightTheme(nightThemeEnabled: Boolean) {
        nightTheme = nightThemeEnabled
        AppCompatDelegate.setDefaultNightMode(
            if (nightThemeEnabled) {
                AppCompatDelegate.MODE_NIGHT_YES
            } else {
                AppCompatDelegate.MODE_NIGHT_NO
            }
        )
    }
}