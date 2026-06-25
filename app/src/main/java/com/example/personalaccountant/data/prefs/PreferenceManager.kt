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

    fun removeCustomCategory(category: String) {
        val current = _customCategories.value.toMutableSet()
        if (current.remove(category)) {
            prefs.edit().putStringSet(PREF_CUSTOM_CATEGORIES, current).apply()
            _customCategories.value = current.toList()
        }
    }

    // Simulator Preferences
    fun getSimLoan(): String = prefs.getString("sim_loan", "170000") ?: "170000"
    fun setSimLoan(value: String) = prefs.edit().putString("sim_loan", value).apply()

    fun getSimInterest(): String = prefs.getString("sim_interest", "33") ?: "33"
    fun setSimInterest(value: String) = prefs.edit().putString("sim_interest", value).apply()

    fun getSimTerm(): String = prefs.getString("sim_term", "5") ?: "5"
    fun setSimTerm(value: String) = prefs.edit().putString("sim_term", value).apply()

    fun getSimSalary(): String = prefs.getString("sim_salary", "500000") ?: "500000"
    fun setSimSalary(value: String) = prefs.edit().putString("sim_salary", value).apply()
}
