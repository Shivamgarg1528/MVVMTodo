package com.codinginflow.mvvmtodo.data

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.createDataStore
import androidx.datastore.preferences.edit
import androidx.datastore.preferences.emptyPreferences
import androidx.datastore.preferences.preferencesKey
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject

enum class SORT { BY_NAME, BY_DATE }

data class FilterPreferences(val sortOrder: SORT, val hideCompleted: Boolean)

class PreferencesManager @Inject constructor(@ApplicationContext context: Context) {

    private val dataStore = context.createDataStore("app_preference")

    val preferencesFlow = dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    Log.e(LOG_TAG, "Error reading preferences", exception)
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                val sortOrder = SORT.valueOf(preferences[KEY_SORT_ORDER] ?: SORT.BY_NAME.name)
                val hideCompleted = preferences[KEY_HIDE_COMPLETED] ?: false
                return@map FilterPreferences(sortOrder = sortOrder, hideCompleted = hideCompleted)
            }

    suspend fun updateSortOrder(sortOrder: SORT) {
        dataStore.edit {
            it[KEY_SORT_ORDER] = sortOrder.name
        }
    }

    suspend fun updateHideCompleted(hideCompleted: Boolean) {
        dataStore.edit {
            it[KEY_HIDE_COMPLETED] = hideCompleted
        }
    }

    companion object {
        private const val LOG_TAG = "PreferencesManager"
        private val KEY_SORT_ORDER = preferencesKey<String>("sort_order")
        private val KEY_HIDE_COMPLETED = preferencesKey<Boolean>("hide_completed")
    }
}