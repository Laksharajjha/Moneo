package com.moneo.app.data.local.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class PreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        val APP_LOCK_ENABLED = booleanPreferencesKey("app_lock_enabled")
        val NOTIFICATION_LISTENER_ENABLED = booleanPreferencesKey("notification_listener_enabled")
        val MONITORED_APPS = androidx.datastore.preferences.core.stringSetPreferencesKey("monitored_apps")
    }

    val appLockEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[APP_LOCK_ENABLED] ?: false
    }

    suspend fun setAppLockEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[APP_LOCK_ENABLED] = enabled
        }
    }

    val notificationListenerEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[NOTIFICATION_LISTENER_ENABLED] ?: false
    }

    suspend fun setNotificationListenerEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[NOTIFICATION_LISTENER_ENABLED] = enabled
        }
    }

    val monitoredApps: Flow<Set<String>> = context.dataStore.data.map { preferences ->
        preferences[MONITORED_APPS] ?: setOf("com.google.android.apps.nbu.paisa.user", "com.phonepe.app", "com.sbi.upi")
    }

    suspend fun setMonitoredApps(apps: Set<String>) {
        context.dataStore.edit { preferences ->
            preferences[MONITORED_APPS] = apps
        }
    }
}
