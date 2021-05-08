package com.udacity.project4.locationreminders.reminderslist

import android.os.Bundle
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.R
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@MediumTest
class ReminderListFragmentTest : AutoCloseKoinTest() {

//    TODO: test the navigation of the fragments. (DONE)
//    TODO: test the displayed data on the UI. (DONE)
//    TODO: add testing for the error messages. (DONE)

    private lateinit var testReminderRepository: ReminderDataSource
    // An Idling Resource that waits for Data Binding to have no pending bindings
//    private val dataBindingIdlingResource = DataBindingIdlingResource()


    @Before
    fun setup() {
        stopKoin()
        val myModule = module {
            viewModel { RemindersListViewModel(getApplicationContext(), get() as ReminderDataSource) }
            single {
                SaveReminderViewModel(getApplicationContext(), get() as ReminderDataSource)
            }
            single {
                RemindersLocalRepository(get()) as ReminderDataSource
            }
            single {
                LocalDB.createRemindersDao(getApplicationContext())
            }
        }
        startKoin {
            androidContext(getApplicationContext())
            modules(listOf(myModule))
        }
        testReminderRepository = get()
        runBlocking { testReminderRepository.deleteAllReminders() }
    }


    @After
    fun cleanUpData() = runBlocking {
        stopKoin()
    }


    @Test
    fun shouldDisplayDataInListView() {
        val reminder = ReminderDTO("newTitle", "newDescription", "Sunnyvale", 12.3, 12.3)
        runBlocking {
            // GIVEN
            testReminderRepository.saveReminder(reminder)
            launchFragmentInContainer<ReminderListFragment>(Bundle.EMPTY, R.style.AppTheme)

            // THEN
            onView(withText(reminder.title)).check(ViewAssertions.matches(isDisplayed()))
            onView(withText(reminder.description)).check(ViewAssertions.matches(isDisplayed()))
            onView(withText(reminder.location)).check(ViewAssertions.matches(isDisplayed()))
        }
    }


    @Test
    fun clickFabAndShouldNavigateToSaveReminderFragment() {
        // GIVEN
        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle.EMPTY, R.style.AppTheme)
        val navController = mock(NavController::class.java)
        scenario.onFragment {
            it._viewModel.deleteAllReminders()
            Navigation.setViewNavController(it.view!!, navController)
        }
        // WHEN
        onView(withId(R.id.addReminderFAB)).perform(click())

        // THEN
        verify(navController).navigate(
            ReminderListFragmentDirections.toSaveReminder()
        )
    }


    @Test
    fun shouldNavigateAndDisplayToAddRemindersFragmentAndDisplayErrorMessage() {
        val scenario = ActivityScenario.launch(RemindersActivity::class.java)

        onView(withId(R.id.addReminderFAB)).check(ViewAssertions.matches(isDisplayed()))
        onView(withId(R.id.addReminderFAB)).perform(click())

        onView(withId(R.id.reminderTitle)).check(ViewAssertions.matches(isDisplayed()))
        onView(withId(R.id.reminderDescription)).check(ViewAssertions.matches(isDisplayed()))
        onView(withId(R.id.saveReminder)).check(ViewAssertions.matches(isDisplayed()))

        onView(withId(R.id.saveReminder)).perform(click())
        onView(withId(com.google.android.material.R.id.snackbar_text))
            .check(ViewAssertions.matches(withText(R.string.err_enter_title)))
    }

}