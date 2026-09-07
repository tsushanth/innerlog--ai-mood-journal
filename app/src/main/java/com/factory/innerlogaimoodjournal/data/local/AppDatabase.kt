package com.factory.innerlogaimoodjournal.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.factory.innerlogaimoodjournal.data.local.dao.GoalDao
import com.factory.innerlogaimoodjournal.data.local.dao.HabitDao
import com.factory.innerlogaimoodjournal.data.local.dao.JournalDao
import com.factory.innerlogaimoodjournal.data.local.dao.MoodDao
import com.factory.innerlogaimoodjournal.data.local.entity.GoalEntity
import com.factory.innerlogaimoodjournal.data.local.entity.HabitCompletionEntity
import com.factory.innerlogaimoodjournal.data.local.entity.HabitEntity
import com.factory.innerlogaimoodjournal.data.local.entity.JournalEntryEntity
import com.factory.innerlogaimoodjournal.data.local.entity.MoodEntryEntity

@Database(
    entities = [
        JournalEntryEntity::class,
        HabitEntity::class,
        HabitCompletionEntity::class,
        GoalEntity::class,
        MoodEntryEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun journalDao(): JournalDao
    abstract fun habitDao(): HabitDao
    abstract fun goalDao(): GoalDao
    abstract fun moodDao(): MoodDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "innerlog.db"
                ).build().also { instance = it }
            }
    }
}
