package com.codinginflow.mvvmtodo.vm

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.codinginflow.mvvmtodo.data.Task
import com.codinginflow.mvvmtodo.data.TaskDao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

class TaskListViewModel @ViewModelInject constructor(
    private val taskDao: TaskDao,
    @Assisted private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    var mQueryAsFlow = MutableStateFlow("")

    private val mTaskStateFlow = mQueryAsFlow.flatMapLatest {
        taskDao.getAllTask(it)
    }

    val mTask: LiveData<List<Task>> = mTaskStateFlow.asLiveData()

    fun addTask(task: Task) = viewModelScope.launch {
        taskDao.insertTask(task)
    }
}