package com.factory.innerlogaimoodjournal.data.local.dao

import android.app.Application
import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.factory.innerlogaimoodjournal.data.local.AppDatabase
import com.factory.innerlogaimoodjournal.data.local.entity.MoodEntryEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.time.LocalDate
import java.time.LocalDateTime

@RunWith(AndroidJUnit4::class)
@Config(application = Application::class)
class MoodDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var dao: MoodDao

    private fun moodEntry(score: Int, date: LocalDate) = MoodEntryEntity(
        date = date,
        moodScore = score,
        note = "",
        createdAt = LocalDateTime.now()
    )

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = database.moodDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `insert then getAllMoodEntries returns newest first`() = runTest {
        dao.insert(moodEntry(3, LocalDate.now().minusDays(1)))
        dao.insert(moodEntry(5, LocalDate.now()))

        val entries = dao.getAllMoodEntries().first()

        assertEquals(listOf(5, 3), entries.map { it.moodScore })
    }

    @Test
    fun `getMoodEntriesSince excludes entries before the cutoff`() = runTest {
        dao.insert(moodEntry(3, LocalDate.now().minusDays(10)))
        dao.insert(moodEntry(4, LocalDate.now().minusDays(1)))

        val recent = dao.getMoodEntriesSince(LocalDate.now().minusDays(5)).first()

        assertEquals(1, recent.size)
        assertEquals(4, recent.first().moodScore)
    }

    @Test
    fun `getMoodEntryForDate returns the entry logged that day`() = runTest {
        val today = LocalDate.now()
        dao.insert(moodEntry(4, today))

        val entry = dao.getMoodEntryForDate(today)

        assertEquals(4, entry?.moodScore)
    }

    @Test
    fun `getMoodEntryForDate returns null when nothing logged`() = runTest {
        assertNull(dao.getMoodEntryForDate(LocalDate.now()))
    }

    @Test
    fun `inserting with an existing id replaces the entry`() = runTest {
        val id = dao.insert(moodEntry(3, LocalDate.now()))

        dao.insert(moodEntry(5, LocalDate.now()).copy(id = id))

        val entries = dao.getAllMoodEntries().first()
        assertEquals(1, entries.size)
        assertEquals(5, entries.first().moodScore)
    }

    @Test
    fun `update modifies the persisted mood entry`() = runTest {
        val id = dao.insert(moodEntry(3, LocalDate.now()))
        val updated = dao.getMoodEntryForDate(LocalDate.now())!!.copy(moodScore = 5)

        dao.update(updated)

        assertEquals(5, dao.getMoodEntryForDate(LocalDate.now())?.moodScore)
        assertEquals(id, dao.getMoodEntryForDate(LocalDate.now())?.id)
    }

    @Test
    fun `delete removes the mood entry`() = runTest {
        val today = LocalDate.now()
        dao.insert(moodEntry(3, today))
        val saved = dao.getMoodEntryForDate(today)!!

        dao.delete(saved)

        assertNull(dao.getMoodEntryForDate(today))
    }
}
