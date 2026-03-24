package com.example.personalaccountant.data.prefs

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferenceManager @Inject constructor(@ApplicationContext context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("financify_prefs", Context.MODE_PRIVATE)
    
    private val _isDarkMode = MutableStateFlow(prefs.getBoolean("dark_mode", false))
    val isDarkMode = _isDarkMode.asStateFlow()

    fun setDarkMode(enabled: Boolean) {
        prefs.edit().putBoolean("dark_mode", enabled).apply()
        _isDarkMode.value = enabled
    }

    fun toggleDarkMode() {
        setDarkMode(!_isDarkMode.value)
    }

    private val PREF_CUSTOM_CATEGORIES = "custom_categories"
    
    private val _customCategories = MutableStateFlow(
        prefs.getStringSet(PREF_CUSTOM_CATEGORIES, emptySet())?.toList() ?: emptyList()
    )
    val customCategories = _customCategories.asStateFlow()

    fun addCustomCategory(category: String) {
        val current = _customCategories.value.toMutableSet()
        if (current.add(category)) {
            prefs.edit().putStringSet(PREF_CUSTOM_CATEGORIES, current).apply()
            _customCategories.value = current.toList()
        }
    }
}
