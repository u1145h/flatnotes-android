package com.flatnotes.android.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [NoteEntity::class], version = 1, exportSchema = false)
abstract class FlatnotesDatabase : RoomDatabase() {

    abstract fun noteDao(): NoteDao

    companion object {
        @Volatile
        private var INSTANCE: FlatnotesDatabase? = null

        fun getInstance(context: Context): FlatnotesDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FlatnotesDatabase::class.java,
                    "flatnotes_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
