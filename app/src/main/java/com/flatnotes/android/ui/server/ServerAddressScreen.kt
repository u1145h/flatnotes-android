package com.flatnotes.android.ui.server

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.res.painterResource
import com.composables.icons.lucide.R
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.flatnotes.android.ui.components.OneUiScaffold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServerAddressScreen(
    viewModel: ServerAddressViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.saved) {
        if (uiState.saved) {
            onNavigateBack()
        }
    }

    OneUiScaffold(
        title = "Server Address",
        onBack = onNavigateBack,
        actions = {
            if (uiState.connectionSuccess) {
                IconButton(onClick = viewModel::saveAndExit) {
                    Icon(painterResource(R.drawable.lucide_ic_check), contentDescription = "Save")
                }
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = uiState.serverUrl,
                onValueChange = viewModel::updateServerUrl,
                label = { Text("Server URL") },
                placeholder = { Text("http://192.168.1.100:8080") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                singleLine = true,
                leadingIcon = {
                    Icon(painterResource(R.drawable.lucide_ic_settings), contentDescription = null)
                }
            )

            OutlinedTextField(
                value = uiState.pathPrefix,
                onValueChange = viewModel::updatePathPrefix,
                label = { Text("Path Prefix (optional)") },
                placeholder = { Text("flatnotes") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Button(
                onClick = viewModel::testConnection,
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading && uiState.serverUrl.isNotBlank()
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Testing...")
                } else {
                    Text("Test Connection")
                }
            }

            if (uiState.error != null) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = uiState.error!!,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

            if (uiState.connectionSuccess) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Connected!",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        uiState.authType?.let { auth ->
                            Text(
                                text = "Auth type: $auth",
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "Tap the checkmark to save. This will clear your local data and require re-authentication.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }
    }
}
