package com.bk.workdaycounter.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bk.workdaycounter.data.AppStore
import com.bk.workdaycounter.data.Holiday
import com.bk.workdaycounter.data.Holidays
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

private val FMT: DateTimeFormatter = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HolidaysScreen(store: AppStore, modifier: Modifier = Modifier) {

    val disabled = remember { mutableStateListOf<Long>().also { it.addAll(store.disabledBuiltIn) } }
    val custom = remember { mutableStateListOf<Holiday>().also { it.addAll(store.customHolidays) } }
    var showPicker by remember { mutableStateOf(false) }
    var pendingDate by remember { mutableStateOf<LocalDate?>(null) }
    var pendingName by remember { mutableStateOf("") }

    Column(modifier = modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 12.dp)) {

        Text(
            "Untick anything your office doesn't observe. Lunar-calendar dates are estimates - correct them here.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(10.dp))

        OutlinedButton(onClick = { showPicker = true }, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Filled.Add, contentDescription = null)
            Spacer(Modifier.width(6.dp))
            Text("Add your own holiday")
        }

        Spacer(Modifier.height(12.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {

            if (custom.isNotEmpty()) {
                item {
                    Text(
                        "YOUR HOLIDAYS",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                items(custom, key = { "c_" + it.date.toEpochDay() + it.name }) { hol ->
                    HolidayRow(
                        name = hol.name,
                        date = hol.date,
                        checked = true,
                        onCheckedChange = null,
                        onDelete = {
                            custom.removeAll { it.date == hol.date && it.name == hol.name }
                            store.customHolidays = custom.toList()
                        }
                    )
                }
                item { Spacer(Modifier.height(10.dp)) }
            }

            item {
                Text(
                    "INDIAN PUBLIC HOLIDAYS",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            items(Holidays.BUILT_IN, key = { it.date.toEpochDay().toString() + it.name }) { hol ->
                val epoch = hol.date.toEpochDay()
                HolidayRow(
                    name = hol.name,
                    date = hol.date,
                    checked = epoch !in disabled,
                    onCheckedChange = { on ->
                        if (on) disabled.remove(epoch) else if (epoch !in disabled) disabled.add(epoch)
                        store.disabledBuiltIn = disabled.toSet()
                    },
                    onDelete = null
                )
            }
        }
    }

    if (showPicker) {
        val state = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let {
                        pendingDate = Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate()
                    }
                    showPicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showPicker = false }) { Text("Cancel") } }
        ) { DatePicker(state = state) }
    }

    pendingDate?.let { date ->
        AlertDialog(
            onDismissRequest = { pendingDate = null; pendingName = "" },
            title = { Text("Name this holiday") },
            text = {
                Column {
                    Text(date.format(FMT), style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = pendingName,
                        onValueChange = { pendingName = it },
                        placeholder = { Text("e.g. Company foundation day") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val name = pendingName.trim().ifEmpty { "Holiday" }
                    if (custom.none { it.date == date }) custom.add(Holiday(date, name))
                    custom.sortBy { it.date }
                    store.customHolidays = custom.toList()
                    pendingDate = null; pendingName = ""
                }) { Text("Add") }
            },
            dismissButton = {
                TextButton(onClick = { pendingDate = null; pendingName = "" }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun HolidayRow(
    name: String,
    date: LocalDate,
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    onDelete: (() -> Unit)?
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(end = 4.dp)) {
            Checkbox(checked = checked, onCheckedChange = onCheckedChange, enabled = onCheckedChange != null)
            Column(Modifier.weight(1f).padding(vertical = 8.dp)) {
                Text(name, fontWeight = FontWeight.Medium)
                Text(
                    date.format(FMT),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (onDelete != null) {
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Filled.DeleteOutline,
                        contentDescription = "Remove",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
