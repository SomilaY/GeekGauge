package com.somila.geekgauge.domain.usecases

import com.somila.geekgauge.domain.enums.SessionStatus
import com.somila.geekgauge.domain.enums.SessionType
import com.somila.geekgauge.domain.enums.SyncStatus
import com.somila.geekgauge.domain.models.Session
import com.somila.geekgauge.domain.repository.SessionRepository
import java.util.UUID
import javax.inject.Inject

class StartSessionUseCase @Inject constructor(
    private val sessionRepository: SessionRepository
) {
    suspend operator fun invoke(
        geekId: String,
        trainerId: String,
        sessionType: SessionType
    ): Result<Session> {

        if (geekId.isBlank()) {
            return Result.failure(SessionException.InvalidGeek("Geek ID cannot be blank"))
        }
        if (trainerId.isBlank()) {
            return Result.failure(SessionException.InvalidTrainer("Trainer ID cannot be blank"))
        }

        val session = Session(
            id = UUID.randomUUID().toString(),
            geekId = geekId,
            trainerId = trainerId,
            type = sessionType,
            status = SessionStatus.IDLE,
            startTime = System.currentTimeMillis(),
            syncStatus = SyncStatus.PENDING
        )

        return sessionRepository.createSession(session)
    }
}

sealed class SessionException(message: String) : Exception(message) {
    class InvalidGeek(message: String) : SessionException(message)
    class InvalidTrainer(message: String) : SessionException(message)
    class CreateFailed(message: String) : SessionException(message)
}