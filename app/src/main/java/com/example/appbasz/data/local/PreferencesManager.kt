package com.example.appbasz.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
val FAVORITES_KEY = stringSetPreferencesKey("user_favorites")
val SEARCH_HISTORY_KEY = stringSetPreferencesKey("search_history")
val PROFILE_IMAGE_URI_KEY = stringPreferencesKey("profile_image_uri")
class PreferencesManager(context: Context) {
    private val dataStore = context.dataStore

    companion object {
        val DARK_THEME_KEY = booleanPreferencesKey("dark_theme")
        val NOTIFICATIONS_KEY = booleanPreferencesKey("notifications_enabled")
        val USER_LOGGED_IN_KEY = booleanPreferencesKey("user_logged_in")
    }

    // Tema
    suspend fun setDarkThemeEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[DARK_THEME_KEY] = enabled
        }
    }

    fun getDarkThemeEnabled(): Flow<Boolean> {
        return dataStore.data.map { preferences ->
            preferences[DARK_THEME_KEY] ?: false
        }
    }

    // Notificaciones
    suspend fun setNotificationsEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[NOTIFICATIONS_KEY] = enabled
        }
    }

    fun getNotificationsEnabled(): Flow<Boolean> {
        return dataStore.data.map { preferences ->
            preferences[NOTIFICATIONS_KEY] ?: true
        }
    }

    // Sesión de usuario
    suspend fun setUserLoggedIn(loggedIn: Boolean) {
        dataStore.edit { preferences ->
            preferences[USER_LOGGED_IN_KEY] = loggedIn
        }
    }

    fun getUserLoggedIn(): Flow<Boolean> {
        return dataStore.data.map { preferences ->
            preferences[USER_LOGGED_IN_KEY] ?: false
        }
    }

    // Favoritos
    suspend fun addToFavorites(productId: String) {
        dataStore.edit { preferences ->
            val currentFavorites = preferences[FAVORITES_KEY] ?: emptySet()
            val updatedFavorites = currentFavorites + productId
            preferences[FAVORITES_KEY] = updatedFavorites
        }
    }

    suspend fun removeFromFavorites(productId: String) {
        dataStore.edit { preferences ->
            val currentFavorites = preferences[FAVORITES_KEY] ?: emptySet()
            val updatedFavorites = currentFavorites - productId
            preferences[FAVORITES_KEY] = updatedFavorites
        }
    }

    fun getFavorites(): Flow<Set<String>> {
        return dataStore.data.map { preferences ->
            preferences[FAVORITES_KEY] ?: emptySet()
        }
    }
    // Historial de búsquedas
    suspend fun addToSearchHistory(query: String) {
        dataStore.edit { preferences ->
            val currentHistory = preferences[SEARCH_HISTORY_KEY] ?: emptySet()
            val updatedHistory = (currentHistory.toList() + query).takeLast(10).toSet()
            preferences[SEARCH_HISTORY_KEY] = updatedHistory
        }
    }
    fun getSearchHistory(): Flow<Set<String>> {
        return dataStore.data.map { preferences ->
            preferences[SEARCH_HISTORY_KEY] ?: emptySet()
        }
    }
    suspend fun clearSearchHistory() {
        dataStore.edit { preferences ->
            preferences.remove(SEARCH_HISTORY_KEY)
        }
    }
    //Imagen perfil
    suspend fun saveProfileImageUri(uri: String) {
        dataStore.edit { preferences ->
            preferences[PROFILE_IMAGE_URI_KEY] = uri
        }
    }

    suspend fun removeProfileImage() {
        dataStore.edit { preferences ->
            preferences.remove(PROFILE_IMAGE_URI_KEY)
        }
    }

    fun getProfileImageUri(): Flow<String> {
        return dataStore.data.map { preferences ->
            preferences[PROFILE_IMAGE_URI_KEY] ?: ""
        }
    }
}