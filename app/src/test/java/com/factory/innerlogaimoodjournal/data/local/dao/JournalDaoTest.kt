package com.factory.innerlogaimoodjournal.data.local.dao

import android.app.Application
import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.factory.innerlogaimoodjournal.data.local.AppDatabase
import com.factory.innerlogaimoodjournal.data.local.entity.JournalEntryEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.time.LocalDateTime

@RunWith(AndroidJUnit4::class)
@Config(application = Application::class)
class JournalDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var dao: JournalDao

    private fun entry(title: String, content: String, minutesAgo: Long = 0, favorite: Boolean = false) =
        JournalEntryEntity(
            title = title,
            content = content,
            mood = "OKAY",
            createdAt = LocalDateTime.now().minusMinutes(minutesAgo),
            updatedAt = LocalDateTime.now().minusMinutes(minutesAgo),
            isFavorite = favorite
        )

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = database.journalDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `insert then getEntryById returns the persisted entry`() = runTest {
        val id = dao.insert(entry("Title", "Content"))

        val loaded = dao.getEntryById(id)

        assertEquals("Title", loaded?.title)
        assertEquals("Content", loaded?.content)
    }

    @Test
    fun `getEntryById returns null for missing id`() = runTest {
        assertNull(dao.getEntryById(999L))
    }

    @Test
    fun `getAllEntries orders newest first`() = runTest {
        dao.insert(entry("Old", "content", minutesAgo = 10))
        dao.insert(entry("New", "content", minutesAgo = 0))

        val entries = dao.getAllEntries().first()

        assertEquals(listOf("New", "Old"), entries.map { it.title })
    }

    @Test
    fun `getFavoriteEntries returns only favorites`() = runTest {
        dao.insert(entry("Fav", "content", favorite = true))
        dao.insert(entry("NotFav", "content", favorite = false))

        val favorites = dao.getFavoriteEntries().first()

        assertEquals(1, favorites.size)
        assertEquals("Fav", favorites.first().title)
    }

    @Test
    fun `searchEntries matches title or content substring`() = runTest {
        dao.insert(entry("Morning walk", "felt great"))
        dao.insert(entry("Random", "nothing special"))

        val results = dao.searchEntries("walk").first()

        assertEquals(1, results.size)
        assertEquals("Morning walk", results.first().title)
    }

    @Test
    fun `update modifies the persisted row`() = runTest {
        val id = dao.insert(entry("Title", "Content"))
        val updated = dao.getEntryById(id)!!.copy(title = "Updated title")

        dao.update(updated)

        assertEquals("Updated title", dao.getEntryById(id)?.title)
    }

    @Test
    fun `delete removes the row`() = runTest {
        val id = dao.insert(entry("Title", "Content"))
        val saved = dao.getEntryById(id)!!

        dao.delete(saved)

        assertNull(dao.getEntryById(id))
    }

    @Test
    fun `getEntryCount reflects number of rows`() = runTest {
        assertEquals(0, dao.getEntryCount().first())

        dao.insert(entry("A", "content"))
        dao.insert(entry("B", "content"))

        assertEquals(2, dao.getEntryCount().first())
    }
}
