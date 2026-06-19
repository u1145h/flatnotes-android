package com.flatnotes.android.ui.notes

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.flatnotes.android.data.api.RetrofitClient
import com.flatnotes.android.data.api.TokenStorage
import com.flatnotes.android.data.local.FlatnotesDatabase
import com.flatnotes.android.data.repository.NoteRepository
import com.flatnotes.android.util.NetworkResult
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class NoteEditorUiState(
    val title: String = "",
    val content: String = "",
    val originalTitle: String = "",
    val isNewNote: Boolean = false,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val saved: Boolean = false
)

class NoteEditorViewModel(application: Application) : AndroidViewModel(application) {

    private val tokenStorage = TokenStorage(application)
    private val dao = FlatnotesDatabase.getInstance(application).noteDao()
    private val noteRepository: NoteRepository

    private val _uiState = MutableStateFlow(NoteEditorUiState())
    val uiState: StateFlow<NoteEditorUiState> = _uiState.asStateFlow()

    init {
        val api = RetrofitClient.create(
            tokenStorage.serverUrl,
            tokenStorage.pathPrefix,
            tokenStorage
        )
        noteRepository = NoteRepository(api, dao)
    }

    fun loadNote(title: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, isNewNote = false) }

            val local = noteRepository.getLocalNote(title)
            if (local != null) {
                _uiState.update {
                    it.copy(
                        title = local.title,
                        content = local.content,
                        originalTitle = local.title,
                        isLoading = false
                    )
                }
            } else {
                when (val result = noteRepository.getRemoteNote(title)) {
                    is NetworkResult.Success -> {
                        val note = result.data
                        _uiState.update {
                            it.copy(
                                title = note.title,
                                content = note.content ?: "",
                                originalTitle = note.title,
                                isLoading = false
                            )
                        }
                    }
                    is NetworkResult.Error -> {
                        _uiState.update {
                            it.copy(isLoading = false, error = result.message)
                        }
                    }
                    else -> {}
                }
            }
        }
    }

    fun newNote() {
        _uiState.update {
            it.copy(
                title = "",
                content = "",
                originalTitle = "",
                isNewNote = true,
                isLoading = false
            )
        }
    }

    fun updateTitle(value: String) {
        _uiState.update { it.copy(title = value, error = null, saved = false) }
    }

    fun updateContent(value: String) {
        _uiState.update { it.copy(content = value, error = null, saved = false) }
    }

    fun saveNote() {
        val state = _uiState.value
        if (state.title.isBlank()) {
            _uiState.update { it.copy(error = "Title cannot be empty") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }

            val result = if (state.isNewNote) {
                noteRepository.createNote(state.title, state.content)
            } else {
                noteRepository.updateNote(
                    state.originalTitle,
                    state.content,
                    if (state.title != state.originalTitle) state.title else null
                )
            }

            when (result) {
                is NetworkResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            saved = true,
                            isNewNote = false,
                            originalTitle = result.data.title
                        )
                    }
                }
                is NetworkResult.Error -> {
                    noteRepository.saveLocalNote(state.title, state.content)
                    _uiState.update {
                        it.copy(isSaving = false, saved = true, error = "Saved offline: ${result.message}")
                    }
                }
                else -> {}
            }
        }
    }
}
