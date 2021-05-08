package com.udacity.project4

import android.app.Application
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
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

@RunWith(AndroidJUnit4::class)
@LargeTest
@ExperimentalCoroutinesApi
//END TO END test to black box test the app
class RemindersActivityTest :
    AutoCloseKoinTest() {// Extended Koin Test - embed autoclose @after method to close Koin after every test

    private lateinit var repository: ReminderDataSource
    private lateinit var appContext: Application

    /**
     * As we use Koin as a Service Locator Library to develop our code, we'll also use Koin to test our code.
     * at this step we will initialize Koin related code to be able to use it in out testing.
     */
    @Before
    fun init() {
        stopKoin()
        appContext = getApplicationContext()
        val myModule = module {
            viewModel { RemindersListViewModel(appContext, get() as ReminderDataSource) }
            single {
                SaveReminderViewModel(appContext, get() as ReminderDataSource)
            }
            single {
                RemindersLocalRepository(get()) as ReminderDataSource
            }
            single {
                LocalDB.createRemindersDao(appContext)
            }
        }
        startKoin {
            androidContext(appContext)
            modules(listOf(myModule))
        }
        repository = get()
        runBlocking {
            repository.deleteAllReminders()
        }
    }


//    TODO: add End to End testing to the app (DONE)
    @Test
    fun shouldNavigateAndDisplayToAddRemindersFragment() {
        val reminder = ReminderDTO("End-To-End-Title", "newDescription", "Sunnyvale", 12.3, 12.3)
        runBlocking {
            val scenario = ActivityScenario.launch(RemindersActivity::class.java)

            // 1. click on FAB for creating a new reminder
            onView(withId(R.id.addReminderFAB))
                .check(matches(isDisplayed()))
            onView(withId(R.id.addReminderFAB)).perform(click())

            // 2. Check, that the fragment is correctly displayed with the text fields
            onView(withId(R.id.reminderTitle))
                .check(matches(isDisplayed()))
            onView(withId(R.id.reminderDescription))
                .check(matches(isDisplayed()))
            onView(withId(R.id.saveReminder))
                .check(matches(isDisplayed()))

            // 3. input some value to the text fields
            onView(withId(R.id.reminderTitle))
                .perform(typeText(reminder.title))
            onView(withId(R.id.reminderDescription)).perform(
                typeText(reminder.description)
            )
            Espresso.closeSoftKeyboard()

            // 4. open the map to select a location
            onView(withId(R.id.selectLocation)).perform(click())

            // 5. selecting a location
            onView(withId(R.id.locationMap)).check(matches(isDisplayed()))
            onView(withId(R.id.locationMap)).perform(longClick())

            // 6. save the location
            onView(withId(R.id.saveLocationBtn)).perform(click())

            // 7. save the reminder
            onView(withId(R.id.saveReminder)).perform(click())

            // 8. check if the reminder is displayed in the list
            onView(withText("End-To-End-Title"))
                .check(matches(isDisplayed()))
        }
    }

}
