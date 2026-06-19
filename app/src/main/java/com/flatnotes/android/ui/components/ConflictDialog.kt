package com.flatnotes.android.ui.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight

@Composable
fun ConflictDialog(
    noteTitle: String,
    onDismiss: () -> Unit,
    onKeepLocal: () -> Unit,
    onOverwriteServer: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Conflict Detected", fontWeight = FontWeight.Bold)
        },
        text = {
            Text("The note \"$noteTitle\" has been modified on the server. What would you like to do?")
        },
        confirmButton = {
            TextButton(onClick = onOverwriteServer) {
                Text("Overwrite Server")
            }
        },
        dismissButton = {
            TextButton(onClick = onKeepLocal) {
                Text("Keep Local")
            }
        }
    )
}
