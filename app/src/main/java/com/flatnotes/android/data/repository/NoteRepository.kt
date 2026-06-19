package com.flatnotes.android.data.repository

import com.flatnotes.android.data.api.FlatnotesApiService
import com.flatnotes.android.data.local.NoteDao
import com.flatnotes.android.data.local.NoteEntity
import com.flatnotes.android.data.model.NoteCreateDto
import com.flatnotes.android.data.model.NoteDto
import com.flatnotes.android.data.model.NoteUpdateDto
import com.flatnotes.android.data.model.SearchResultDto
import com.flatnotes.android.util.NetworkResult
import kotlinx.coroutines.flow.Flow

class NoteRepository(
    private val api: FlatnotesApiService,
    private val noteDao: NoteDao
) {

    fun getLocalNotes(): Flow<List<NoteEntity>> = noteDao.getAllNotes()

    fun searchLocalNotes(query: String): Flow<List<NoteEntity>> = noteDao.searchNotes(query)

    suspend fun getLocalNote(title: String): NoteEntity? = noteDao.getNoteByTitle(title)

    suspend fun refreshNotes(): NetworkResult<Unit> {
        return try {
            val response = api.search("*")
            if (response.isSuccessful && response.body() != null) {
                val entities = response.body()!!.map { it.toEntity() }
                noteDao.upsertAll(entities)
                noteDao.cleanSyncedDeletions()
                NetworkResult.Success(Unit)
            } else {
                NetworkResult.Error("Failed to fetch notes: ${response.code()}", response.code())
            }
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Connection error")
        }
    }

    suspend fun getRemoteNote(title: String): NetworkResult<NoteDto> {
        return try {
            val response = api.getNote(title)
            if (response.isSuccessful && response.body() != null) {
                NetworkResult.Success(response.body()!!)
            } else {
                NetworkResult.Error("Failed to get note: ${response.code()}", response.code())
            }
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Connection error")
        }
    }

    suspend fun createNote(title: String, content: String?): NetworkResult<NoteDto> {
        return try {
            val response = api.createNote(NoteCreateDto(title, content))
            if (response.isSuccessful && response.body() != null) {
                val note = response.body()!!
                noteDao.upsertNote(note.toEntity())
                NetworkResult.Success(note)
            } else if (response.code() == 409) {
                NetworkResult.Error("Note already exists", 409)
            } else {
                NetworkResult.Error("Failed to create note: ${response.code()}", response.code())
            }
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Connection error")
        }
    }

    suspend fun updateNote(title: String, newContent: String?, newTitle: String?): NetworkResult<NoteDto> {
        return try {
            val response = api.updateNote(title, NoteUpdateDto(newTitle, newContent))
            if (response.isSuccessful && response.body() != null) {
                val note = response.body()!!
                noteDao.upsertNote(note.toEntity())
                if (newTitle != null && newTitle != title) {
                    noteDao.markDeleted(title)
                }
                NetworkResult.Success(note)
            } else if (response.code() == 409) {
                NetworkResult.Error("Note with that title already exists", 409)
            } else {
                NetworkResult.Error("Failed to update note: ${response.code()}", response.code())
            }
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Connection error")
        }
    }

    suspend fun deleteNote(title: String): NetworkResult<Unit> {
        return try {
            val response = api.deleteNote(title)
            if (response.isSuccessful || response.code() == 204) {
                noteDao.markDeleted(title)
                NetworkResult.Success(Unit)
            } else {
                NetworkResult.Error("Failed to delete note: ${response.code()}", response.code())
            }
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Connection error")
        }
    }

    suspend fun saveLocalNote(title: String, content: String) {
        noteDao.upsertNote(
            NoteEntity(
                title = title,
                content = content,
                isDirty = true
            )
        )
    }

    suspend fun searchRemote(term: String): NetworkResult<List<SearchResultDto>> {
        return try {
            val response = api.search(term)
            if (response.isSuccessful && response.body() != null) {
                NetworkResult.Success(response.body()!!)
            } else {
                NetworkResult.Error("Search failed: ${response.code()}", response.code())
            }
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Connection error")
        }
    }
}

private fun NoteDto.toEntity() = NoteEntity(
    title = title,
    content = content ?: "",
    lastModified = lastModified,
    isDirty = false,
    isDeleted = false
)

private fun SearchResultDto.toEntity() = NoteEntity(
    title = title,
    lastModified = lastModified,
    isDirty = false,
    isDeleted = false
)
