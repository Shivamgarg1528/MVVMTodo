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
                    insertTask(Task("Task-1"))
                    insertTask(Task("Task-2"))
                    insertTask(Task("Task-3"))
                    insertTask(Task("Task-4"))
                    insertTask(Task("Task-5"))
                    insertTask(Task("Task-6"))
                    insertTask(Task("Task-7"))
                    insertTask(Task("Task-8"))
                    insertTask(Task("Task-9"))
                    insertTask(Task("Task-10"))
                }
            }
        }
    }
}