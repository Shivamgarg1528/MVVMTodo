package com.codinginflow.mvvmtodo.vm

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.codinginflow.mvvmtodo.data.PreferencesManager
import com.codinginflow.mvvmtodo.data.SORT
import com.codinginflow.mvvmtodo.data.Task
import com.codinginflow.mvvmtodo.data.TaskDao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

class TaskListViewModel @ViewModelInject constructor(
        private val taskDao: TaskDao,
        @Assisted private val savedStateHandle: SavedStateHandle,
        private val preferencesManager: PreferencesManager
) : ViewModel() {

    var mQueryAsFlow = MutableStateFlow("")
    val preferencesFlow = preferencesManager.preferencesFlow

    private val mTaskStateFlow = combine(mQueryAsFlow, preferencesFlow) { query, preferencesFlow ->
        Pair(query, preferencesFlow)
    }.flatMapLatest { (query, filterPreferences) ->
        taskDao.getTasks(query, filterPreferences.sortOrder, filterPreferences.hideCompleted)
    }

    val mTask: LiveData<List<Task>> = mTaskStateFlow.asLiveData()

    fun addTask(task: Task) = viewModelScope.launch {
        taskDao.insertTask(task)
    }

    fun onSortOrderSelected(sort: SORT) = viewModelScope.launch {
        preferencesManager.updateSortOrder(sort)
    }

    fun onHideCompletedSelected(hideCompleted: Boolean) = viewModelScope.launch {
        preferencesManager.updateHideCompleted(hideCompleted)
    }

    fun onCheckBoxClick(task: Task, completed: Boolean) = viewModelScope.launch {
        taskDao.updateTask(task.copy(completed = completed))
    }

    fun onItemClick(task: Task) {

    }
}