package com.somila.geekgauge.di

import com.somila.geekgauge.mock.MockAuthDataSource
import com.somila.geekgauge.data.mock.MockCohortDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MockModule {

    @Provides
    @Singleton
    fun provideAuthDataSource(): MockAuthDataSource = MockAuthDataSource()

    @Provides
    @Singleton
    fun provideGeekGaugeDataSource(authDataSource: MockAuthDataSource): MockCohortDataSource {
        return MockCohortDataSource(authDataSource.getAllUsers())
    }
}
