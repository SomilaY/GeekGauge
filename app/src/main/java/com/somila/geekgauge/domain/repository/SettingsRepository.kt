package com.somila.geekgauge.domain.repository

import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun areNotificationsEnabled(): Boolean
    suspend fun setNotificationsEnabled(enabled: Boolean)
    fun notificationsEnabledFlow(): Flow<Boolean>
}