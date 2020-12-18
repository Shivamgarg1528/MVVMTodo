package com.codinginflow.mvvmtodo.vm

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.codinginflow.mvvmtodo.data.Task
import com.codinginflow.mvvmtodo.data.TaskDao

class TaskListViewModel @ViewModelInject constructor(taskDao: TaskDao) : ViewModel() {

    val _tasks = taskDao.getAllTask()
    val mTask: LiveData<List<Task>> = _tasks

}