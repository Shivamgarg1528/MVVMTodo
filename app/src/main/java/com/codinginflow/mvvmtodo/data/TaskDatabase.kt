package com.codinginflow.mvvmtodo.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.codinginflow.mvvmtodo.di.ApplicationCoroutine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Provider

@Database(entities = [Task::class], version = 1, exportSchema = true)
abstract class TaskDatabase : RoomDatabase() {

    abstract fun getTaskDao(): TaskDao

    class Callback @Inject constructor(
            @ApplicationCoroutine private val scope: CoroutineScope,
            private val provider: Provider<TaskDatabase>
    ) : RoomDatabase.Callback() {

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            scope.launch {
                provider.get().getTaskDao().apply {
                    insertTask(Task("Wash the dishes"))
                    insertTask(Task("Do the laundry"))
                    insertTask(Task("Buy groceries", important = true))
                    insertTask(Task("Prepare food", completed = true))
                    insertTask(Task("Call mom"))
                    insertTask(Task("Visit grandma", completed = true))
                    insertTask(Task("Repair my bike"))
                    insertTask(Task("Call Elon Musk"))
                }
            }
        }
    }
}