package com.flatnotes.android.ui.notes.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.ui.res.painterResource
import com.composables.icons.lucide.R
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flatnotes.android.data.local.NoteEntity
import dev.jeziellago.compose.markdowntext.MarkdownText

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NoteCard(
    note: NoteEntity,
    isPinned: Boolean = false,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isPinned) {
                    Icon(
                        painterResource(R.drawable.lucide_ic_pin),
                        contentDescription = "Pinned",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(4.dp))
                }
                Text(
                    text = note.title,
                    style = MaterialTheme.typography.titleSmall.copy(fontSize = 17.sp),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                if (note.isDirty) {
                    Spacer(Modifier.width(4.dp))
                    Icon(
                        painterResource(R.drawable.lucide_ic_cloud_off),
                        contentDescription = "Unsaved",
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
            if (note.content.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                MarkdownText(
                    markdown = note.content.take(250),
                    maxLines = 4,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }
    }
}
