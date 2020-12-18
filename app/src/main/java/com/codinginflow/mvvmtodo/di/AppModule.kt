package com.codinginflow.mvvmtodo.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.codinginflow.mvvmtodo.data.Task
import com.codinginflow.mvvmtodo.data.TaskDao
import com.codinginflow.mvvmtodo.data.TaskDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Provider

@InstallIn(ApplicationComponent::class)
@Module
object AppModule {

    @Provides
    @JvmStatic
    fun provideDatabase(
            @ApplicationContext context: Context,
            @CoroutineScopeAnnotation scope: CoroutineScope,
            provider: Provider<TaskDatabase>
    ): TaskDatabase {
        return Room.databaseBuilder(context, TaskDatabase::class.java, "task_db")
                .fallbackToDestructiveMigration()
                .addCallback(object : RoomDatabase.Callback() {

                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        val taskDao = provider.get().getTaskDao()
                        scope.launch {
                            taskDao.insertTask(Task("Task 1"))
                            taskDao.insertTask(Task("Task 2"))
                            taskDao.insertTask(Task("Task 3"))
                            taskDao.insertTask(Task("Task 4"))
                            taskDao.insertTask(Task("Task 5"))
                        }
                    }
                })
                .build()
    }

    @Provides
    @JvmStatic
    fun provideTaskDao(taskDataBase: TaskDatabase): TaskDao {
        return taskDataBase.getTaskDao()
    }

    @Provides
    @CoroutineScopeAnnotation
    fun provideCoroutineScope(): CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
}

@Retention(value = AnnotationRetention.SOURCE)
annotation class CoroutineScopeAnnotation