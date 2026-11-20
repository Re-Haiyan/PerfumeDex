package ph.perfumedex

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

class Store(context: Context) {
    private val prefs = context.getSharedPreferences("dex", Context.MODE_PRIVATE)
    fun load(): Triple<List<Scent>, List<Wear>, Set<String>> {
        val root = JSONObject(prefs.getString("data", "{}")!!)
        val custom = root.optJSONArray("scents") ?: JSONArray()
        val scents = (0 until custom.length()).map { custom.getJSONObject(it).let { s -> Scent(s.getString("id"), s.getString("name"), s.getString("house"), s.getString("notes"), "Your own discovery.", 0xFFDED1EA) } }
        val logs = root.optJSONArray("wears") ?: JSONArray()
        val wears = (0 until logs.length()).map { logs.getJSONObject(it).let { w -> Wear(w.getString("id"), w.getString("scent"), w.getString("occasion"), w.getString("note"), w.getString("format"), w.getString("date"), w.getBoolean("shared")) } }
        val saved = root.optJSONArray("saved") ?: JSONArray()
        return Triple(catalog + scents, wears, (0 until saved.length()).map { saved.getString(it) }.toSet())
    }
    fun votes(): Set<String> = prefs.getStringSet("demoVotes", emptySet())!!.toSet()
    fun save(scents: List<Scent>, wears: List<Wear>, saved: Set<String>, votes: Set<String>) {
        val custom = JSONArray().apply { scents.filter { s -> catalog.none { it.id == s.id } }.forEach { put(JSONObject().put("id", it.id).put("name", it.name).put("house", it.house).put("notes", it.notes)) } }
        val logs = JSONArray().apply { wears.forEach { put(JSONObject().put("id", it.id).put("scent", it.scent).put("occasion", it.occasion).put("note", it.note).put("format", it.format).put("date", it.date).put("shared", it.shared)) } }
        prefs.edit().putString("data", JSONObject().put("scents", custom).put("wears", logs).put("saved", JSONArray(saved.toList())).toString()).putStringSet("demoVotes", votes).apply()
    }
}
