package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO

import org.junit.Before
import org.junit.Rule
import org.junit.runner.RunWith

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Test

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {

//    TODO: Add testing implementation to the RemindersDao.kt (DONE)

    private lateinit var database: RemindersDatabase

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun initDb() {
        // using an in-memory database because the information stored here disappears when the
        // process is killed
        database = Room.inMemoryDatabaseBuilder(
                ApplicationProvider.getApplicationContext(),
                RemindersDatabase::class.java
        ).build()
    }

    @After
    fun closeDb() = database.close()


    @Test
    fun insertNewReminderAndGetById() = runBlockingTest {
        // GIVEN - insert a task
        val newReminder = ReminderDTO("title", "description", "Googleplex", 123.2, 123.4)
        database.reminderDao().saveReminder(newReminder)

        // WHEN - Get the task by id from the database
        val loadedReminder = database.reminderDao().getReminderById(newReminder.id)

        // THEN - The loaded data contains the expected values
        assertThat(loadedReminder as ReminderDTO, notNullValue())
        assertThat(loadedReminder.id, `is`(newReminder.id))
        assertThat(loadedReminder.title, `is`(newReminder.title))
        assertThat(loadedReminder.description, `is`(newReminder.description))
        assertThat(loadedReminder.location, `is`(newReminder.location))
        assertThat(loadedReminder.latitude, `is`(newReminder.latitude))
        assertThat(loadedReminder.longitude, `is`(newReminder.longitude))
    }


}