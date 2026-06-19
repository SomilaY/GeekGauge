package com.somila.geekgauge.di

import com.somila.geekgauge.data.repository.AuthRepositoryImpl
import com.somila.geekgauge.data.repository.CohortRepositoryImpl
import com.somila.geekgauge.data.repository.ReportRepositoryImpl
import com.somila.geekgauge.domain.repository.AuthRepository
import com.somila.geekgauge.domain.repository.CohortRepository
import com.somila.geekgauge.domain.repository.ReportRepository
import com.somila.geekgauge.domain.repository.SessionRepository
import com.somila.geekgauge.domain.repository.SessionRepositoryImpl
import com.somila.geekgauge.domain.repository.TranscriptRepository
import com.somila.geekgauge.domain.repository.TranscriptRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindSessionRepository(
        impl: SessionRepositoryImpl
    ): SessionRepository

    @Binds
    @Singleton
    abstract fun bindTranscriptRepository(
        impl: TranscriptRepositoryImpl
    ): TranscriptRepository

    @Binds
    @Singleton
    abstract fun bindReportRepository(
        impl: ReportRepositoryImpl
    ): ReportRepository

    @Binds
    @Singleton
    abstract fun bindCohortRepository(
        impl: CohortRepositoryImpl
    ): CohortRepository

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        impl: AuthRepositoryImpl
    ): AuthRepository
}