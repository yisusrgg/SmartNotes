package com.example.smartnotes.ui.screens

import androidx.compose.foundation.background
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
import com.example.smartnotes.ui.components.ItemCard
import com.example.smartnotes.ui.viewmodels.ItemViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(
    viewModel: ItemViewModel,
    onAddClick: (String) -> Unit,
    onDetailClick: (String) -> Unit
) {
    val items by viewModel.items.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredItems = remember(items, selectedTab, searchQuery) {
        val itemsByType = if (selectedTab == 0) items.filterIsInstance<Item.Task>() else items.filterIsInstance<Item.Note>()
        if (searchQuery.isNotBlank()) {
            itemsByType.filter { it.title.contains(searchQuery, ignoreCase = true) }
        } else {
            itemsByType
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("SmartNotes") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
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
                ) {
                    Text("Tareas", modifier = Modifier.padding(8.dp))
                }
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 }
                ) {
                    Text("Notas", modifier = Modifier.padding(8.dp))
                }
            }

            if (filteredItems.isEmpty()) {
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No hay elementos aún. ¡Agrega uno!")
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .background(MaterialTheme.colorScheme.surfaceContainerLow)
                ){
                    items(filteredItems) { item ->
                        ItemCard(
                            item = item,
                            onCheckedChange = { updated -> viewModel.updateItem(updated) },
                            onClick = { onDetailClick(item.id) },
                            onDelete = { viewModel.removeItem(item.id) }
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Buscar") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar") },
                    modifier = Modifier.weight(1f)
                )
                FloatingActionButton(
                    onClick = {
                        val type = if (selectedTab == 0) "task" else "note"
                        onAddClick(type)
                    }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Agregar")
                }
            }
        }
    }
}
