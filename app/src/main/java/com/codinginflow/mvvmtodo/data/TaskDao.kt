package com.codinginflow.mvvmtodo.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    @Query("SELECT * FROM task_table WHERE name LIKE '%' || :searchQuery || '%' ORDER BY important DESC")
    fun getAllTask(searchQuery: String): Flow<List<Task>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task)

    @Delete
    suspend fun deleteTask(task: Task)

    @Update
    suspend fun updateTask(task: Task)

}