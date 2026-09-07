package com.factory.innerlogaimoodjournal.data.local.dao

import android.app.Application
import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.factory.innerlogaimoodjournal.data.local.AppDatabase
import com.factory.innerlogaimoodjournal.data.local.entity.HabitCompletionEntity
import com.factory.innerlogaimoodjournal.data.local.entity.HabitEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
@Config(application = Application::class)
class HabitDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var dao: HabitDao

    private fun habit(name: String, archived: Boolean = false) = HabitEntity(
        name = name,
        icon = "icon",
        targetDaysPerWeek = 3,
        createdAt = LocalDate.now(),
        isArchived = archived
    )

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = database.habitDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `insertHabit then getAllHabits returns non-archived habits`() = runTest {
        dao.insertHabit(habit("Meditate"))
        dao.insertHabit(habit("Archived", archived = true))

        val habits = dao.getAllHabits().first()

        assertEquals(1, habits.size)
        assertEquals("Meditate", habits.first().name)
    }

    @Test
    fun `updateHabit persists changes`() = runTest {
        val id = dao.insertHabit(habit("Meditate"))
        val updated = habit("Meditate").copy(id = id, targetDaysPerWeek = 7)

        dao.updateHabit(updated)

        val habits = dao.getAllHabits().first()
        assertEquals(7, habits.first().targetDaysPerWeek)
    }

    @Test
    fun `deleteHabit removes habit and cascades to its completions`() = runTest {
        val id = dao.insertHabit(habit("Meditate"))
        dao.insertCompletion(HabitCompletionEntity(habitId = id, date = LocalDate.now()))

        dao.deleteHabit(habit("Meditate").copy(id = id))

        assertEquals(0, dao.getAllHabits().first().size)
        assertEquals(0, dao.getCompletionsForHabit(id).first().size)
    }

    @Test
    fun `getCompletionsForDate returns completions logged on that date`() = runTest {
        val id = dao.insertHabit(habit("Meditate"))
        val today = LocalDate.now()
        dao.insertCompletion(HabitCompletionEntity(habitId = id, date = today))
        dao.insertCompletion(HabitCompletionEntity(habitId = id, date = today.minusDays(1)))

        val todaysCompletions = dao.getCompletionsForDate(today).first()

        assertEquals(1, todaysCompletions.size)
        assertEquals(today, todaysCompletions.first().date)
    }

    @Test
    fun `getCompletion returns null when not completed`() = runTest {
        val id = dao.insertHabit(habit("Meditate"))
        assertNull(dao.getCompletion(id, LocalDate.now()))
    }

    @Test
    fun `inserting a duplicate completion for the same day replaces it`() = runTest {
        val id = dao.insertHabit(habit("Meditate"))
        val today = LocalDate.now()
        dao.insertCompletion(HabitCompletionEntity(habitId = id, date = today, completed = true))
        dao.insertCompletion(HabitCompletionEntity(habitId = id, date = today, completed = true))

        assertEquals(1, dao.getCompletionsForHabit(id).first().size)
    }

    @Test
    fun `deleteCompletion removes only the targeted completion`() = runTest {
        val id = dao.insertHabit(habit("Meditate"))
        val today = LocalDate.now()
        dao.insertCompletion(HabitCompletionEntity(habitId = id, date = today))
        val completion = dao.getCompletion(id, today)!!

        dao.deleteCompletion(completion)

        assertTrue(dao.getCompletionsForHabit(id).first().isEmpty())
    }
}
