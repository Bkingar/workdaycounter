package com.bk.workdaycounter.data

import java.time.DayOfWeek
import java.time.LocalDate

object WorkdayCalc {

    /** Working days between [start] and [end] inclusive, skipping Sat/Sun and [holidays]. */
    fun workingDays(start: LocalDate, end: LocalDate, holidays: Set<LocalDate>): Int {
        if (end.isBefore(start)) return 0
        var count = 0
        var day = start
        while (!day.isAfter(end)) {
            if (isWorkingDay(day, holidays)) count++
            day = day.plusDays(1)
        }
        return count
    }

    fun isWorkingDay(day: LocalDate, holidays: Set<LocalDate>): Boolean {
        if (day.dayOfWeek == DayOfWeek.SATURDAY || day.dayOfWeek == DayOfWeek.SUNDAY) return false
        return day !in holidays
    }

    fun weekendCount(start: LocalDate, end: LocalDate): Int {
        if (end.isBefore(start)) return 0
        var count = 0
        var day = start
        while (!day.isAfter(end)) {
            if (day.dayOfWeek == DayOfWeek.SATURDAY || day.dayOfWeek == DayOfWeek.SUNDAY) count++
            day = day.plusDays(1)
        }
        return count
    }

    /** Holidays inside the range that actually fall on a weekday (i.e. that really cost a day). */
    fun effectiveHolidays(start: LocalDate, end: LocalDate, holidays: Set<LocalDate>): Int =
        holidays.count {
            !it.isBefore(start) && !it.isAfter(end) &&
                it.dayOfWeek != DayOfWeek.SATURDAY && it.dayOfWeek != DayOfWeek.SUNDAY
        }

    fun totalDays(start: LocalDate, end: LocalDate): Int {
        if (end.isBefore(start)) return 0
        return (end.toEpochDay() - start.toEpochDay()).toInt() + 1
    }
}
