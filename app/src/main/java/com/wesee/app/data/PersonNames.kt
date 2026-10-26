package com.wesee.app.data

private fun String?.cleanName(): String = this.orEmpty().trim().replace(Regex("\\s+"), " ")

/** Primeiro nome para saudações ("Olá Gabriel."). Vazio/ausente → "usuário". */
fun firstNameOf(nome: String?): String = nome.cleanName().substringBefore(' ').ifBlank { "usuário" }

/**
 * Nome da pessoa acompanhada, derivado do nome do responsável: "Lucas" → "Lucas Jr.".
 * Remove espaços extras, não duplica o "Jr." e não quebra com nome vazio/ausente.
 */
fun acompanhadoName(responsavelNome: String?): String {
    val nome = responsavelNome.cleanName()
    if (nome.isEmpty()) return "Pessoa acompanhada"
    return if (Regex("(?i)\\bjr\\.?$").containsMatchIn(nome)) nome else "$nome Jr."
}
