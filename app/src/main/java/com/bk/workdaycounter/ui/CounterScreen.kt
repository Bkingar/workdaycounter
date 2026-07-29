package com.bk.workdaycounter.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bk.workdaycounter.data.AppStore
import com.bk.workdaycounter.data.WorkdayCalc
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

private val DATE_FMT: DateTimeFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CounterScreen(store: AppStore, celebrateTrigger: Int, modifier: Modifier = Modifier) {

    var start by remember { mutableStateOf(store.startDate) }
    var end by remember { mutableStateOf(store.endDate) }
    var adjust by remember { mutableStateOf(store.adjustLeaves) }
    var leavesText by remember { mutableStateOf(store.leavesText) }
    var excludeHol by remember { mutableStateOf(store.excludeHolidays) }
    var picking by remember { mutableStateOf<String?>(null) }

    val today = remember(celebrateTrigger) { LocalDate.now() }
    val holidays = remember(excludeHol, celebrateTrigger) { store.activeHolidayDates() }

    // "Remaining" -> never count days that have already gone by.
    val countFrom = start?.let { if (it.isBefore(today)) today else it }
    val finish = end

    // Day-by-day loops, so keep them out of recomposition (typing in the leaves field
    // must not re-walk a multi-year range on every keystroke).
    val workingDays = remember(countFrom, finish, holidays) {
        if (countFrom != null && finish != null) WorkdayCalc.workingDays(countFrom, finish, holidays) else 0
    }
    val holidaysHit = remember(countFrom, finish, holidays) {
        if (countFrom != null && finish != null) WorkdayCalc.effectiveHolidays(countFrom, finish, holidays) else 0
    }
    val weekends = remember(countFrom, finish) {
        if (countFrom != null && finish != null) WorkdayCalc.weekendCount(countFrom, finish) else 0
    }
    val totalDays = remember(countFrom, finish) {
        if (countFrom != null && finish != null) WorkdayCalc.totalDays(countFrom, finish) else 0
    }

    val leaves = leavesText.toIntOrNull() ?: 0
    val finalCounter = (if (adjust) workingDays - leaves else workingDays).coerceAtLeast(0)

    Box(modifier = modifier.fillMaxSize()) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {

            // ---------- date selectors ----------
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DateCard(
                    label = "START DATE",
                    value = start?.format(DATE_FMT),
                    modifier = Modifier.weight(1f)
                ) { picking = "start" }
                DateCard(
                    label = "END DATE",
                    value = end?.format(DATE_FMT),
                    modifier = Modifier.weight(1f)
                ) { picking = "end" }
            }

            if (start != null && end != null && end!!.isBefore(start!!)) {
                Spacer(Modifier.height(8.dp))
                Text(
                    "End date is before start date.",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            // ---------- the big number ----------
            Spacer(Modifier.height(28.dp))
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (start == null || end == null) {
                    Text(
                        "--",
                        fontSize = 110.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Pick a start and end date",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    RollingNumber(
                        target = finalCounter,
                        trigger = celebrateTrigger,
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontSize = 116.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = if (adjust) "WORKING DAYS LEFT (AFTER LEAVES)" else "WORKING DAYS LEFT",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.5.sp,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "counting from ${countFrom!!.format(DATE_FMT)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // ---------- breakdown ----------
            if (start != null && end != null) {
                Spacer(Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatChip("Total days", totalDays.toString(), Modifier.weight(1f))
                    StatChip("Weekends", weekends.toString(), Modifier.weight(1f))
                    StatChip("Holidays", holidaysHit.toString(), Modifier.weight(1f))
                }
                if (adjust) {
                    Spacer(Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StatChip("Working days", workingDays.toString(), Modifier.weight(1f))
                        StatChip("Leaves", leaves.toString(), Modifier.weight(1f))
                    }
                }
            }

            // ---------- controls ----------
            Spacer(Modifier.height(28.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(Modifier.padding(16.dp)) {

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("Adjust leaves", fontWeight = FontWeight.SemiBold)
                            Text(
                                "Subtract planned leaves from the count",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = adjust,
                            onCheckedChange = { adjust = it; store.adjustLeaves = it }
                        )
                    }

                    if (adjust) {
                        Spacer(Modifier.height(12.dp))
                        OutlinedTextField(
                            value = leavesText,
                            onValueChange = { input ->
                                val clean = input.filter { it.isDigit() }.take(3)
                                leavesText = clean
                                store.leavesText = clean
                            },
                            label = { Text("Number of leaves") },
                            placeholder = { Text("e.g. 5") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("Exclude Indian holidays", fontWeight = FontWeight.SemiBold)
                            Text(
                                "Manage the list in the Holidays tab",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = excludeHol,
                            onCheckedChange = { excludeHol = it; store.excludeHolidays = it }
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
        }

        ConfettiOverlay(trigger = celebrateTrigger, modifier = Modifier.fillMaxSize())
    }

    // ---------- date picker dialog ----------
    if (picking != null) {
        val which = picking!!
        val initial = (if (which == "start") start else end) ?: LocalDate.now()
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = initial.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { picking = null },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { millis ->
                        val picked = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
                        if (which == "start") {
                            start = picked; store.startDate = picked
                        } else {
                            end = picked; store.endDate = picked
                        }
                    }
                    picking = null
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { picking = null }) { Text("Cancel") } }
        ) {
            DatePicker(state = pickerState)
        }
    }
}

@Composable
private fun DateCard(label: String, value: String?, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(
        // clip first so the ripple follows the rounded corners
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.CalendarMonth,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(
                text = value ?: "Select",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun StatChip(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                RoundedCornerShape(14.dp)
            )
            .padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
