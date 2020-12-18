package com.codinginflow.mvvmtodo.di

import android.content.Context
import androidx.room.Room
import com.codinginflow.mvvmtodo.data.TaskDao
import com.codinginflow.mvvmtodo.data.TaskDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import javax.inject.Qualifier

@InstallIn(ApplicationComponent::class)
@Module
object AppModule {

    @Provides
    @JvmStatic
    fun provideDatabase(
        @ApplicationContext context: Context,
        callback: TaskDatabase.Callback
    ): TaskDatabase {
        return Room.databaseBuilder(context, TaskDatabase::class.java, "task_db")
            .fallbackToDestructiveMigration()
            .addCallback(callback)
            .build()
    }

    @Provides
    @JvmStatic
    fun provideTaskDao(taskDataBase: TaskDatabase): TaskDao {
        return taskDataBase.getTaskDao()
    }

    @Provides
    @ApplicationCoroutine
    fun provideCoroutineScope(): CoroutineScope = CoroutineScope(SupervisorJob())
}

@Retention(value = AnnotationRetention.RUNTIME)
@Qualifier
annotation class ApplicationCoroutine