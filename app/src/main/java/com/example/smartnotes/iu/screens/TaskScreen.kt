package com.example.smartnotes.iu.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.smartnotes.data.Item
import com.example.smartnotes.iu.components.ItemCard
import com.example.smartnotes.viewmodel.ItemViewModel
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(
    viewModel: ItemViewModel,
    onAddClick: (String) -> Unit, // Pasa "task" o "note"
    onDetailClick: (String) -> Unit
) {
    val items by viewModel.items.collectAsState()
    var selectedTab by remember { mutableStateOf(0) } // 0: Tareas, 1: Notas

    val filteredItems = remember(items, selectedTab) {
        if (selectedTab == 0) items.filterIsInstance<Item.Task>() else items.filterIsInstance<Item.Note>()
    }

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
            FloatingActionButton(
                onClick = {
                    val type = if (selectedTab == 0) "task" else "note"
                    onAddClick(type)
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 }
                ) { Text("Tareas", modifier = Modifier.padding(8.dp)) }
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 }
                ) { Text("Notas", modifier = Modifier.padding(8.dp)) }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (filteredItems.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No hay elementos aún. ¡Agrega uno!")
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(filteredItems) { item ->
                        ItemCard(
                            item = item,
                            onCheckedChange = { updated -> viewModel.updateItem(updated) },
                            onClick = { onDetailClick(item.id) }
                        )
                    }
                }
            }
        }
    }
}

