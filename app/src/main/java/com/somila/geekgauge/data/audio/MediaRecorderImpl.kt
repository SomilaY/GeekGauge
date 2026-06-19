package com.somila.geekgauge.data.audio

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import androidx.annotation.RequiresApi
import com.somila.geekgauge.domain.models.RecordingState
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaRecorderImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : AudioRecorder {

    private var mediaRecorder: MediaRecorder? = null
    private var outputFile: File? = null
    private var startTimeMs: Long = 0L
    private var pausedDurationMs: Long = 0L
    private var pauseStartTimeMs: Long = 0L

    private var hasStarted: Boolean = false

    @Volatile override var isRecording: Boolean = false
        private set
    @Volatile override var isPaused: Boolean = false
        private set

    private var finalDurationMs: Long = 0L

    override suspend fun startRecording(outputFile: File): Result<Unit> {
        return try {
            this.outputFile = outputFile
            outputFile.parentFile?.mkdirs()

            mediaRecorder = createMediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioSamplingRate(44100)
                setAudioEncodingBitRate(128000)
                setAudioChannels(2)
                setOutputFile(outputFile.absolutePath)
                setMaxFileSize(500 * 1024 * 1024)

                setOnErrorListener { _, what, extra ->
                    isRecording = false
                    isPaused = false
                    hasStarted = false
                }

                prepare()
                start()
                hasStarted = true  // only set after start() succeeds
            }

            isRecording = true
            isPaused = false
            startTimeMs = System.currentTimeMillis()
            pausedDurationMs = 0L

            Result.success(Unit)
        } catch (e: IOException) {
            hasStarted = false
            Result.failure(AudioRecorderException.InitializationFailed(e.message ?: "Failed to start"))
        } catch (e: SecurityException) {
            hasStarted = false
            Result.failure(AudioRecorderException.PermissionDenied("Microphone permission not granted"))
        } catch (e: Exception) {
            hasStarted = false
            Result.failure(AudioRecorderException.Unknown(e.message ?: "Unknown error"))
        }
    }
    private fun createMediaRecorder(): MediaRecorder {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }
    }

    override suspend fun pauseRecording(): Result<Unit> {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            return Result.failure(AudioRecorderException.PauseFailed("Pause not supported below API 24"))
        }
        if (!hasStarted) {
            return Result.failure(AudioRecorderException.NotRecording("Recording hasn't started yet"))
        }
        return try {
            mediaRecorder?.let {
                it.pause()
                isPaused = true
                pauseStartTimeMs = System.currentTimeMillis()
                Result.success(Unit)
            } ?: Result.failure(AudioRecorderException.NotRecording("No active recording"))
        } catch (e: Exception) {
            Result.failure(AudioRecorderException.PauseFailed(e.message ?: "Failed to pause"))
        }
    }

    override suspend fun resumeRecording(): Result<Unit> {
        return try {
            mediaRecorder?.let {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                    return Result.failure(AudioRecorderException.ResumeFailed("Resume not supported below API 24"))
                }
                isPaused = false
                pausedDurationMs += System.currentTimeMillis() - pauseStartTimeMs
                Result.success(Unit)
            } ?: Result.failure(AudioRecorderException.NotRecording("No active recording to resume"))
        } catch (e: Exception) {
            Result.failure(AudioRecorderException.ResumeFailed(e.message ?: "Failed to resume recording"))
        }
    }

    override suspend fun stopRecording(): Result<File> {
        return try {
            var stopException: Exception? = null
            mediaRecorder?.apply {
                if (hasStarted) {  // only call stop() if recording actually started
                    try { stop() } catch (e: RuntimeException) { stopException = e }
                }
                release()
            }

            finalDurationMs = getRecordingDurationMs()
            mediaRecorder = null
            isRecording = false
            isPaused = false
            hasStarted = false

            val file = outputFile
            if (file == null) return Result.failure(AudioRecorderException.NoFile("No output file"))
            if (!file.exists() || file.length() == 0L) {
                return Result.failure(
                    AudioRecorderException.EmptyFile(stopException?.message ?: "Empty recording")
                )
            }
            Result.success(file)
        } catch (e: Exception) {
            Result.failure(AudioRecorderException.StopFailed(e.message ?: "Failed to stop"))
        }
    }

    override fun getCurrentAmplitude(): Int {
        return try {
            if (isRecording && !isPaused && hasStarted) {
                val amp = mediaRecorder?.maxAmplitude ?: 0
                amp
            } else 0
        } catch (e: Exception) {
            0
        }
    }

    override fun getRecordingDurationMs(): Long {
        return when {
            isRecording && !isPaused -> System.currentTimeMillis() - startTimeMs - pausedDurationMs
            isPaused -> pauseStartTimeMs - startTimeMs - pausedDurationMs
            else -> finalDurationMs
        }
    }

    override fun cleanup() {
        try {
            mediaRecorder?.apply {
                if (hasStarted) {
                    try { stop() } catch (e: Exception) { }
                }
                release()
            }
        } catch (e: Exception) {
        } finally {
            mediaRecorder = null
            isRecording = false
            isPaused = false
            hasStarted = false
            finalDurationMs = 0L
        }
    }
}

// Domain exception hierarchy
sealed class AudioRecorderException(message: String) : Exception(message) {
    class InitializationFailed(message: String) : AudioRecorderException(message)
    class PermissionDenied(message: String) : AudioRecorderException(message)
    class NotRecording(message: String) : AudioRecorderException(message)
    class PauseFailed(message: String) : AudioRecorderException(message)
    class ResumeFailed(message: String) : AudioRecorderException(message)
    class StopFailed(message: String) : AudioRecorderException(message)
    class EmptyFile(message: String) : AudioRecorderException(message)
    class NoFile(message: String) : AudioRecorderException(message)
    class Unknown(message: String) : AudioRecorderException(message)
}