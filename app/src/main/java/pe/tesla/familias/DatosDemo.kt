package pe.tesla.familias

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

data class Attendance(val date: LocalDate, val status: String)
data class Notice(val id: String, val title: String, val body: String, val time: String, val read: Boolean)
data class Obligation(val amount: Long, val due: LocalDate, val paid: Boolean)

// Guarda los datos de prueba en el teléfono.
class DatosDemo(context: Context) {
    private val prefs = context.getSharedPreferences("tesla_demo", Context.MODE_PRIVATE)
    var loggedIn: Boolean
        get() = prefs.getBoolean("session", false)
        set(value) { prefs.edit().putBoolean("session", value).apply() }

    init {
        if (!prefs.contains("attendance")) {
            reset()
        }
    }

    fun attendance(): List<Attendance> {
        val rows = JSONArray(prefs.getString("attendance", "[]"))
        val records = mutableListOf<Attendance>()
        for (i in 0 until rows.length()) {
            val row = rows.getJSONObject(i)
            records.add(Attendance(LocalDate.parse(row.getString("date")), row.getString("status")))
        }
        return records.sortedByDescending { it.date }
    }

    fun todayStatus() = attendance().firstOrNull { it.date == LocalDate.now() }?.status ?: "Sin registro"

    fun saveAttendance(status: String) {
        require(status in listOf("Presente", "Tardanza", "Inasistencia"))
        val today = LocalDate.now()
        val rows = attendance().filterNot { it.date == today } + Attendance(today, status)
        prefs.edit().putString("attendance", attendanceJson(rows).toString()).apply()
    }

    fun obligation() = Obligation(
        prefs.getLong("amount", 25000),
        LocalDate.parse(prefs.getString("due", LocalDate.now().plusDays(10).toString())),
        prefs.getBoolean("paid", false)
    )

    fun saveObligation(amount: Long, due: LocalDate, paid: Boolean) {
        require(amount in 0..99999999L)
        prefs.edit().putLong("amount", amount).putString("due", due.toString()).putBoolean("paid", paid).apply()
    }

    fun notices(): List<Notice> {
        val rows = JSONArray(prefs.getString("notices", "[]"))
        val records = mutableListOf<Notice>()
        for (i in 0 until rows.length()) {
            val row = rows.getJSONObject(i)
            records.add(Notice(row.getString("id"), row.getString("title"), row.getString("body"), row.getString("time"), row.getBoolean("read")))
        }
        return records
    }

    fun addNotice(title: String, body: String): Notice {
        val notice = Notice(UUID.randomUUID().toString(), title, body, LocalDateTime.now().toString(), false)
        storeNotices(listOf(notice) + notices())
        return notice
    }

    fun markRead(id: String) { storeNotices(notices().map { if (it.id == id) it.copy(read = true) else it }) }
    private fun storeNotices(rows: List<Notice>) {
        val array = JSONArray()
        rows.forEach { array.put(JSONObject().put("id", it.id).put("title", it.title).put("body", it.body).put("time", it.time).put("read", it.read)) }
        prefs.edit().putString("notices", array.toString()).apply()
    }
    private fun attendanceJson(rows: List<Attendance>): JSONArray {
        val array = JSONArray()
        rows.forEach { array.put(JSONObject().put("date", it.date.toString()).put("status", it.status)) }
        return array
    }

    fun reset() {
        val today = LocalDate.now()
        val states = listOf("Tardanza", "Presente", "Inasistencia", "Presente", "Presente")
        prefs.edit().putString("attendance", attendanceJson(states.mapIndexed { i, s -> Attendance(today.minusDays(i.toLong()), s) }).toString())
            .putLong("amount", 25000).putString("due", today.plusDays(10).toString()).putBoolean("paid", false).apply()
        storeNotices(listOf(
            Notice("initial-attendance", "Tardanza", "Juan Pérez llegó tarde. Registro ficticio de demostración.", today.atTime(8, 5).toString(), false),
            Notice("initial-obligation", "Obligación próxima", "La cuota de demostración vence el ${today.plusDays(10)}.", today.atTime(8, 0).toString(), false)
        ))
    }
}
