package com.bk.workdaycounter.data

import java.time.LocalDate

data class Holiday(val date: LocalDate, val name: String)

/**
 * Built-in Indian public holidays.
 *
 * The three gazetted national holidays (Republic Day, Independence Day, Gandhi Jayanti)
 * are fixed. The rest follow lunar / religious calendars and are announced regionally,
 * so treat them as a sensible default: every one can be switched off, and you can add
 * your own dates, from the Holidays tab.
 */
object Holidays {

    val BUILT_IN: List<Holiday> = listOf(
        // ---------- 2026 ----------
        h(2026, 1, 26, "Republic Day"),
        h(2026, 3, 3, "Holika Dahan"),
        h(2026, 3, 4, "Holi"),
        h(2026, 3, 21, "Eid-ul-Fitr"),
        h(2026, 3, 26, "Ram Navami"),
        h(2026, 3, 31, "Mahavir Jayanti"),
        h(2026, 4, 3, "Good Friday"),
        h(2026, 5, 1, "Buddha Purnima"),
        h(2026, 5, 27, "Bakrid / Eid-ul-Adha"),
        h(2026, 6, 26, "Muharram"),
        h(2026, 8, 15, "Independence Day"),
        h(2026, 8, 25, "Milad-un-Nabi"),
        h(2026, 9, 4, "Janmashtami"),
        h(2026, 10, 2, "Gandhi Jayanti"),
        h(2026, 10, 20, "Dussehra"),
        h(2026, 11, 8, "Diwali"),
        h(2026, 11, 10, "Bhai Dooj"),
        h(2026, 11, 24, "Guru Nanak Jayanti"),
        h(2026, 12, 25, "Christmas"),

        // ---------- 2027 ----------
        h(2027, 1, 26, "Republic Day"),
        h(2027, 3, 22, "Holi"),
        h(2027, 3, 11, "Eid-ul-Fitr"),
        h(2027, 3, 26, "Good Friday"),
        h(2027, 4, 15, "Ram Navami"),
        h(2027, 5, 20, "Buddha Purnima"),
        h(2027, 5, 17, "Bakrid / Eid-ul-Adha"),
        h(2027, 6, 15, "Muharram"),
        h(2027, 8, 15, "Independence Day"),
        h(2027, 8, 25, "Janmashtami"),
        h(2027, 10, 2, "Gandhi Jayanti"),
        h(2027, 10, 9, "Dussehra"),
        h(2027, 10, 29, "Diwali"),
        h(2027, 11, 13, "Guru Nanak Jayanti"),
        h(2027, 12, 25, "Christmas")
    ).sortedBy { it.date }

    private fun h(y: Int, m: Int, d: Int, name: String) = Holiday(LocalDate.of(y, m, d), name)
}
