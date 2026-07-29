package com.bk.workdaycounter.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.bk.workdaycounter.data.AppStore
import com.bk.workdaycounter.data.TodoItem

@Composable
fun TodoScreen(store: AppStore, modifier: Modifier = Modifier) {

    val todos = remember { mutableStateListOf<TodoItem>().also { it.addAll(store.todos) } }
    var input by remember { mutableStateOf("") }

    fun persist() {
        store.todos = todos.toList()
    }

    fun add() {
        val text = input.trim()
        if (text.isEmpty()) return
        todos.add(0, TodoItem(text = text))
        input = ""
        persist()
    }

    Column(modifier = modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 12.dp)) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                placeholder = { Text("Add a task...") },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.weight(1f)
            )
            FilledIconButton(onClick = { add() }) {
                Icon(Icons.Filled.Add, contentDescription = "Add task")
            }
        }

        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val done = todos.count { it.done }
            Text(
                text = if (todos.isEmpty()) "No tasks yet" else "$done of ${todos.size} done",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
            if (todos.any { it.done }) {
                TextButton(onClick = { todos.removeAll { it.done }; persist() }) {
                    Text("Clear completed")
                }
            }
        }

        Spacer(Modifier.height(4.dp))

        if (todos.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    "Your to-do list is empty.\nAdd something above to get started.",
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(todos, key = { it.id }) { item ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(start = 4.dp, end = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = item.done,
                                onCheckedChange = { checked ->
                                    val i = todos.indexOfFirst { it.id == item.id }
                                    if (i >= 0) {
                                        todos[i] = todos[i].copy(done = checked)
                                        persist()
                                    }
                                }
                            )
                            Text(
                                text = item.text,
                                modifier = Modifier.weight(1f).padding(vertical = 14.dp),
                                fontWeight = FontWeight.Medium,
                                textDecoration = if (item.done) TextDecoration.LineThrough else null,
                                color = if (item.done) MaterialTheme.colorScheme.onSurfaceVariant
                                else MaterialTheme.colorScheme.onSurface
                            )
                            IconButton(onClick = {
                                todos.removeAll { it.id == item.id }
                                persist()
                            }) {
                                Icon(
                                    Icons.Filled.DeleteOutline,
                                    contentDescription = "Delete task",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
