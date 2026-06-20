package com.flatnotes.android.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {

    @Query("SELECT * FROM notes WHERE is_deleted = 0 AND title != '_flatnotes_config' ORDER BY last_modified DESC")
    fun getAllNotes(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE is_deleted = 0 AND title != '_flatnotes_config' ORDER BY last_modified DESC")
    suspend fun getAllNotesList(): List<NoteEntity>

    @Query("SELECT * FROM notes WHERE title = :title LIMIT 1")
    suspend fun getNoteByTitle(title: String): NoteEntity?

    @Query("SELECT * FROM notes WHERE is_deleted = 0 AND title != '_flatnotes_config' AND (title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%') ORDER BY last_modified DESC")
    fun searchNotes(query: String): Flow<List<NoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertNote(note: NoteEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(notes: List<NoteEntity>)

    @Query("UPDATE notes SET is_deleted = 1, is_dirty = 1 WHERE title = :title")
    suspend fun markDeleted(title: String)

    @Query("SELECT * FROM notes WHERE is_dirty = 1 AND is_deleted = 0")
    suspend fun getDirtyNotes(): List<NoteEntity>

    @Query("SELECT * FROM notes WHERE is_deleted = 1 AND is_dirty = 1")
    suspend fun getDeletedNotes(): List<NoteEntity>

    @Query("UPDATE notes SET is_dirty = 0 WHERE title = :title")
    suspend fun markSynced(title: String)

    @Query("DELETE FROM notes WHERE is_deleted = 1 AND is_dirty = 0")
    suspend fun cleanSyncedDeletions()

    @Query("DELETE FROM notes")
    suspend fun clearAll()
}
