package com.bk.workdaycounter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.bk.workdaycounter.data.AppStore
import com.bk.workdaycounter.ui.CounterScreen
import com.bk.workdaycounter.ui.HolidaysScreen
import com.bk.workdaycounter.ui.TodoScreen
import com.bk.workdaycounter.ui.theme.WorkdayCounterTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WorkdayCounterTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    AppRoot()
                }
            }
        }
    }
}

private enum class Tab(val label: String, val icon: ImageVector) {
    COUNTER("Counter", Icons.Filled.CalendarMonth),
    TODO("To-Do", Icons.Filled.Checklist),
    HOLIDAYS("Holidays", Icons.Filled.EventBusy)
}

@Composable
private fun AppRoot() {
    val context = LocalContext.current
    val store = remember { AppStore(context) }
    var tab by remember { mutableStateOf(Tab.COUNTER) }

    // Bumped every time the app comes to the foreground -> re-runs the roll + confetti.
    var celebrateTrigger by remember { mutableIntStateOf(0) }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) celebrateTrigger++
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                Tab.entries.forEach { t ->
                    NavigationBarItem(
                        selected = tab == t,
                        onClick = { tab = t },
                        icon = { Icon(t.icon, contentDescription = t.label) },
                        label = { Text(t.label) }
                    )
                }
            }
        }
    ) { inner ->
        when (tab) {
            Tab.COUNTER -> CounterScreen(
                store = store,
                celebrateTrigger = celebrateTrigger,
                modifier = Modifier.padding(inner)
            )
            Tab.TODO -> TodoScreen(store = store, modifier = Modifier.padding(inner))
            Tab.HOLIDAYS -> HolidaysScreen(store = store, modifier = Modifier.padding(inner))
        }
    }
}
