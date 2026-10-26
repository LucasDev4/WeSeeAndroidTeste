package com.wesee.app.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/** [highlights]: trechos destacados como (início, fim exclusivo) em [text]. */
data class Note(val id: Long, val text: String, val color: Long, val highlights: List<Pair<Int, Int>> = emptyList())

val NoteColors: List<Long> = listOf(0xFF8CBBFF, 0xFFFFE680, 0xFFA8E6B0, 0xFFFFB3C7, 0xFFCDB8FF)

private val seedNotes = listOf(
    Note(1, "Lembrar de levar o carregador.", NoteColors[0], listOf(19 to 29)),
    Note(2, "Consulta médica na quinta às 15h.", NoteColors[1], listOf(19 to 32)),
    Note(3, "Ponto de ônibus fica na esquina.", NoteColors[2]),
)

/** Persistência simples das anotações (SharedPreferences + JSON). */
class NotesStore(context: Context) {
    private val prefs = context.getSharedPreferences("wesee_notes", Context.MODE_PRIVATE)

    fun load(): List<Note> {
        val raw = prefs.getString(KEY, null) ?: return seedNotes
        return try {
            val arr = JSONArray(raw)
            (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                val h = o.getJSONArray("h")
                Note(o.getLong("id"), o.getString("text"), o.getLong("color"),
                    (0 until h.length()).map { j -> h.getJSONArray(j).let { it.getInt(0) to it.getInt(1) } })
            }
        } catch (e: Exception) { seedNotes }
    }

    fun save(notes: List<Note>) {
        val arr = JSONArray()
        notes.forEach { n ->
            val h = JSONArray()
            n.highlights.forEach { h.put(JSONArray().put(it.first).put(it.second)) }
            arr.put(JSONObject().put("id", n.id).put("text", n.text).put("color", n.color).put("h", h))
        }
        prefs.edit().putString(KEY, arr.toString()).apply()
    }

    private companion object { const val KEY = "notes" }
}
