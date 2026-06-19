package com.flatnotes.android.ui.notes

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.flatnotes.android.data.api.RetrofitClient
import com.flatnotes.android.data.api.TokenStorage
import com.flatnotes.android.data.local.FlatnotesDatabase
import com.flatnotes.android.data.local.NoteEntity
import com.flatnotes.android.data.repository.NoteRepository
import com.flatnotes.android.sync.SyncManager
import com.flatnotes.android.util.NetworkResult
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class NoteListUiState(
    val notes: List<NoteEntity> = emptyList(),
    val isLoading: Boolean = false,
    val isSyncing: Boolean = false,
    val searchQuery: String = "",
    val error: String? = null,
    val syncResult: String? = null
)

class NoteListViewModel(application: Application) : AndroidViewModel(application) {

    private val tokenStorage = TokenStorage(application)
    private val dao = FlatnotesDatabase.getInstance(application).noteDao()
    private val noteRepository: NoteRepository
    private val syncManager: SyncManager

    private val _uiState = MutableStateFlow(NoteListUiState())
    val uiState: StateFlow<NoteListUiState> = _uiState.asStateFlow()

    init {
        val api = RetrofitClient.create(
            tokenStorage.serverUrl,
            tokenStorage.pathPrefix,
            tokenStorage
        )
        noteRepository = NoteRepository(api, dao)
        syncManager = SyncManager(api, dao)

        viewModelScope.launch {
            noteRepository.getLocalNotes().collect { notes ->
                _uiState.update { it.copy(notes = notes) }
            }
        }

        refreshNotes()
    }

    fun refreshNotes() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = noteRepository.refreshNotes()) {
                is NetworkResult.Success -> {}
                is NetworkResult.Error -> {
                    _uiState.update { it.copy(error = result.message) }
                }
                else -> {}
            }
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun syncNotes() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSyncing = true, syncResult = null) }
            val result = syncManager.sync()
            _uiState.update {
                it.copy(
                    isSyncing = false,
                    syncResult = "Pushed: ${result.pushed}, Pulled: ${result.pulled}" +
                            if (result.conflicts.isNotEmpty()) ", Conflicts: ${result.conflicts.size}" else "" +
                            if (result.errors.isNotEmpty()) ", Errors: ${result.errors.size}" else ""
                )
            }
        }
    }

    fun search(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        viewModelScope.launch {
            if (query.isBlank()) {
                noteRepository.getLocalNotes().collect { notes ->
                    _uiState.update { it.copy(notes = notes) }
                }
            } else {
                noteRepository.searchLocalNotes(query).collect { notes ->
                    _uiState.update { it.copy(notes = notes) }
                }
            }
        }
    }

    fun deleteNote(title: String) {
        viewModelScope.launch {
            when (val result = noteRepository.deleteNote(title)) {
                is NetworkResult.Success -> {}
                is NetworkResult.Error -> {
                    _uiState.update { it.copy(error = result.message) }
                }
                else -> {}
            }
        }
    }

    fun clearSyncResult() {
        _uiState.update { it.copy(syncResult = null) }
    }

    fun logout() {
        tokenStorage.clearToken()
    }
}
