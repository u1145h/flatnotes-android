package com.flatnotes.android.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.R
import com.flatnotes.android.ui.components.OneUiScaffold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToSync: () -> Unit,
    onNavigateToTheme: () -> Unit,
    onNavigateToServerAddress: () -> Unit,
    onLogout: () -> Unit
) {
    val serverUrl by viewModel.serverUrl.collectAsState()

    OneUiScaffold(
        title = "Settings",
        onBack = onNavigateBack
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            SettingsItemRow(
                icon = R.drawable.lucide_ic_refresh_cw,
                title = "Sync Interval",
                subtitle = "How often notes sync to the server",
                onClick = onNavigateToSync
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            SettingsItemRow(
                icon = R.drawable.lucide_ic_moon,
                title = "Theme",
                subtitle = "Customize app appearance",
                onClick = onNavigateToTheme
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            SettingsItemRow(
                icon = R.drawable.lucide_ic_cloud,
                title = "Server Address",
                subtitle = if (serverUrl.isNotBlank()) serverUrl else "Not configured",
                onClick = onNavigateToServerAddress
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onLogout)
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(R.drawable.lucide_ic_log_out),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(16.dp))
                Text(
                    text = "Logout",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun SettingsItemRow(
    icon: Int,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            painter = painterResource(R.drawable.lucide_ic_chevron_right),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
        )
    }
}
