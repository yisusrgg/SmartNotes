package com.example.smartnotes.iu.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.smartnotes.data.Task
import com.example.smartnotes.viewmodel.TaskViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Add
import androidx.compose.ui.tooling.preview.Preview
import com.example.smartnotes.iu.components.TaskCard


@OptIn(ExperimentalMaterial3Api::class)

@Composable
fun TasksScreen(
    viewModel: TaskViewModel,
    onAddClick: () -> Unit,
    onEditClick: (String) -> Unit
) {
    val tasks by viewModel.tasks.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Tareas") },
                actions = {
                    IconButton(onClick = { /* search */ }) {
                        Icon(Icons.Default.Search, contentDescription = "Buscar")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddClick) {
                Icon(Icons.Default.Add, contentDescription = "Agregar")
            }
        }
    ) { padding ->
        Column(modifier = Modifier
            .padding(padding)
            .fillMaxSize()
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) { Text("Tareas", modifier = Modifier.padding(8.dp)) }
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) { Text("Notas", modifier = Modifier.padding(8.dp)) }
            }

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(tasks) { task ->
                    TaskCard(
                        task = task,
                        onCheckedChange = { updated ->
                            viewModel.updateTask(updated)
                        },
                        onClick = { onEditClick(task.id) }
                    )
                }
            }
        }
    }
}


