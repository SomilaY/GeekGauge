package com.somila.geekgauge.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.somila.geekgauge.data.local.dao.ReportDao
import com.somila.geekgauge.data.local.dao.SessionDao
import com.somila.geekgauge.data.local.dao.TranscriptDao
import com.somila.geekgauge.data.local.entities.SessionEntity
import com.somila.geekgauge.data.local.entities.TranscriptEntity
import com.somila.geekgauge.data.local.entities.ReportEntity

@Database(
    entities = [SessionEntity::class, TranscriptEntity::class, ReportEntity::class],
    version = 3
)
abstract class GeekGaugeDatabase : RoomDatabase() {
    abstract fun sessionDao(): SessionDao
    abstract fun transcriptDao(): TranscriptDao
    abstract fun reportDao(): ReportDao
}