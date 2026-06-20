package com.flatnotes.android.ui.notes

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.NoteAdd
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.flatnotes.android.ui.notes.components.NoteCard
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteListScreen(
    viewModel: NoteListViewModel,
    onNoteClick: (String) -> Unit,
    onCreateNote: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToSearch: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val isRefreshing = uiState.isLoading
    val pullToRefreshState = rememberPullToRefreshState()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    if (pullToRefreshState.isRefreshing) {
        LaunchedEffect(true) {
            viewModel.refreshNotes()
        }
    }

    LaunchedEffect(isRefreshing) {
        if (!isRefreshing) {
            pullToRefreshState.endRefresh()
        }
    }

    val showPullIndicator = pullToRefreshState.isRefreshing || pullToRefreshState.progress > 0f

    val pinnedNotes = uiState.notes.filter { viewModel.isPinned(it.title) }
    val unpinnedNotes = uiState.notes.filter { !viewModel.isPinned(it.title) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = true,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier
                    .width(250.dp)
                    .fillMaxHeight()
            ) {
                Column(
                    modifier = Modifier.padding(start = 16.dp, bottom = 8.dp, top = 20.dp)
                ) {
                    Text(
                        "Flatnotes",
                        style = MaterialTheme.typography.headlineMedium
                    )
                }

                HorizontalDivider()

                NavigationDrawerItem(
                    icon = { Icon(Icons.AutoMirrored.Filled.NoteAdd, contentDescription = null) },
                    label = { Text("All Notes") },
                    selected = true,
                    onClick = {
                        scope.launch { drawerState.close() }
                    }
                )

                Spacer(Modifier.weight(1f))

                HorizontalDivider()

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                    label = { Text("Settings") },
                    selected = false,
                    onClick = {
                        scope.launch {
                            drawerState.close()
                            onNavigateToSettings()
                        }
                    }
                )
            }
        }
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val topFraction = if (maxWidth > maxHeight) 0.25f else 0.4f

            Column(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(topFraction),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Notes",
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontWeight = FontWeight.Normal
                        )
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .nestedScroll(pullToRefreshState.nestedScrollConnection)
                    ) {
                        Column(modifier = Modifier.fillMaxSize()) {
                            Spacer(Modifier.height(52.dp))

                            if (uiState.isSyncing) {
                                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                            }

                            uiState.syncResult?.let { result ->
                                Snackbar(
                                    modifier = Modifier.padding(8.dp),
                                    action = {
                                        TextButton(onClick = viewModel::clearSyncResult) {
                                            Text("Dismiss")
                                        }
                                    }
                                ) {
                                    Text(result)
                                }
                            }

                            uiState.error?.let { error ->
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.errorContainer
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp)
                                ) {
                                    Text(
                                        text = error,
                                        modifier = Modifier.padding(12.dp),
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                            }

                            if (uiState.notes.isEmpty() && !uiState.isLoading) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            Icons.AutoMirrored.Filled.NoteAdd,
                                            contentDescription = null,
                                            modifier = Modifier.size(64.dp),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(Modifier.height(16.dp))
                                        Text(
                                            "No notes yet",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            "Tap + to create one",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            } else {
                                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                                    val columns = when {
                                        maxWidth <= 550.dp -> 2
                                        maxWidth <= 800.dp -> 3
                                        else -> 4
                                    }

                                    LazyVerticalStaggeredGrid(
                                        columns = StaggeredGridCells.Fixed(columns),
                                        modifier = Modifier.fillMaxSize(),
                                        contentPadding = PaddingValues(8.dp),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalItemSpacing = 8.dp
                                    ) {
                                        items(pinnedNotes, key = { it.title }) { note ->
                                            var showMenu by remember { mutableStateOf(false) }
                                            Box {
                                                NoteCard(
                                                    note = note,
                                                    isPinned = true,
                                                    onClick = { onNoteClick(note.title) },
                                                    onLongClick = { showMenu = true }
                                                )
                                                DropdownMenu(
                                                    expanded = showMenu,
                                                    onDismissRequest = { showMenu = false }
                                                ) {
                                                    DropdownMenuItem(
                                                        text = { Text("Unpin") },
                                                        onClick = {
                                                            showMenu = false
                                                            viewModel.togglePin(note.title)
                                                        },
                                                        leadingIcon = {
                                                            Icon(Icons.Default.Star, contentDescription = null)
                                                        }
                                                    )
                                                }
                                            }
                                        }

                                        if (pinnedNotes.isNotEmpty()) {
                                            item(span = StaggeredGridItemSpan.FullLine) {
                                                Text(
                                                    "All Notes",
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(vertical = 12.dp),
                                                    textAlign = TextAlign.Center,
                                                    style = MaterialTheme.typography.titleMedium,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }

                                        items(unpinnedNotes, key = { it.title }) { note ->
                                            var showMenu by remember { mutableStateOf(false) }
                                            Box {
                                                NoteCard(
                                                    note = note,
                                                    isPinned = false,
                                                    onClick = { onNoteClick(note.title) },
                                                    onLongClick = { showMenu = true }
                                                )
                                                DropdownMenu(
                                                    expanded = showMenu,
                                                    onDismissRequest = { showMenu = false }
                                                ) {
                                                    DropdownMenuItem(
                                                        text = { Text("Pin") },
                                                        onClick = {
                                                            showMenu = false
                                                            viewModel.togglePin(note.title)
                                                        },
                                                        leadingIcon = {
                                                            Icon(Icons.Default.Star, contentDescription = null)
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        if (showPullIndicator) {
                            PullToRefreshContainer(
                                state = pullToRefreshState,
                                modifier = Modifier.align(Alignment.TopCenter)
                            )
                        }

                        FloatingActionButton(
                            onClick = onCreateNote,
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(16.dp)
                                .size(66.dp),
                            shape = CircleShape
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Create note",
                                modifier = Modifier.size(35.dp)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(Modifier.padding(start = 8.dp)) {
                            IconButton(onClick = {
                                scope.launch { drawerState.open() }
                            }) {
                                Icon(Icons.Default.Menu, contentDescription = "Open menu")
                            }
                        }
                        Row(Modifier.padding(end = 8.dp)) {
                            IconButton(onClick = onNavigateToSearch) {
                                Icon(Icons.Default.Search, contentDescription = "Search")
                            }
                        }
                    }
                }
            }
        }
    }
}
