package com.udacity.project4.locationreminders.savereminder

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.R
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.getOrAwaitValue
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.junit.After
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.annotation.Config

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.O_MR1])
class SaveReminderViewModelTest {


    //TODO: provide testing to the SaveReminderView and its live data objects (DONE)

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @After
    fun tearDown() {
        stopKoin()
    }


    @Test
    fun shouldSuccessfulValidateEnteredData() {
        val dataSource = FakeDataSource()
        dataSource.reminderList = ArrayList()
        val saveReminderViewModel = SaveReminderViewModel(ApplicationProvider.getApplicationContext(), dataSource)

        val reminder = ReminderDataItem("Reminder1", "This is reminder1", "Googleplex", 123.3, 123.4)
        assertThat(saveReminderViewModel.validateEnteredData(reminder), Matchers.`is`(true))
    }


    @Test
    fun shouldFailValidateEnteredData() {
        val dataSource = FakeDataSource()
        dataSource.reminderList = ArrayList()
        val saveReminderViewModel = SaveReminderViewModel(ApplicationProvider.getApplicationContext(), dataSource)

        val reminder = ReminderDataItem("", "This is reminder1", "Googleplex", 123.3, 123.4)
        assertThat(saveReminderViewModel.validateEnteredData(reminder), Matchers.`is`(false))
        assertThat(saveReminderViewModel.showSnackBarInt.getOrAwaitValue(), Matchers.`is`(R.string.err_enter_title))

        reminder.title = "Reminder1"
        reminder.location = ""
        assertThat(saveReminderViewModel.validateEnteredData(reminder), Matchers.`is`(false))
        assertThat(saveReminderViewModel.showSnackBarInt.getOrAwaitValue(), Matchers.`is`(R.string.err_select_location))
    }

    @Test
    fun shouldSaveReminderSuccessful() {
        runBlockingTest {
            val dataSource = FakeDataSource()
            dataSource.reminderList = ArrayList()
            val saveReminderViewModel = SaveReminderViewModel(ApplicationProvider.getApplicationContext(), dataSource)

            val newReminder = ReminderDataItem("Reminder1", "This is reminder1", "Googleplex", 123.3, 123.4)
            val newId = newReminder.id
            saveReminderViewModel.saveReminder(newReminder)

            val result = dataSource.getReminder(newId)
            result.let { res ->
                if (res is Result.Success) {
                    val data = res.data
                    assertThat(data.title, Matchers.`is`(newReminder.title))
                    assertThat(data.description, Matchers.`is`(newReminder.description))
                    assertThat(data.location, Matchers.`is`(newReminder.location))
                    assertThat(data.latitude, Matchers.`is`(newReminder.latitude))
                    assertThat(data.longitude, Matchers.`is`(newReminder.longitude))
                } else {
                    Assert.fail("Could not get the saved reminder!")
                }
            }
        }
    }

    @Test
    fun shouldSaveReminderWithError() {
        runBlockingTest {
            val dataSource = FakeDataSource()
            dataSource.setShouldFail()
            dataSource.reminderList = ArrayList()
            val saveReminderViewModel = SaveReminderViewModel(ApplicationProvider.getApplicationContext(), dataSource)

            val newReminder = ReminderDataItem("Reminder1", "This is reminder1", "Googleplex", 123.3, 123.4)
            val newId = newReminder.id
            saveReminderViewModel.saveReminder(newReminder)
            val result = dataSource.getReminder(newId)
            result.let { res ->
                if (res is Result.Error) {
                    assertThat(res.message, Matchers.`is`("Reminder not found: id=${newId}"))
                } else {
                    Assert.fail("Error while saving reminder not recognized!")
                }
            }
        }
    }

}