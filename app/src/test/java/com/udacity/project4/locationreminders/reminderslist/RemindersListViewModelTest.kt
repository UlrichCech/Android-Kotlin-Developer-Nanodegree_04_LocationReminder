package com.udacity.project4.locationreminders.reminderslist

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.annotation.Config
import kotlin.collections.ArrayList

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
@Config(sdk = [Build.VERSION_CODES.O_MR1])
class RemindersListViewModelTest {

    //TODO: provide testing to the RemindersListViewModel and its live data objects


    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun shouldSuccessfulLoadReminders() {
        val dataSource = FakeDataSource()
        dataSource.reminderList = createResultList()

        val remindersListViewModel = RemindersListViewModel(ApplicationProvider.getApplicationContext(), dataSource)
        remindersListViewModel.loadReminders()
        val value = remindersListViewModel.remindersList.getOrAwaitValue()

        assertThat(value.size, Matchers.`is`(2))
    }

    private fun createResultList(): MutableList<ReminderDTO> {
        val rm1 = ReminderDTO("Reminder1", "This is reminder1", "Googleplex", 123.3, 123.4)
        val rm2 = ReminderDTO("Reminder2", "This is reminder2", "Sunnydale", 23.3, 23.4)
        return mutableListOf(rm1, rm2)
    }

    @Test
    fun shouldFailedLoadReminders() {
        val dataSource = FakeDataSource()
        dataSource.reminderList = createResultList()
        dataSource.setShouldFail()

        val remindersListViewModel = RemindersListViewModel(ApplicationProvider.getApplicationContext(), dataSource)
        remindersListViewModel.loadReminders()

        assertThat(remindersListViewModel.showSnackBar.getOrAwaitValue(), Matchers.`is`("Could not fetch the reminders."))
    }


    @Test
    fun shouldDisplayNoDataIcon() {
        val dataSource = FakeDataSource()
        dataSource.reminderList = ArrayList()

        val remindersListViewModel = RemindersListViewModel(ApplicationProvider.getApplicationContext(), dataSource)
        remindersListViewModel.loadReminders()
        val value = remindersListViewModel.remindersList.getOrAwaitValue()

        assertThat(value.size, Matchers.`is`(0))
        assertThat(remindersListViewModel.isReminderListEmpty(), Matchers.`is`(true))
        assertThat(remindersListViewModel.showNoData.getOrAwaitValue(), Matchers.`is`(true))
    }


    @Test
    fun shouldDisplayLoadingState() {
        mainCoroutineRule.pauseDispatcher()
        val dataSource = FakeDataSource()
        val remindersListViewModel = RemindersListViewModel(ApplicationProvider.getApplicationContext(), dataSource)
        remindersListViewModel.loadReminders()
        assertThat(remindersListViewModel.showLoading.getOrAwaitValue(), Matchers.`is`(true))
        mainCoroutineRule.resumeDispatcher()
        assertThat(remindersListViewModel.showLoading.getOrAwaitValue(), Matchers.`is`(false))
    }

}