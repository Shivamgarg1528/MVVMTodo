package com.codinginflow.mvvmtodo.vm

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codinginflow.mvvmtodo.data.Task
import com.codinginflow.mvvmtodo.data.TaskDao
import com.codinginflow.mvvmtodo.di.ApplicationCoroutine
import com.codinginflow.mvvmtodo.ui.MainActivity.Companion.ADD_TASK_RESULT_OK
import com.codinginflow.mvvmtodo.ui.MainActivity.Companion.EDIT_TASK_RESULT_OK
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class AddEditViewModel @ViewModelInject constructor(
    private val taskDao: TaskDao,
    @Assisted private val savedStateHandle: SavedStateHandle,
    @ApplicationCoroutine private val appScope: CoroutineScope
) : ViewModel() {

    private val _addEditTaskEventChannel = Channel<AddEditTaskEvents>()
    val mAddTaskEventFlow = _addEditTaskEventChannel.receiveAsFlow()

    val task = savedStateHandle.get<Task>("task")

    var taskName = savedStateHandle.get<String>("taskName") ?: task?.name ?: ""
        set(value) {
            field = value
            savedStateHandle.set("taskName", value)
        }

    var taskImportance = savedStateHandle.get<Boolean>("taskImportance") ?: task?.important ?: false
        set(value) {
            field = value
            savedStateHandle.set("taskImportance", value)
        }

    fun onSaveClick() = viewModelScope.launch {
        if (taskName.isBlank()) {
            _addEditTaskEventChannel.send(AddEditTaskEvents.TaskNameEmpty)
            return@launch
        }
        if (task != null) {
            val updatedTask = task.copy(name = taskName, important = taskImportance)
            taskDao.updateTask(updatedTask)
            _addEditTaskEventChannel.send(
                AddEditTaskEvents.NavigateBackWithResult(EDIT_TASK_RESULT_OK)
            )
        } else {
            val newTask = Task(name = taskName, important = taskImportance)
            taskDao.insertTask(newTask)
            _addEditTaskEventChannel.send(
                AddEditTaskEvents.NavigateBackWithResult(ADD_TASK_RESULT_OK)
            )
        }
    }

    sealed class AddEditTaskEvents {
        object TaskNameEmpty : AddEditTaskEvents()
        data class NavigateBackWithResult(val resultCode: Int) : AddEditTaskEvents()
    }
}