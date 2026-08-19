package com.example.myapplication_prueba

import com.russhwolf.settings.Settings

object SettingsManager {
    private val settings: Settings = Settings()

    const val BIOMETRIC_TOKEN = "biometric_token"
    const val USER_EMAIL = "user_email"
    const val SESSION_TOKEN = "session_token"
    const val SCREEN_STATE = "screen_state"
    const val USER_ROLE = "user_role"

    fun saveString(key: String, value: String) {
        settings.putString(key, value)
    }

    fun getString(key: String): String? {
        return settings.getStringOrNull(key)
    }

    fun clear() {
        settings.clear()
    }
}