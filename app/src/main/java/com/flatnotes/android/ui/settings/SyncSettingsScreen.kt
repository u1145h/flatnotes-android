package com.flatnotes.android.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.flatnotes.android.ui.components.OneUiScaffold

data class IntervalOption(val label: String, val minutes: Long)

private val intervalOptions = listOf(
    IntervalOption("5 minutes", 5),
    IntervalOption("15 minutes", 15),
    IntervalOption("30 minutes", 30),
    IntervalOption("1 hour", 60),
    IntervalOption("4 hours", 240)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SyncSettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit
) {
    val currentInterval by viewModel.syncInterval.collectAsState()

    OneUiScaffold(
        title = "Sync Interval",
        onBack = onNavigateBack
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            intervalOptions.forEach { option ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.setSyncInterval(option.minutes) }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = currentInterval == option.minutes,
                        onClick = { viewModel.setSyncInterval(option.minutes) }
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = option.label,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text = if (currentInterval <= 5) "Notes will sync every $currentInterval minutes."
                       else "Notes will sync every $currentInterval minutes in the background.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
