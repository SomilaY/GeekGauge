package com.somila.geekgauge.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.somila.geekgauge.domain.repository.SettingsRepository
import com.somila.geekgauge.presentation.settings.dataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : SettingsRepository {

    private val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")

    override fun areNotificationsEnabled(): Boolean {
        // runBlocking is acceptable here since it's called from
        // NotificationHelper which is already off the main thread
        return runBlocking {
            context.dataStore.data
                .map { prefs -> prefs[NOTIFICATIONS_ENABLED] ?: true }
                .first()
        }
    }

    override suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[NOTIFICATIONS_ENABLED] = enabled
        }
    }

    override fun notificationsEnabledFlow(): Flow<Boolean> {
        return context.dataStore.data
            .map { prefs -> prefs[NOTIFICATIONS_ENABLED] ?: true }
    }
}