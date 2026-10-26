package com.wesee.app.voice

import android.speech.SpeechRecognizer
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

enum class VoiceState { Idle, Speaking, Listening, Processing }
enum class FieldKind { Name, Email, Password, ConfirmPassword }
data class VoiceStep(val key: FieldKind, val question: String, val secret: Boolean = false)

private val digitWords = mapOf(
    "zero" to '0', "um" to '1', "uma" to '1', "dois" to '2', "duas" to '2', "três" to '3', "tres" to '3',
    "quatro" to '4', "cinco" to '5', "seis" to '6', "meia" to '6', "sete" to '7', "oito" to '8', "nove" to '9',
)

/** "um dois três 4 5 seis" → "123456" (o backend exige senha com exatamente 6 números). */
fun spokenToDigits(text: String): String = buildString {
    text.lowercase().split(Regex("[^\\p{L}\\p{N}]+")).forEach { tok ->
        if (tok.isNotEmpty() && tok.all { it.isDigit() }) append(tok) else digitWords[tok]?.let { append(it) }
    }
}

/**
 * Conversa guiada: IA pergunta (voz) → usuário toca no microfone e responde → reconhecimento →
 * confirmação (nome/e-mail: "sim/não"; senha: não é repetida em voz alta) → próxima pergunta.
 * Ao responder tudo, [onFinished] é chamado (envie ao backend) e a tela deve terminar com
 * [complete], [retryFrom] ou [failSubmit].
 */
class VoiceSession(
    private val assistant: VoiceAssistant,
    private val steps: List<VoiceStep>,
    private val intro: String,
    private val onFinished: (VoiceSession, Map<FieldKind, String>) -> Unit,
) {
    var state by mutableStateOf(VoiceState.Idle)
        private set
    private var index = 0
    private var started = false
    private var done = false
    private var awaitingRetry = false
    private var pending: String? = null
    private val answers = mutableMapOf<FieldKind, String>()

    fun begin() {
        if (started) return
        started = true
        say("$intro ${steps[0].question} Toque no botão grande para responder.")
    }

    fun announce(text: String) = say(text)

    fun onMicTap() {
        if (done) return
        if (awaitingRetry) { // erro de rede: reenviar as mesmas respostas
            assistant.stopSpeaking(); awaitingRetry = false; state = VoiceState.Processing
            onFinished(this, answers.toMap()); return
        }
        when (state) {
            VoiceState.Speaking -> { assistant.stopSpeaking(); listen() }
            VoiceState.Idle -> listen()
            else -> Unit
        }
    }

    /** Resposta final com sucesso (ex.: "Cadastro concluído"). */
    fun complete(message: String, then: () -> Unit = {}) {
        done = true
        assistant.beep(Beep.Success)
        say(message, then)
    }

    /** O backend recusou algo: explica e volta a perguntar a partir do campo [key]. */
    fun retryFrom(key: FieldKind, message: String) {
        done = false; awaitingRetry = false; pending = null
        index = steps.indexOfFirst { it.key == key }.coerceAtLeast(0)
        assistant.beep(Beep.Error)
        say("$message ${steps[index].question}")
    }

    /** Falha de rede/servidor: explica; o próximo toque no microfone reenvia as respostas. */
    fun failSubmit(message: String) {
        awaitingRetry = true
        assistant.beep(Beep.Error)
        say(message)
    }

    private fun say(text: String, then: () -> Unit = {}) {
        state = VoiceState.Speaking
        assistant.speak(text) { state = VoiceState.Idle; then() }
    }

    private fun listen() {
        state = VoiceState.Listening
        assistant.beep(Beep.Start)
        assistant.listen(processing = { state = VoiceState.Processing }, result = ::handle, failure = ::fail)
    }

    private fun handle(text: String) {
        state = VoiceState.Processing
        pending?.let { return confirm(it, text) }
        val step = steps[index]
        val value = normalize(step.key, text)
        if (value.isBlank()) return fail(SpeechRecognizer.ERROR_NO_MATCH)
        when {
            step.key == FieldKind.ConfirmPassword ->
                if (value == answers[FieldKind.Password]) {
                    answers[step.key] = value
                    assistant.beep(Beep.Success)
                    say("Senhas confirmadas.") { next() }
                } else {
                    assistant.beep(Beep.Error)
                    index = steps.indexOfFirst { it.key == FieldKind.Password }
                    say("As senhas não são iguais. ${steps[index].question}")
                }
            step.key == FieldKind.Password && value.length != 6 -> {
                assistant.beep(Beep.Error)
                say("A senha precisa ter exatamente 6 números. ${step.question}")
            }
            step.secret -> {
                answers[step.key] = value
                assistant.beep(Beep.Success)
                say("Senha recebida.") { next() }
            }
            else -> {
                pending = value
                assistant.beep(Beep.Success)
                say("Entendi: ${spoken(value)}. Está correto? Toque no botão e diga sim ou não.")
            }
        }
    }

    private fun confirm(value: String, text: String) {
        val words = text.lowercase().split(Regex("\\W+"))
        val no = words.any { it in setOf("não", "nao", "errado", "errada") }
        val yes = words.any { it in setOf("sim", "correto", "certo", "isso", "confirmo") }
        when {
            yes && !no -> {
                pending = null
                answers[steps[index].key] = value
                say("Ótimo.") { next() }
            }
            no -> { pending = null; say("Tudo bem. ${steps[index].question}") }
            else -> fail(SpeechRecognizer.ERROR_NO_MATCH, "Não entendi. Toque no botão e diga sim ou não.")
        }
    }

    private fun next() {
        index++
        if (index >= steps.size) finish() else say(steps[index].question)
    }

    private fun finish() {
        state = VoiceState.Processing
        assistant.beep(Beep.Success)
        onFinished(this, answers.toMap())
    }

    private fun fail(code: Int, message: String? = null) {
        assistant.beep(Beep.Error)
        say(message ?: when (code) {
            SpeechRecognizer.ERROR_NO_MATCH, SpeechRecognizer.ERROR_SPEECH_TIMEOUT ->
                "Não consegui ouvir. Toque no botão e responda novamente."
            SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT ->
                "Sem conexão para reconhecer a voz. Verifique a internet e tente de novo."
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS ->
                "Preciso da permissão do microfone. Ative nas configurações do aplicativo."
            -1 -> "O reconhecimento de voz não está disponível neste aparelho."
            else -> "Ocorreu um erro. Toque no botão e tente novamente."
        })
    }

    private fun normalize(key: FieldKind, text: String): String = when (key) {
        FieldKind.Name -> text.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
            .joinToString(" ") { it.replaceFirstChar(Char::uppercase) }
        FieldKind.Email -> text.lowercase()
            .replace(Regex("\\s*arroba\\s*"), "@").replace(Regex("\\s+ponto\\s+"), ".")
            .replace(Regex("\\s+(sublinhado|underline)\\s+"), "_").replace(Regex("\\s+traço\\s+"), "-")
            .replace(" ", "")
        FieldKind.Password, FieldKind.ConfirmPassword -> spokenToDigits(text)
    }

    private fun spoken(value: String) = value.replace("@", " arroba ").replace(".", " ponto ")
}
