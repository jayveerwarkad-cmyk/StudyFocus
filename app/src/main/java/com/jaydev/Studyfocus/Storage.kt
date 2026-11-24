package com.jaydev.studyfocus

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

val Context.dataStore by preferencesDataStore(name = "study_focus_prefs")

class Storage(private val context: Context) {

    private objectPreferenceKeys {
        val IS_ONBOARDED = booleanPreferencesKey("is_onboarded")
        val STUDY_SESSION_DURATION = longPreferencesKey("study_session_duration")
        val BREAK_DURATION = longPreferencesKey("break_duration")
        val REMINDERS_ENABLED = booleanPreferencesKey("reminders_enabled")
        val REMINDER_TIME_HOUR = intPreferencesKey("reminder_time_hour")
        val REMINDER_TIME_MINUTE = intPreferencesKey("reminder_time_minute")
        val STREAK_COUNT = intPreferencesKey("streak_count")
        val LAST_STUDY_DATE = stringPreferencesKey("last_study_date")
    }

    val isOnBoarded: Flow<Boolean> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }.map { preferences ->
            preferences[PreferenceKeys.IS_ONBOARDED] ?: false
        }

    suspend fun setOnBoarded(isBoarded: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferenceKeys.IS_ONBOARDED] = isBoarded
        }
    }

    val studySessionDuration: Flow<Long> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }.map { preferences ->
            preferences[PreferenceKeys.STUDY_SESSION_DURATION] ?: 25L * 60 * 1000 // Default 25 mins
        }

    suspend fun setStudySessionDuration(durationInMillis: Long) {
        context.dataStore.edit { preferences ->
            preferences[PreferenceKeys.STUDY_SESSION_DURATION] = durationInMillis
        }
    }

    val breakDuration: Flow<Long> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }.map { preferences ->
            preferences[PreferenceKeys.BREAK_DURATION] ?: 5L * 60 * 1000 // Default 5 mins
        }

    suspend fun setBreakDuration(durationInMillis: Long) {
        context.dataStore.edit { preferences ->
            preferences[PreferenceKeys.BREAK_DURATION] = durationInMillis
        }
    }

    val remindersEnabled: Flow<Boolean> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }.map { preferences ->
            preferences[PreferenceKeys.REMINDERS_ENABLED] ?: true
        }

    suspend fun setRemindersEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferenceKeys.REMINDERS_ENABLED] = enabled
        }
    }

    val reminderTime: Flow<Pair<Int, Int>> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }.map { preferences ->
            val hour = preferences[PreferenceKeys.REMINDER_TIME_HOUR] ?: 10 // Default 10 AM
            val minute = preferences[PreferenceKeys.REMINDER_TIME_MINUTE] ?: 0
            Pair(hour, minute)
        }

    suspend fun setReminderTime(hour: Int, minute: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferenceKeys.REMINDER_TIME_HOUR] = hour
            preferences[PreferenceKeys.REMINDER_TIME_MINUTE] = minute
        }
    }

    val streakCount: Flow<Int> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }.map { preferences ->
            preferences[PreferenceKeys.STREAK_COUNT] ?: 0
        }

    suspend fun setStreakCount(count: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferenceKeys.STREAK_COUNT] = count
        }
    }

    val lastStudyDate: Flow<String?> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }.map { preferences ->
            preferences[PreferenceKeys.LAST_STUDY_DATE]
        }

    suspend fun setLastStudyDate(date: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferenceKeys.LAST_STUDY_DATE] = date
        }
    }
}
