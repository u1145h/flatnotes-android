package com.flatnotes.android.ui.notes

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.res.painterResource
import com.composables.icons.lucide.R
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import dev.jeziellago.compose.markdowntext.MarkdownText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditorScreen(
    viewModel: NoteEditorViewModel,
    title: String?,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showPreview by remember { mutableStateOf(title != null && title.isNotEmpty()) }
    var contentFieldValue by remember { mutableStateOf(TextFieldValue(uiState.content)) }

    LaunchedEffect(uiState.content) {
        if (uiState.content != contentFieldValue.text) {
            contentFieldValue = TextFieldValue(uiState.content)
        }
    }

    LaunchedEffect(title) {
        if (title != null && title.isNotEmpty()) {
            viewModel.loadNote(title)
        } else {
            viewModel.newNote()
        }
    }

    DisposableEffect(Unit) {
        onDispose { viewModel.saveOnExit() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (uiState.isNewNote) "New Note" else "Edit Note",
                        maxLines = 1
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(painterResource(R.drawable.lucide_ic_chevron_left), contentDescription = "Back")
                    }
                },
                actions = {
                    if (!uiState.isNewNote) {
                        var showMenu by remember { mutableStateOf(false) }
                        IconButton(onClick = { showMenu = true }) {
                            Icon(painterResource(R.drawable.lucide_ic_ellipsis_vertical), contentDescription = "More")
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                                onClick = {
                                    showMenu = false
                                    viewModel.deleteNote(onNavigateBack)
                                }
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                CircularProgressIndicator(modifier = Modifier.padding(16.dp))
            }
        } else {
            Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 16.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    BasicTextField(
                        value = uiState.title,
                        onValueChange = viewModel::updateTitle,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .padding(bottom = 16.dp),
                        textStyle = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        singleLine = true,
                        decorationBox = { innerTextField ->
                            Box {
                                if (uiState.title.isEmpty()) {
                                    Text(
                                        text = "Title",
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

                    if (showPreview) {
                        val segments = remember(contentFieldValue.text) {
                            parsePreviewSegments(contentFieldValue.text)
                        }
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = 16.dp)
                        ) {
                            if (contentFieldValue.text.isBlank()) {
                                Text(
                                    text = "*No content*",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            } else {
                                segments.forEach { segment ->
                                    when (segment) {
                                        is PreviewSegment.Markdown -> MarkdownText(
                                            markdown = segment.text,
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                        is PreviewSegment.Checkbox -> {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable {
                                                        val toggled = toggleCheckbox(contentFieldValue.text, segment.lineIndex)
                                                        contentFieldValue = TextFieldValue(toggled)
                                                        viewModel.updateContent(toggled)
                                                    },
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Checkbox(
                                                    checked = segment.checked,
                                                    onCheckedChange = {
                                                        val toggled = toggleCheckbox(contentFieldValue.text, segment.lineIndex)
                                                        contentFieldValue = TextFieldValue(toggled)
                                                        viewModel.updateContent(toggled)
                                                    }
                                                )
                                                Text(
                                                    text = segment.label,
                                                    style = MaterialTheme.typography.bodyLarge
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        MarkdownToolbar(onInsert = { transform ->
                            contentFieldValue = transform(contentFieldValue)
                            viewModel.updateContent(contentFieldValue.text)
                        })
                        Spacer(Modifier.height(8.dp))
                        BasicTextField(
                            value = contentFieldValue,
                            onValueChange = {
                                contentFieldValue = it
                                viewModel.updateContent(it.text)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(start = 16.dp, end = 16.dp, top = 10.dp),
                            textStyle = MaterialTheme.typography.bodyLarge.copy(
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            decorationBox = { innerTextField ->
                                Box {
                                    if (contentFieldValue.text.isEmpty()) {
                                        Text(
                                            text = "Start writing...",
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

                    uiState.error?.let { error ->
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }

                }

                FloatingActionButton(
                    onClick = {
                        viewModel.saveOnExit()
                        showPreview = !showPreview
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                        .size(66.dp),
                    shape = CircleShape
                ) {
                    Icon(
                        painterResource(if (showPreview) R.drawable.lucide_ic_pen else R.drawable.lucide_ic_book_open),
                        contentDescription = if (showPreview) "Edit" else "Preview",
                        modifier = Modifier.size(25.dp)
                    )
                }
            }
        }
    }
}

private data class MarkdownAction(val icon: Int, val contentDescription: String, val transform: TextFieldValue.() -> TextFieldValue)

@Composable
private fun MarkdownToolbar(onInsert: (TextFieldValue.() -> TextFieldValue) -> Unit) {
    val actions = remember {
        listOf(
            MarkdownAction(R.drawable.lucide_ic_list_todo, "Task list") { insertLinePrefix("- [ ] ") },
            MarkdownAction(R.drawable.lucide_ic_heading_1, "Heading 1") { insertLinePrefix("# ") },
            MarkdownAction(R.drawable.lucide_ic_heading_2, "Heading 2") { insertLinePrefix("## ") },
            MarkdownAction(R.drawable.lucide_ic_heading_3, "Heading 3") { insertLinePrefix("### ") },
            MarkdownAction(R.drawable.lucide_ic_heading_4, "Heading 4") { insertLinePrefix("#### ") },
            MarkdownAction(R.drawable.lucide_ic_heading_5, "Heading 5") { insertLinePrefix("##### ") },
            MarkdownAction(R.drawable.lucide_ic_heading_6, "Heading 6") { insertLinePrefix("###### ") },
            MarkdownAction(R.drawable.lucide_ic_bold, "Bold") { wrapSelection("**", "**") },
            MarkdownAction(R.drawable.lucide_ic_italic, "Italic") { wrapSelection("*", "*") },
            MarkdownAction(R.drawable.lucide_ic_link, "Link") { insertLink() },
            MarkdownAction(R.drawable.lucide_ic_code, "Code") { wrapSelection("`", "`") },
            MarkdownAction(R.drawable.lucide_ic_quote, "Quote") { insertLinePrefix("> ") },
            MarkdownAction(R.drawable.lucide_ic_image, "Image") { insertImage() },
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(vertical = 2.dp)
            .horizontalScroll(rememberScrollState()),
        verticalAlignment = Alignment.CenterVertically
    ) {
        actions.forEach { action ->
            IconButton(
                onClick = { onInsert(action.transform) },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    painterResource(action.icon),
                    contentDescription = action.contentDescription,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

private fun TextFieldValue.wrapSelection(prefix: String, suffix: String): TextFieldValue {
    val selected = if (selection.collapsed) "text" else text.substring(selection.min, selection.max)
    val newText = text.substring(0, selection.min) + prefix + selected + suffix + text.substring(selection.max)
    val cursorPos = selection.min + prefix.length + selected.length + suffix.length
    return TextFieldValue(newText, TextRange(cursorPos))
}

private fun TextFieldValue.insertLinePrefix(prefix: String): TextFieldValue {
    val pos = selection.start
    val newText = text.substring(0, pos) + prefix + text.substring(pos)
    return TextFieldValue(newText, TextRange(pos + prefix.length))
}

private fun TextFieldValue.insertLink(): TextFieldValue {
    val selected = if (selection.collapsed) "" else text.substring(selection.min, selection.max)
    val linkText = selected.ifEmpty { "text" }
    val insertion = "[$linkText](url)"
    val newText = text.substring(0, selection.min) + insertion + text.substring(selection.max)
    val cursorPos = selection.min + insertion.length
    return TextFieldValue(newText, TextRange(cursorPos))
}

private fun TextFieldValue.insertImage(): TextFieldValue {
    val insertion = "![alt](url)"
    val newText = text.substring(0, selection.start) + insertion + text.substring(selection.start)
    return TextFieldValue(newText, TextRange(selection.start + insertion.length))
}

private sealed class PreviewSegment {
    data class Markdown(val text: String) : PreviewSegment()
    data class Checkbox(val checked: Boolean, val label: String, val lineIndex: Int) : PreviewSegment()
}

private fun parsePreviewSegments(content: String): List<PreviewSegment> {
    val segments = mutableListOf<PreviewSegment>()
    val lines = content.split("\n")
    var i = 0
    while (i < lines.size) {
        val line = lines[i]
        val match = Regex("^(- \\[([ x])] )(.*)$").find(line)
        if (match != null) {
            segments.add(
                PreviewSegment.Checkbox(
                    checked = match.groupValues[2] == "x",
                    label = match.groupValues[3],
                    lineIndex = i
                )
            )
            i++
        } else {
            val markdownLines = mutableListOf(line)
            i++
            while (i < lines.size) {
                if (Regex("^(- \\[[ x]] )(.*)$").matches(lines[i])) break
                markdownLines.add(lines[i])
                i++
            }
            segments.add(PreviewSegment.Markdown(markdownLines.joinToString("\n")))
        }
    }
    return segments
}

private fun toggleCheckbox(content: String, lineIndex: Int): String {
    val lines = content.split("\n").toMutableList()
    val line = lines[lineIndex]
    lines[lineIndex] = if ("- [ ]" in line) {
        line.replaceFirst("- [ ]", "- [x]")
    } else {
        line.replaceFirst("- [x]", "- [ ]")
    }
    return lines.joinToString("\n")
}
