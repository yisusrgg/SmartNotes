package com.example.smartnotes.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object TaskRepository {
    private val _tasks = MutableStateFlow<List<Task>>(sampleTasks())
    val tasks: StateFlow<List<Task>> = _tasks

    private fun sampleTasks() = listOf(
        Task(title = "Completar informe mensual", dateTimeText = "Ayer, 3 PM", hasAttachment = true),
        Task(title = "Presentación cliente nuevo", dateTimeText = "Hoy, 10 AM", hasAttachment = true),
        Task(title = "Comprar suministros oficina", dateTimeText = "Mañana, 9 PM"),
        Task(title = "Investigar nuevas tecnologías", dateTimeText = "20/10/2023, 2", hasAudio = true)
    )

    fun addTask(task: Task) {
        _tasks.value = _tasks.value + task
    }

    fun updateTask(task: Task) {
        _tasks.value = _tasks.value.map { if (it.id == task.id) task else it }
    }

    fun removeTask(taskId: String) {
        _tasks.value = _tasks.value.filterNot { it.id == taskId }
    }
}
