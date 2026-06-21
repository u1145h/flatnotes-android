package com.flatnotes.android.ui.notes

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.flatnotes.android.data.api.RetrofitClient
import com.flatnotes.android.data.api.TokenStorage
import com.flatnotes.android.data.local.FlatnotesDatabase
import com.flatnotes.android.data.repository.NoteRepository
import com.flatnotes.android.util.NetworkResult
import kotlinx.coroutines.FlowPreview
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

@OptIn(FlowPreview::class)
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

        viewModelScope.launch {
            _uiState
                .drop(1)
                .debounce(1000L)
                .collect { performAutoSave() }
        }
    }

    fun loadNote(title: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, isNewNote = false) }

            val local = noteRepository.getLocalNote(title)

            if (local != null && local.content.isNotBlank()) {
                _uiState.update {
                    it.copy(
                        title = local.title,
                        content = local.content,
                        originalTitle = local.title,
                        isLoading = false,
                        saved = true
                    )
                }
                return@launch
            }

            when (val result = noteRepository.getRemoteNote(title)) {
                is NetworkResult.Success -> {
                    val note = result.data
                    if (local != null) {
                        dao.upsertNote(
                            com.flatnotes.android.data.local.NoteEntity(
                                title = note.title,
                                content = note.content ?: "",
                                lastModified = note.lastModified,
                                isDirty = false,
                                isDeleted = false
                            )
                        )
                    }
                    _uiState.update {
                        it.copy(
                            title = note.title,
                            content = note.content ?: "",
                            originalTitle = note.title,
                            isLoading = false,
                            saved = true
                        )
                    }
                }
                is NetworkResult.Error -> {
                    if (local != null) {
                        _uiState.update {
                            it.copy(
                                title = local.title,
                                content = local.content,
                                originalTitle = local.title,
                                isLoading = false,
                                saved = true
                            )
                        }
                    } else {
                        _uiState.update {
                            it.copy(isLoading = false, error = result.message, saved = true)
                        }
                    }
                }
                else -> {}
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
        viewModelScope.launch { saveInternal() }
    }

    fun saveOnExit() {
        val s = _uiState.value
        if (s.title.isBlank()) return
        if (s.saved) return
        viewModelScope.launch { saveInternal() }
    }

    private suspend fun performAutoSave() {
        val s = _uiState.value
        if (s.isNewNote) return
        if (s.title.isBlank()) return
        if (s.isSaving) return
        if (s.saved) return
        saveInternal()
    }

    private suspend fun saveInternal() {
        val stateAtStart = _uiState.value
        val contentAtStart = stateAtStart.content
        val titleAtStart = stateAtStart.title
        _uiState.update { it.copy(isSaving = true, error = null) }

        val result = if (stateAtStart.isNewNote) {
            noteRepository.createNote(stateAtStart.title, stateAtStart.content)
        } else {
            noteRepository.updateNote(
                stateAtStart.originalTitle,
                stateAtStart.content,
                if (stateAtStart.title != stateAtStart.originalTitle) stateAtStart.title else null
            )
        }

        when (result) {
            is NetworkResult.Success -> {
                val stateNow = _uiState.value
                val hasNewerChanges = stateNow.content != contentAtStart || stateNow.title != titleAtStart
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        saved = !hasNewerChanges,
                        isNewNote = false,
                        originalTitle = result.data.title
                    )
                }
            }
            is NetworkResult.Error -> {
                noteRepository.saveLocalNote(stateAtStart.title, stateAtStart.content)
                _uiState.update {
                    it.copy(isSaving = false, saved = true, error = "Saved offline: ${result.message}")
                }
            }
            else -> {}
        }
    }

    fun deleteNote(onDone: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            noteRepository.deleteNote(_uiState.value.originalTitle)
            noteRepository.markLocalDeleted(_uiState.value.originalTitle)
            onDone()
        }
    }
}
