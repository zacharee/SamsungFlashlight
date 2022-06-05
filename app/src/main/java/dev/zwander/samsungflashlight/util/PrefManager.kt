package dev.zwander.samsungflashlight.util

import android.annotation.SuppressLint
import android.content.Context
import android.content.ContextWrapper
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import dev.zwander.samsungflashlight.R

val Context.prefManager: PrefManager
    get() = PrefManager.getInstance(this)

class PrefManager private constructor(context: Context) : ContextWrapper(context) {
    companion object {
        @SuppressLint("StaticFieldLeak")
        private var instance: PrefManager? = null

        fun getInstance(context: Context): PrefManager {
            return instance ?: PrefManager(context).apply {
                instance = this
            }
        }
    }

    object Keys {
        const val KEY_LIGHT_STRENGTH = "light_strength"
    }

    private val prefs = PreferenceManager.getDefaultSharedPreferences(this)

    var lightStrength: Int
        get() = prefs.getInt(Keys.KEY_LIGHT_STRENGTH, resources.getInteger(R.integer.default_strength))
        set(value) {
            prefs.edit { putInt(Keys.KEY_LIGHT_STRENGTH, value) }
        }

    fun registerOnSharedPreferenceChangedListener(listener: OnSharedPreferenceChangeListener) {
        prefs.registerOnSharedPreferenceChangeListener(listener)
    }

    fun unregisterOnSharedPreferencesChangedListener(listener: OnSharedPreferenceChangeListener) {
        prefs.unregisterOnSharedPreferenceChangeListener(listener)
    }
}