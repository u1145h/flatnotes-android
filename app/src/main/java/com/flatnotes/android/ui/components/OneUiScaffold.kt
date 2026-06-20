package com.flatnotes.android.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun OneUiScaffold(
    title: String,
    showBackButton: Boolean = true,
    onBack: () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    content: @Composable () -> Unit
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val topFraction = if (maxWidth > maxHeight) 0.25f else 0.4f

        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(topFraction)
                    .padding(start = 8.dp, end = 8.dp, top = 8.dp)
            ) {
                if (showBackButton) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.align(Alignment.TopStart)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(end = 4.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    actions()
                }

                Text(
                    text = title,
                    style = MaterialTheme.typography.displayLarge,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 24.dp, end = 24.dp)
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                content()
            }
        }
    }
}
