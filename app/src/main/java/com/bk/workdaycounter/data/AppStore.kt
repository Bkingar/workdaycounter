package com.bk.workdaycounter.data

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.util.UUID

data class TodoItem(val id: String = UUID.randomUUID().toString(), val text: String, val done: Boolean = false)

/**
 * Plain SharedPreferences persistence - no extra dependencies, survives app restarts.
 * Dates entered by the user therefore stay put until they change them.
 */
class AppStore(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("workday_counter", Context.MODE_PRIVATE)

    // ---------------- dates ----------------

    var startDate: LocalDate?
        get() = prefs.getLong(KEY_START, -1L).takeIf { it >= 0 }?.let { LocalDate.ofEpochDay(it) }
        set(value) = prefs.edit().apply {
            if (value == null) remove(KEY_START) else putLong(KEY_START, value.toEpochDay())
        }.apply()

    var endDate: LocalDate?
        get() = prefs.getLong(KEY_END, -1L).takeIf { it >= 0 }?.let { LocalDate.ofEpochDay(it) }
        set(value) = prefs.edit().apply {
            if (value == null) remove(KEY_END) else putLong(KEY_END, value.toEpochDay())
        }.apply()

    // ---------------- leaves ----------------

    var adjustLeaves: Boolean
        get() = prefs.getBoolean(KEY_ADJUST, false)
        set(value) = prefs.edit().putBoolean(KEY_ADJUST, value).apply()

    var leavesText: String
        get() = prefs.getString(KEY_LEAVES, "") ?: ""
        set(value) = prefs.edit().putString(KEY_LEAVES, value).apply()

    // ---------------- holidays ----------------

    var excludeHolidays: Boolean
        get() = prefs.getBoolean(KEY_EXCLUDE_HOL, true)
        set(value) = prefs.edit().putBoolean(KEY_EXCLUDE_HOL, value).apply()

    /** Built-in holidays the user switched OFF, stored as epoch days. */
    var disabledBuiltIn: Set<Long>
        get() = prefs.getStringSet(KEY_DISABLED, emptySet())!!.mapNotNull { it.toLongOrNull() }.toSet()
        set(value) = prefs.edit().putStringSet(KEY_DISABLED, value.map { it.toString() }.toSet()).apply()

    /** Extra holidays the user added themselves. */
    var customHolidays: List<Holiday>
        get() {
            val raw = prefs.getString(KEY_CUSTOM, "[]") ?: "[]"
            return runCatching {
                val arr = JSONArray(raw)
                (0 until arr.length()).map { i ->
                    val o = arr.getJSONObject(i)
                    Holiday(LocalDate.ofEpochDay(o.getLong("d")), o.optString("n", "Holiday"))
                }
            }.getOrDefault(emptyList())
        }
        set(value) {
            val arr = JSONArray()
            value.forEach { arr.put(JSONObject().put("d", it.date.toEpochDay()).put("n", it.name)) }
            prefs.edit().putString(KEY_CUSTOM, arr.toString()).apply()
        }

    /** Every holiday date currently in force. */
    fun activeHolidayDates(): Set<LocalDate> {
        if (!excludeHolidays) return emptySet()
        val off = disabledBuiltIn
        val builtIn = Holidays.BUILT_IN.filter { it.date.toEpochDay() !in off }.map { it.date }
        return (builtIn + customHolidays.map { it.date }).toSet()
    }

    // ---------------- todos ----------------

    var todos: List<TodoItem>
        get() {
            val raw = prefs.getString(KEY_TODOS, "[]") ?: "[]"
            return runCatching {
                val arr = JSONArray(raw)
                (0 until arr.length()).map { i ->
                    val o = arr.getJSONObject(i)
                    TodoItem(o.getString("id"), o.getString("t"), o.optBoolean("c", false))
                }
            }.getOrDefault(emptyList())
        }
        set(value) {
            val arr = JSONArray()
            value.forEach { arr.put(JSONObject().put("id", it.id).put("t", it.text).put("c", it.done)) }
            prefs.edit().putString(KEY_TODOS, arr.toString()).apply()
        }

    private companion object {
        const val KEY_START = "start_date"
        const val KEY_END = "end_date"
        const val KEY_ADJUST = "adjust_leaves"
        const val KEY_LEAVES = "leaves"
        const val KEY_EXCLUDE_HOL = "exclude_holidays"
        const val KEY_DISABLED = "disabled_builtin"
        const val KEY_CUSTOM = "custom_holidays"
        const val KEY_TODOS = "todos"
    }
}
