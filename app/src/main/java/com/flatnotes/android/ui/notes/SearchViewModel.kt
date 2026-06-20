package com.flatnotes.android.ui.notes

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.flatnotes.android.data.local.FlatnotesDatabase
import com.flatnotes.android.data.local.NoteEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SearchUiState(
    val searchQuery: String = "",
    val notes: List<NoteEntity> = emptyList()
)

class SearchViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = FlatnotesDatabase.getInstance(application).noteDao()

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    init {
        loadAllNotes()
    }

    fun search(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        viewModelScope.launch {
            if (query.isBlank()) {
                loadAllNotes()
            } else {
                dao.searchNotes(query).collect { notes ->
                    _uiState.update { it.copy(notes = notes) }
                }
            }
        }
    }

    private fun loadAllNotes() {
        viewModelScope.launch {
            dao.getAllNotes().collect { notes ->
                _uiState.update { it.copy(notes = notes) }
            }
        }
    }
}
