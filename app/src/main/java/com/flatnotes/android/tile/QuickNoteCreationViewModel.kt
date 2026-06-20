package com.flatnotes.android.tile

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.flatnotes.android.data.api.RetrofitClient
import com.flatnotes.android.data.api.TokenStorage
import com.flatnotes.android.data.local.FlatnotesDatabase
import com.flatnotes.android.data.repository.NoteRepository
import com.flatnotes.android.util.NetworkResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun getDefaultQuickNoteTitle(): String {
    val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
    return "Quick Note - ${dateFormat.format(Date())}"
}

data class QuickNoteUiState(
    val title: String = getDefaultQuickNoteTitle(),
    val content: String = "",
    val isSaving: Boolean = false,
    val saved: Boolean = false,
    val savedOffline: Boolean = false,
    val error: String? = null
)

class QuickNoteCreationViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "QuickNoteVM"
    }

    private val noteRepository: NoteRepository?
    val initError: String?

    private val _uiState = MutableStateFlow(QuickNoteUiState())
    val uiState: StateFlow<QuickNoteUiState> = _uiState.asStateFlow()

    init {
        var repo: NoteRepository? = null
        var err: String? = null
        try {
            val ts = TokenStorage(application)
            val dao = FlatnotesDatabase.getInstance(application).noteDao()
            val api = RetrofitClient.create(
                ts.serverUrl,
                ts.pathPrefix,
                ts
            )
            repo = NoteRepository(api, dao)
            Log.d(TAG, "NoteRepository initialized")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize NoteRepository", e)
            err = "Failed to initialize: ${e.message}"
        }
        noteRepository = repo
        initError = err
    }

    fun updateTitle(value: String) {
        _uiState.update { it.copy(title = value, error = null) }
    }

    fun updateContent(value: String) {
        _uiState.update { it.copy(content = value) }
    }

    fun saveNote() {
        val state = _uiState.value
        if (state.title.isBlank()) {
            _uiState.update { it.copy(error = "Title cannot be empty") }
            return
        }

        val repo = noteRepository
        if (repo == null) {
            _uiState.update { it.copy(error = initError ?: "Not initialized") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }

            try {
                val result = repo.createNote(
                    state.title,
                    state.content.ifBlank { null }
                )

                when (result) {
                    is NetworkResult.Success -> {
                        Log.d(TAG, "Note created successfully")
                        _uiState.update { it.copy(isSaving = false, saved = true) }
                    }
                    is NetworkResult.Error -> {
                        Log.w(TAG, "API failed, saving locally: ${result.message}")
                        repo.saveLocalNote(state.title, state.content)
                        _uiState.update { it.copy(isSaving = false, saved = true, savedOffline = true) }
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception during save", e)
                try {
                    repo.saveLocalNote(state.title, state.content)
                    _uiState.update { it.copy(isSaving = false, saved = true, savedOffline = true) }
                } catch (e2: Exception) {
                    _uiState.update { it.copy(isSaving = false, error = "Save failed: ${e2.message}") }
                }
            }
        }
    }
}
