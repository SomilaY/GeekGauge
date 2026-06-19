package com.somila.geekgauge.data.audio

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import com.somila.geekgauge.domain.TranscriptionService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SpeechRecognizerImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : TranscriptionService {

    private var speechRecognizer: SpeechRecognizer? = null
    private val transcriptBuffer = StringBuilder()

    private val _liveTranscript = MutableStateFlow("")
    val liveTranscript: StateFlow<String> = _liveTranscript.asStateFlow()

    private val _transcriptReady = MutableStateFlow(false)
    private val _rmsLevel = MutableStateFlow(0f)
    val rmsLevel: StateFlow<Float> = _rmsLevel.asStateFlow()

    // Flag to control our continuous listening loop
    private var isLoopActive: Boolean = false
    private val TAG = "SpeechRecognizerImpl"

    override fun isAvailable(): Boolean = SpeechRecognizer.isRecognitionAvailable(context)

    fun startListening() {
        Log.d(TAG, "startListening() called")
        isLoopActive = true
        transcriptBuffer.clear()
        _liveTranscript.value = ""
        _transcriptReady.value = false
        _rmsLevel.value = 0f

        createRecognizer()
        beginListening()
    }

    fun stopListening() {
        Log.d(TAG, "stopListening() called. Winding down...")
        isLoopActive = false // Break the loop first!
        speechRecognizer?.stopListening() // Tells it to finish processing current speech
    }

    fun getCurrentTranscript(): String = transcriptBuffer.toString().trim()

    private fun createRecognizer() {
        speechRecognizer?.destroy()
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
            setRecognitionListener(buildListener())
        }
    }

    private fun releaseRecognizer() {
        speechRecognizer?.destroy()
        speechRecognizer = null
        _rmsLevel.value = 0f
    }

    private fun buildListener() = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) { Log.d(TAG, "onReadyForSpeech") }
        override fun onBeginningOfSpeech() { Log.d(TAG, "onBeginningOfSpeech") }
        override fun onEndOfSpeech() { Log.d(TAG, "onEndOfSpeech") }

        override fun onRmsChanged(rmsdB: Float) {
            // Only update RMS if we are actively listening
            if (!isLoopActive) return
            val normalised = ((rmsdB + 2f) / 12f).coerceIn(0f, 1f)
            _rmsLevel.value = normalised
        }

        override fun onPartialResults(partialResults: Bundle?) {
            val partial = partialResults
                ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                ?.firstOrNull() ?: return
            _liveTranscript.value = transcriptBuffer.toString() + partial
        }

        override fun onResults(results: Bundle?) {
            val result = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()
            Log.d(TAG, "onResults: $result")

            if (!result.isNullOrBlank()) {
                if (transcriptBuffer.isNotEmpty()) transcriptBuffer.append(" ")
                transcriptBuffer.append(result)
                _liveTranscript.value = transcriptBuffer.toString()
            }

            if (isLoopActive) {
                beginListening()
            } else {
                _transcriptReady.value = true
                releaseRecognizer()
            }
        }

        override fun onError(error: Int) {
            Log.e(TAG, "onError: $error")

            if (isLoopActive) {
                when (error) {
                    SpeechRecognizer.ERROR_NO_MATCH,
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> beginListening()
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> {
                        createRecognizer()
                        beginListening()
                    }
                    else -> {
                        // Crucial for recovery
                        createRecognizer()
                        beginListening()
                    }
                }
            } else {
                // If we are stopping, any final error (like NO_MATCH) should unblock the UseCase
                _transcriptReady.value = true
                releaseRecognizer()
            }
        }

        override fun onBufferReceived(buffer: ByteArray?) {}
        override fun onEvent(eventType: Int, params: Bundle?) {}
    }

    private fun beginListening() {
        if (!isLoopActive) return
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }
        speechRecognizer?.startListening(intent)
    }

    override suspend fun transcribeAudio(): Result<String> {
        val transcript = getCurrentTranscript()
        return if (transcript.isNotBlank()) Result.success(transcript)
        else Result.failure(TranscriptionException.EmptyResult("No speech captured"))
    }

    suspend fun awaitFinalTranscript(): String {
        withTimeoutOrNull(4000L) {
            _transcriptReady.first { it }
        }
        return getCurrentTranscript()
    }
}

sealed class TranscriptionException(message: String) : Exception(message) {
    class NotAvailable(message: String) : TranscriptionException(message)
    class EmptyResult(message: String) : TranscriptionException(message)
    class RecognitionFailed(message: String) : TranscriptionException(message)
}