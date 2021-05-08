package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.*
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {

//    TODO: Add testing implementation to the RemindersLocalRepository.kt (DONE)

    private lateinit var remindersLocalRepository: RemindersLocalRepository
    private lateinit var database: RemindersDatabase

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setup() {
        // using an in-memory database for testing, since it doesn't survive killing the process
        database = Room.inMemoryDatabaseBuilder(
                ApplicationProvider.getApplicationContext(),
                RemindersDatabase::class.java
        )
                .allowMainThreadQueries()
                .build()
        remindersLocalRepository =
                RemindersLocalRepository(
                        database.reminderDao(),
                        Dispatchers.Main
                )
    }

    @After
    fun cleanUp() {
        database.close()
    }


    @Test
    fun shouldSaveReminderAndLoadReminders() = runBlocking {
        val newReminder = ReminderDTO("title", "description", "Googleplex", 123.3, 123.4)
        remindersLocalRepository.saveReminder(newReminder)

        // getReminders()
        val reminders = remindersLocalRepository.getReminders()
        assertThat(reminders is Result.Success, `is`(true))
        reminders as Result.Success<List<ReminderDTO>>
        assertThat(reminders.data.size, `is`(1))

        // getReminder()
        val result = remindersLocalRepository.getReminder(newReminder.id)
        assertThat(result is Result.Success, `is`(true))
        result as Result.Success
        assertThat(result.data.title, `is`(newReminder.title))
        assertThat(result.data.description, `is`(newReminder.description))
        assertThat(result.data.location, `is`(newReminder.location))
        assertThat(result.data.latitude, `is`(newReminder.latitude))
        assertThat(result.data.longitude, `is`(newReminder.longitude))

        // delete reminders
        remindersLocalRepository.deleteAllReminders()
        val remindersAfterDeletion = remindersLocalRepository.getReminders()
        assertThat(remindersAfterDeletion is Result.Success, `is`(true))
        remindersAfterDeletion as Result.Success<List<ReminderDTO>>
        assertThat(remindersAfterDeletion.data.size, `is`(0))
    }


    @Test
    fun shouldFailGettingReminderAfterDeletion() = runBlocking {
        val newReminder = ReminderDTO("title", "description", "Googleplex", 123.3, 123.4)
        remindersLocalRepository.saveReminder(newReminder)

        // getReminders() to be sure, that saveReminder was absolute successful
        val reminders = remindersLocalRepository.getReminders()
        assertThat(reminders is Result.Success, `is`(true))
        reminders as Result.Success<List<ReminderDTO>>
        assertThat(reminders.data.size, `is`(1))

        remindersLocalRepository.deleteAllReminders()

        // getReminder() should fail
        val result = remindersLocalRepository.getReminder(newReminder.id)
        assertThat(result is Result.Error, `is`(true))
        result as Result.Error
        assertThat(result.message, `is`("Reminder not found!"))
    }

}