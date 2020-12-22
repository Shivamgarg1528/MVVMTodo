package com.codinginflow.mvvmtodo.vm

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.codinginflow.mvvmtodo.data.PreferencesManager
import com.codinginflow.mvvmtodo.data.SORT
import com.codinginflow.mvvmtodo.data.Task
import com.codinginflow.mvvmtodo.data.TaskDao
import com.codinginflow.mvvmtodo.ui.MainActivity.Companion.ADD_TASK_RESULT_OK
import com.codinginflow.mvvmtodo.ui.MainActivity.Companion.EDIT_TASK_RESULT_OK
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class TaskListViewModel @ViewModelInject constructor(
    private val taskDao: TaskDao,
    @Assisted private val savedStateHandle: SavedStateHandle,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val mQueryAsFlow = MutableStateFlow("")
    val preferencesFlow = preferencesManager.preferencesFlow

    private val _taskEventChannel = Channel<TasksEvent>()
    val mTaskEventsFlow = _taskEventChannel.receiveAsFlow()

    private val mTaskStateFlow = combine(mQueryAsFlow, preferencesFlow) { query, preferencesFlow ->
        Pair(query, preferencesFlow)
    }.flatMapLatest { (query, filterPreferences) ->
        taskDao.getTasks(query, filterPreferences.sortOrder, filterPreferences.hideCompleted)
    }

    val mTasks: LiveData<List<Task>> = mTaskStateFlow.asLiveData()

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

    fun onItemClick(task: Task) = viewModelScope.launch {
        _taskEventChannel.send(TasksEvent.NavigateToEditTaskScreen(task))
    }

    fun onTaskSwiped(task: Task) = viewModelScope.launch {
        taskDao.deleteTask(task)
        _taskEventChannel.send(TasksEvent.OnTaskDeletedEvent(task))
    }

    fun onUndoDeleteClick(task: Task) = viewModelScope.launch {
        taskDao.insertTask(task)
    }

    fun onQueryTextChanged(query: String) = query.also { mQueryAsFlow.value = it }

    fun addTaskButtonClicked() = viewModelScope.launch {
        _taskEventChannel.send(TasksEvent.NavigateToAddTaskScreen)
    }

    fun onAddEditResult(result: Int) = viewModelScope.launch {
        when (result) {
            ADD_TASK_RESULT_OK -> _taskEventChannel.send(
                TasksEvent.ShowTaskSavedConfirmationMessage("Task added")
            )
            EDIT_TASK_RESULT_OK -> _taskEventChannel.send(
                TasksEvent.ShowTaskSavedConfirmationMessage("Task updated")
            )
        }
    }

    fun onDeleteAllCompletedClick() = viewModelScope.launch {
        _taskEventChannel.send(TasksEvent.NavigateToDeleteAllComplete)
    }

    sealed class TasksEvent {
        object NavigateToDeleteAllComplete : TasksEvent()
        object NavigateToAddTaskScreen : TasksEvent()
        data class NavigateToEditTaskScreen(val task: Task) : TasksEvent()
        data class OnTaskDeletedEvent(val task: Task) : TasksEvent()
        data class ShowTaskSavedConfirmationMessage(val msg: String) : TasksEvent()
    }
}