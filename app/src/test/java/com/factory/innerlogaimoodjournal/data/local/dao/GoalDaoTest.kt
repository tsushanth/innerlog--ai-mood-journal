package com.factory.innerlogaimoodjournal.data.local.dao

import android.app.Application
import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.factory.innerlogaimoodjournal.data.local.AppDatabase
import com.factory.innerlogaimoodjournal.data.local.entity.GoalEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
@Config(application = Application::class)
class GoalDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var dao: GoalDao

    private fun goal(
        title: String,
        completed: Boolean = false,
        deadline: LocalDate? = null
    ) = GoalEntity(
        title = title,
        description = "desc",
        targetValue = 10,
        currentValue = 0,
        unit = "times",
        deadline = deadline,
        isCompleted = completed,
        createdAt = LocalDate.now()
    )

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = database.goalDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `insert then getAllGoals returns the persisted goal`() = runTest {
        dao.insert(goal("Run a 5k"))

        val goals = dao.getAllGoals().first()

        assertEquals(1, goals.size)
        assertEquals("Run a 5k", goals.first().title)
    }

    @Test
    fun `getAllGoals orders incomplete goals before completed ones`() = runTest {
        dao.insert(goal("Done", completed = true, deadline = LocalDate.now()))
        dao.insert(goal("Pending", completed = false, deadline = LocalDate.now().plusDays(1)))

        val goals = dao.getAllGoals().first()

        assertEquals("Pending", goals.first().title)
        assertEquals("Done", goals.last().title)
    }

    @Test
    fun `update modifies the persisted goal`() = runTest {
        val id = dao.insert(goal("Run a 5k"))
        val updated = goal("Run a 5k").copy(id = id, currentValue = 5)

        dao.update(updated)

        assertEquals(5, dao.getAllGoals().first().first().currentValue)
    }

    @Test
    fun `delete removes the goal`() = runTest {
        val id = dao.insert(goal("Run a 5k"))

        dao.delete(goal("Run a 5k").copy(id = id))

        assertEquals(0, dao.getAllGoals().first().size)
    }
}
