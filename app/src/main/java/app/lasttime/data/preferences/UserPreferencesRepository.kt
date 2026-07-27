package app.lasttime.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import app.lasttime.domain.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.userPreferencesDataStore by preferencesDataStore(name = "user_preferences")

interface UserPreferencesRepository {
    val themeMode: Flow<ThemeMode>

    suspend fun setThemeMode(mode: ThemeMode)
}

internal class DataStoreUserPreferencesRepository(
    private val context: Context,
) : UserPreferencesRepository {
    override val themeMode: Flow<ThemeMode> =
        context.userPreferencesDataStore.data
            .catch { error ->
                if (error is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw error
                }
            }.map { preferences ->
                preferences[THEME_MODE_KEY]
                    ?.let { stored -> ThemeMode.entries.firstOrNull { it.name == stored } }
                    ?: ThemeMode.SYSTEM
            }

    override suspend fun setThemeMode(mode: ThemeMode) {
        context.userPreferencesDataStore.edit { preferences ->
            preferences[THEME_MODE_KEY] = mode.name
        }
    }

    private companion object {
        val THEME_MODE_KEY = stringPreferencesKey("theme_mode")
    }
}
