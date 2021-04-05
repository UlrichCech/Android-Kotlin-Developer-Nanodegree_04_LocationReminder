package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import java.util.ArrayList

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource : ReminderDataSource {

//    TODO: Create a fake data source to act as a double to the real data source (DONE)

    var reminderList : MutableList<ReminderDTO> = ArrayList()
    private var shouldResultInError = false


    fun setShouldFail() {
        shouldResultInError = true
    }

    fun setShouldSucceed() {
        shouldResultInError = false
    }


    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        return if (shouldResultInError) {
            Result.Error("Could not fetch the reminders.")
        } else {
            Result.Success(reminderList)
        }
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        reminderList.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        if (shouldResultInError) {
            return Result.Error("Reminder not found: id=${id}")
        } else {
            for (reminder in reminderList) {
                if (reminder.id == id) {
                    return Result.Success(reminder)
                }
            }
            return Result.Error("Reminder not found: id=${id}")
        }
    }

    override suspend fun deleteAllReminders() {
        reminderList.clear()
    }

}