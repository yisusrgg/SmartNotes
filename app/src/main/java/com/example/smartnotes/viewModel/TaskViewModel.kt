package com.example.smartnotes.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartnotes.data.Task
import com.example.smartnotes.data.TaskRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TaskViewModel: ViewModel() {
    val tasks: StateFlow<List<Task>> = TaskRepository.tasks
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun addTask(title: String, dateTimeText: String) {
        viewModelScope.launch {
            TaskRepository.addTask(Task(title = title, dateTimeText = dateTimeText))
        }
    }

    fun updateTask(task: Task) {
        viewModelScope.launch {
            TaskRepository.updateTask(task)
        }
    }

    fun removeTask(id: String) {
        viewModelScope.launch {
            TaskRepository.removeTask(id)
        }
    }
}
