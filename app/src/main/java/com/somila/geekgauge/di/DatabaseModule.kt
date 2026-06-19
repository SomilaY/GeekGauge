package com.somila.geekgauge.di

import android.content.Context
import androidx.room.Room
import com.somila.geekgauge.data.local.GeekGaugeDatabase
import com.somila.geekgauge.data.local.dao.ReportDao
import com.somila.geekgauge.data.local.dao.SessionDao
import com.somila.geekgauge.data.local.dao.TranscriptDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): GeekGaugeDatabase {
        return Room.databaseBuilder(
            context,
            GeekGaugeDatabase::class.java,
            "Geek_Gauge_db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideSessionDao(db: GeekGaugeDatabase): SessionDao = db.sessionDao()

    @Provides
    fun provideTranscriptDao(db: GeekGaugeDatabase): TranscriptDao = db.transcriptDao()

    @Provides
    fun provideReportDao(db: GeekGaugeDatabase): ReportDao = db.reportDao()
}