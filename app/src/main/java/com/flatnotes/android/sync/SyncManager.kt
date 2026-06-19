package com.flatnotes.android.sync

import com.flatnotes.android.data.api.FlatnotesApiService
import com.flatnotes.android.data.local.NoteDao
import com.flatnotes.android.data.model.NoteCreateDto
import com.flatnotes.android.data.model.NoteUpdateDto

class SyncManager(
    private val api: FlatnotesApiService,
    private val noteDao: NoteDao
) {

    data class SyncResult(
        val pushed: Int = 0,
        val pulled: Int = 0,
        val conflicts: List<String> = emptyList(),
        val errors: List<String> = emptyList()
    )

    suspend fun sync(): SyncResult {
        var result = SyncResult()

        result = pushLocalChanges(result)
        result = pullRemoteChanges(result)

        return result
    }

    private suspend fun pushLocalChanges(result: SyncResult): SyncResult {
        var r = result

        val deletedNotes = noteDao.getDeletedNotes()
        for (note in deletedNotes) {
            try {
                val response = api.deleteNote(note.title)
                if (response.isSuccessful || response.code() == 204) {
                    noteDao.cleanSyncedDeletions()
                    r = r.copy(pushed = r.pushed + 1)
                } else {
                    r = r.copy(errors = r.errors + "Failed to delete '${note.title}'")
                }
            } catch (e: Exception) {
                r = r.copy(errors = r.errors + "Error deleting '${note.title}': ${e.message}")
            }
        }

        val dirtyNotes = noteDao.getDirtyNotes()
        for (note in dirtyNotes) {
            try {
                val existing = try {
                    val getResponse = api.getNote(note.title)
                    if (getResponse.isSuccessful) getResponse.body() else null
                } catch (_: Exception) { null }

                if (existing != null) {
                    val updateResponse = api.updateNote(
                        note.title,
                        NoteUpdateDto(newTitle = null, newContent = note.content)
                    )
                    if (updateResponse.isSuccessful) {
                        noteDao.markSynced(note.title)
                        r = r.copy(pushed = r.pushed + 1)
                    } else if (updateResponse.code() == 409) {
                        r = r.copy(conflicts = r.conflicts + note.title)
                    } else {
                        r = r.copy(errors = r.errors + "Failed to update '${note.title}'")
                    }
                } else {
                    val createResponse = api.createNote(NoteCreateDto(note.title, note.content))
                    if (createResponse.isSuccessful) {
                        noteDao.markSynced(note.title)
                        r = r.copy(pushed = r.pushed + 1)
                    } else if (createResponse.code() == 409) {
                        r = r.copy(conflicts = r.conflicts + note.title)
                    } else {
                        r = r.copy(errors = r.errors + "Failed to create '${note.title}'")
                    }
                }
            } catch (e: Exception) {
                r = r.copy(errors = r.errors + "Error syncing '${note.title}': ${e.message}")
            }
        }

        return r
    }

    private suspend fun pullRemoteChanges(result: SyncResult): SyncResult {
        var r = result
        try {
            val response = api.search("*")
            if (response.isSuccessful && response.body() != null) {
                val remoteNotes = response.body()!!
                for (remote in remoteNotes) {
                    val localNote = noteDao.getNoteByTitle(remote.title)
                    if (localNote == null || (!localNote.isDirty && remote.lastModified > localNote.lastModified)) {
                        val detailResponse = api.getNote(remote.title)
                        if (detailResponse.isSuccessful && detailResponse.body() != null) {
                            val noteDetail = detailResponse.body()!!
                            noteDao.upsertNote(
                                com.flatnotes.android.data.local.NoteEntity(
                                    title = noteDetail.title,
                                    content = noteDetail.content ?: "",
                                    lastModified = noteDetail.lastModified,
                                    isDirty = false,
                                    isDeleted = false
                                )
                            )
                            r = r.copy(pulled = r.pulled + 1)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            r = r.copy(errors = r.errors + "Error pulling: ${e.message}")
        }
        return r
    }
}
