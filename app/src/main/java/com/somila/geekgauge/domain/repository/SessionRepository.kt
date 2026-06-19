package com.somila.geekgauge.domain.repository

import com.somila.geekgauge.domain.models.Session
import kotlinx.coroutines.flow.Flow

interface SessionRepository {
    suspend fun createSession(session: Session): Result<Session>
    suspend fun updateSession(session: Session): Result<Session>
    suspend fun getSessionById(sessionId: String): Session?
    fun getSessionsByGeekId(geekId: String): Flow<List<Session>>
}