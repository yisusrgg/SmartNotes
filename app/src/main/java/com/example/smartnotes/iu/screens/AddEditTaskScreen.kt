package com.example.smartnotes.iu.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.smartnotes.viewmodel.TaskViewModel
import com.example.smartnotes.data.Task

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTaskScreen(
    viewModel: TaskViewModel,
    editTaskId: String? = null,
    onDone: () -> Unit
) {
    val tasks by viewModel.tasks.collectAsState()
    val editingTask = editTaskId?.let { id -> tasks.find { it.id == id } }

    var title by remember { mutableStateOf(editingTask?.title ?: "") }
    var dateText by remember { mutableStateOf(editingTask?.dateTimeText ?: "") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (editingTask != null) "Editar tarea" else "Nueva tarea") },
                actions = {
                    TextButton(onClick = {
                        if (title.isBlank()) return@TextButton
                        if (editingTask != null) {
                            val t = editingTask.copy(title = title, dateTimeText = dateText)
                            viewModel.updateTask(t)
                        } else {
                            viewModel.addTask(title, dateText.ifBlank { "Sin fecha" })
                        }
                        onDone()
                    }) {
                        Text("Guardar")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier
            .padding(padding)
            .padding(16.dp)
        ) {
            OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Título") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(value = dateText, onValueChange = { dateText = it }, label = { Text("Fecha / Hora (texto)") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(12.dp))
            // Aquí puedes añadir controles para adjuntos, audio, etc.
            Text("Opciones adicionales (adjuntos, audio) se pueden agregar aquí", style = MaterialTheme.typography.bodySmall)
        }
    }
}
