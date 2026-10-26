package com.wesee.app.voice

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.util.Locale
import java.util.UUID

/**
 * Configuração da voz do assistente. Altere aqui para escolher uma voz mais adequada:
 * [voiceName] aceita o nome de qualquer voz instalada (veja `TextToSpeech.voices`);
 * se for null, usa a primeira voz offline do idioma.
 */
data class VoiceConfig(
    val locale: Locale = Locale("pt", "BR"),
    val voiceName: String? = null,
    val speechRate: Float = 0.95f,
    val pitch: Float = 1.0f,
)

enum class Beep { Start, Success, Error }

/** Fala (TextToSpeech), escuta (SpeechRecognizer) e feedback sonoro. Use sempre na thread principal. */
class VoiceAssistant(context: Context, private val config: VoiceConfig = VoiceConfig()) {
    private val ctx = context.applicationContext
    private val main = Handler(Looper.getMainLooper())
    private val queued = ArrayDeque<() -> Unit>()
    private var ready = false
    private var currentUtterance: String? = null
    private var recognizer: SpeechRecognizer? = null
    private val tone: ToneGenerator? = try { ToneGenerator(AudioManager.STREAM_MUSIC, 80) } catch (e: RuntimeException) { null }

    private val tts: TextToSpeech = TextToSpeech(ctx) { status ->
        if (status == TextToSpeech.SUCCESS) {
            applyConfig()
            ready = true
            while (queued.isNotEmpty()) queued.removeFirst().invoke()
        }
    }

    private fun applyConfig() {
        tts.language = config.locale
        tts.setSpeechRate(config.speechRate)
        tts.setPitch(config.pitch)
        val voices = tts.voices.orEmpty()
        val chosen = voices.firstOrNull { it.name == config.voiceName }
            ?: voices.firstOrNull { it.locale.language == config.locale.language && !it.isNetworkConnectionRequired }
        chosen?.let { tts.voice = it }
    }

    fun speak(text: String, whenDone: () -> Unit = {}) {
        val job = {
            val id = UUID.randomUUID().toString()
            currentUtterance = id
            tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {}
                override fun onDone(utteranceId: String?) { finish(utteranceId) }
                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) { finish(utteranceId) }
                private fun finish(u: String?) = main.post { if (u == currentUtterance) { currentUtterance = null; whenDone() } }
            })
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, id)
            Unit
        }
        if (ready) job() else queued.addLast(job)
    }

    fun stopSpeaking() { currentUtterance = null; tts.stop() }

    fun beep(kind: Beep) {
        when (kind) {
            Beep.Start -> tone?.startTone(ToneGenerator.TONE_PROP_BEEP, 150)
            Beep.Success -> tone?.startTone(ToneGenerator.TONE_PROP_ACK, 200)
            Beep.Error -> tone?.startTone(ToneGenerator.TONE_PROP_NACK, 300)
        }
    }

    /** Escuta uma resposta. [failure] recebe um código de SpeechRecognizer.ERROR_* (ou -1 se indisponível). */
    fun listen(processing: () -> Unit, result: (String) -> Unit, failure: (Int) -> Unit) {
        recognizer?.destroy(); recognizer = null
        if (!SpeechRecognizer.isRecognitionAvailable(ctx)) { failure(-1); return }
        val r = SpeechRecognizer.createSpeechRecognizer(ctx)
        recognizer = r
        r.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
            override fun onEndOfSpeech() = processing()
            override fun onError(error: Int) = failure(error)
            override fun onResults(results: Bundle?) {
                val text = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()
                if (text.isNullOrBlank()) failure(SpeechRecognizer.ERROR_NO_MATCH) else result(text)
            }
        })
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            .putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            .putExtra(RecognizerIntent.EXTRA_LANGUAGE, config.locale.toLanguageTag())
            .putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
            .putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        main.postDelayed({ if (recognizer === r) r.startListening(intent) }, 350) // deixa o bipe terminar
    }

    fun release() {
        currentUtterance = null
        recognizer?.destroy(); recognizer = null
        tts.stop(); tts.shutdown()
        tone?.release()
    }
}
