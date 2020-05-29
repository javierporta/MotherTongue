package com.ipleiria.mothertongue.google_geofencing

import android.content.Context
import com.google.gson.Gson

class ReminderRepository(private val context: Context) {

  companion object {
    private const val PREFS_NAME = "ReminderRepository"
    private const val REMINDERS = "REMINDERS"
  }

  private val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
  private val gson = Gson()

  fun add(reminder: Reminder,
          success: () -> Unit,
          failure: (error: String) -> Unit) {
    val list = getAll() + reminder
    saveAll(list)
    success()
  }

  fun remove(reminder: Reminder,
             success: () -> Unit,
             failure: (error: String) -> Unit) {
    val list = getAll() - reminder
    saveAll(list)
    success()
  }

  private fun saveAll(list: List<Reminder>) {
    preferences
        .edit()
        .putString(REMINDERS, gson.toJson(list))
        .apply()
  }

  fun getAll(): List<Reminder> {
    if (preferences.contains(REMINDERS)) {
      val remindersString = preferences.getString(REMINDERS, null)
      val arrayOfReminders = gson.fromJson(remindersString,
          Array<Reminder>::class.java)
      if (arrayOfReminders != null) {
        return arrayOfReminders.toList()
      }
    }
    return listOf()
  }

  fun get(requestId: String?) = getAll().firstOrNull { it.id == requestId }

  fun getLast() = getAll().lastOrNull()
}