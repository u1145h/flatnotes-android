package com.flatnotes.android.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey val title: String,
    val content: String = "",
    @ColumnInfo(name = "last_modified") val lastModified: Double = 0.0,
    @ColumnInfo(name = "is_dirty") val isDirty: Boolean = false,
    @ColumnInfo(name = "is_deleted") val isDeleted: Boolean = false,
    @ColumnInfo(name = "local_edit_version") val localEditVersion: Int = 0
)
