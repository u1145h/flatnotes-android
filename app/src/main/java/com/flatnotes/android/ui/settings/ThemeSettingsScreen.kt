package com.flatnotes.android.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.flatnotes.android.ui.components.OneUiScaffold

data class ThemeOption(val label: String, val description: String, val key: String)

private val themeOptions = listOf(
    ThemeOption("System default", "Follow your device theme", "system"),
    ThemeOption("Light", "Always light mode", "light"),
    ThemeOption("Dark", "Always dark mode", "dark")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeSettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit
) {
    val currentTheme by viewModel.themeMode.collectAsState()
    val amoledEnabled by viewModel.amoledEnabled.collectAsState()

    OneUiScaffold(
        title = "Theme",
        onBack = onNavigateBack
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            themeOptions.forEach { option ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.setThemeMode(option.key) }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = currentTheme == option.key,
                        onClick = { viewModel.setThemeMode(option.key) }
                    )
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            text = option.label,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = option.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.setAmoledEnabled(!amoledEnabled) }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "AMOLED dark mode",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "Uses true black backgrounds when dark theme is active",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.width(12.dp))
                Switch(
                    checked = amoledEnabled,
                    onCheckedChange = viewModel::setAmoledEnabled
                )
            }
        }
    }
}
