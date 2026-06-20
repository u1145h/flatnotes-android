package com.flatnotes.android.tile

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.isSystemInDarkTheme
import com.flatnotes.android.data.repository.SettingsRepository
import com.flatnotes.android.ui.theme.FlatnotesTheme

class QuickNoteCreationActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val settingsRepo = remember { SettingsRepository(this@QuickNoteCreationActivity) }
            val themeMode by settingsRepo.themeMode.collectAsState(initial = "system")
            val amoledEnabled by settingsRepo.amoledEnabled.collectAsState(initial = false)

            val darkTheme = when (themeMode) {
                "light" -> false
                "dark" -> true
                else -> isSystemInDarkTheme()
            }
            val isAmoled = amoledEnabled && darkTheme

            val viewModel: QuickNoteCreationViewModel = viewModel()
            val uiState by viewModel.uiState.collectAsState()

            LaunchedEffect(uiState.saved) {
                if (uiState.saved) {
                    val message = if (uiState.savedOffline) {
                        getString(com.flatnotes.android.R.string.quick_note_saved_offline)
                    } else {
                        getString(com.flatnotes.android.R.string.quick_note_created)
                    }
                    Toast.makeText(this@QuickNoteCreationActivity, message, Toast.LENGTH_SHORT).show()
                    finish()
                }
            }

            FlatnotesTheme(darkTheme = darkTheme, isAmoled = isAmoled) {
                Surface(
                    modifier = Modifier
                        .padding(15.dp)
                        .widthIn(max = 480.dp),
                color = Color.Transparent
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.5f),
                    shape = RoundedCornerShape(14.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 13.dp, end = 13.dp, top = 10.dp, bottom = 10.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        BasicTextField(
                            value = uiState.title,
                            onValueChange = viewModel::updateTitle,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            textStyle = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            singleLine = true,
                            enabled = !uiState.isSaving,
                            decorationBox = { innerTextField ->
                                Box {
                                    if (uiState.title.isEmpty()) {
                                        Text(
                                            text = getString(com.flatnotes.android.R.string.quick_note_title_hint),
                                            style = MaterialTheme.typography.titleLarge.copy(
                                                fontWeight = FontWeight.SemiBold
                                            ),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                                        )
                                    }
                                    innerTextField()
                                }
                            }
                        )

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        ) {
                            val scrollState = rememberScrollState()
                            BasicTextField(
                                value = uiState.content,
                                onValueChange = viewModel::updateContent,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(scrollState),
                            textStyle = MaterialTheme.typography.bodyLarge.copy(
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            enabled = !uiState.isSaving,
                            decorationBox = { innerTextField ->
                                Box {
                                    if (uiState.content.isEmpty()) {
                                        Text(
                                            text = getString(com.flatnotes.android.R.string.quick_note_content_hint),
                                            style = MaterialTheme.typography.bodyLarge.copy(
                                                fontFamily = FontFamily.Monospace
                                            ),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                                        )
                                    }
                                    innerTextField()
                                }
                            }
                        )
                        }

                        if (viewModel.initError != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = viewModel.initError,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        uiState.error?.let { error ->
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = error,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        if (uiState.isSaving) {
                            Spacer(modifier = Modifier.height(12.dp))
                            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = { finish() },
                                modifier = Modifier.weight(1f),
                                enabled = !uiState.isSaving
                            ) {
                                Text(getString(com.flatnotes.android.R.string.quick_note_cancel))
                            }

                            Button(
                                onClick = { viewModel.saveNote() },
                                modifier = Modifier.weight(1f),
                                enabled = !uiState.isSaving && viewModel.initError == null
                            ) {
                                Text(getString(com.flatnotes.android.R.string.quick_note_save))
                            }
                        }
                    }
                }
            }
        }
    }
}


}
