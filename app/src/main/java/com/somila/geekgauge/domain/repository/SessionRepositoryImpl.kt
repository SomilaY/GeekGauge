package com.somila.geekgauge.domain.repository

import com.somila.geekgauge.data.local.dao.SessionDao
import com.somila.geekgauge.data.local.entities.SessionEntity
import com.somila.geekgauge.data.local.entities.toDomain
import com.somila.geekgauge.data.local.entities.toEntity
import com.somila.geekgauge.domain.models.Session
import com.somila.geekgauge.domain.repository.SessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionRepositoryImpl @Inject constructor(
    private val sessionDao: SessionDao
) : SessionRepository {

    override suspend fun createSession(session: Session): Result<Session> {
        return try {
            sessionDao.insertSession(session.toEntity())
            Result.success(session)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateSession(session: Session): Result<Session> {
        return try {
            sessionDao.updateSession(session.toEntity())
            Result.success(session)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getSessionById(sessionId: String): Session? {
        return sessionDao.getSessionById(sessionId)?.toDomain()
    }

    override fun getSessionsByGeekId(geekId: String): Flow<List<Session>> {
        return sessionDao.getSessionsByGeekId(geekId).map { entities ->
            entities.map { it.toDomain() }
        }
    }
}