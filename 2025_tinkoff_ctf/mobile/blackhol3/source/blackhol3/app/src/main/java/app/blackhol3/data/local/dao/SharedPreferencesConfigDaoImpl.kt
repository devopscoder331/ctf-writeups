package app.blackhol3.data.local.dao

import android.content.Context
import androidx.core.content.edit

class SharedPreferencesConfigDaoImpl(
    context: Context,
) : ConfigDao {
    private val preferences = context.getSharedPreferences("app_config", Context.MODE_PRIVATE)

    override fun getString(
        key: String,
        defaultValue: String?,
    ): String? = preferences.getString(key, defaultValue)

    override fun putString(
        key: String,
        value: String?,
    ) {
        preferences.edit { putString(key, value) }
    }

    override fun getLong(
        key: String,
        defaultValue: Long,
    ): Long = preferences.getLong(key, defaultValue)

    override fun putLong(
        key: String,
        value: Long,
    ) {
        preferences.edit { putLong(key, value) }
    }

    override fun remove(key: String) {
        preferences.edit { remove(key) }
    }
}
