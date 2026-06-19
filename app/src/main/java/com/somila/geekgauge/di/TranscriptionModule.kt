package com.somila.geekgauge.di

import com.somila.geekgauge.data.audio.SpeechRecognizerImpl
import com.somila.geekgauge.domain.TranscriptionService
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class TranscriptionModule {

    @Binds
    @Singleton
    abstract fun bindTranscriptionService(
        impl: SpeechRecognizerImpl
    ): TranscriptionService
}