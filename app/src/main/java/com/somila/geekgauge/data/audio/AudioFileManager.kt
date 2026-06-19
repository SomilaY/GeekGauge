package com.somila.geekgauge.data.audio

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AudioFileManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val audioDir: File by lazy {
        File(context.filesDir, "audio_recordings").also {
            if (!it.exists()) it.mkdirs()
        }
    }

    fun createNewAudioFile(sessionId: String): File {
        val fileName = "${sessionId}_${UUID.randomUUID()}.m4a"
        return File(audioDir, fileName)
    }

    fun getAudioFile(sessionId: String): File? {
        val files = audioDir.listFiles { file ->
            file.name.startsWith(sessionId) && file.name.endsWith(".m4a")
        }
        return files?.firstOrNull()
    }

    fun deleteAudioFile(sessionId: String): Boolean {
        return getAudioFile(sessionId)?.delete() ?: false
    }

    fun getAllAudioFiles(): List<File> {
        return audioDir.listFiles()?.filter {
            it.name.endsWith(".m4a")
        }?.toList() ?: emptyList()
    }

    fun getAudioDirectorySize(): Long {
        return getAllAudioFiles().sumOf { it.length() }
    }

    fun clearAllRecordings() {
        getAllAudioFiles().forEach { it.delete() }
    }
}