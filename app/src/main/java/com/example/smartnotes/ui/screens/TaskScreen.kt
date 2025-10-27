package com.example.smartnotes.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.smartnotes.R
import com.example.smartnotes.data.Item
import com.example.smartnotes.ui.components.ItemCard
import com.example.smartnotes.ui.navigation.LayoutType
import com.example.smartnotes.ui.viewmodels.ItemViewModel
import com.example.smartnotes.ui.viewmodels.ItemsListViewModel
import com.example.smartnotes.ui.viewmodels.NotaTareaUiModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(
    viewModel: ItemsListViewModel,
    onAddClick: (String) -> Unit,
    onDetailClick: (String) -> Unit,
    layoutType: LayoutType
) {
    //StateFlow ahora contiene NotaTareaUiModel
    val items by viewModel.itemsUiState.collectAsState()
    var selectedTab by remember { mutableStateOf(0) } //0:Tareas, 1:Notas
    var searchQuery by remember { mutableStateOf("") }

    val filteredItems = remember(items, selectedTab, searchQuery) {
        val itemsByType = if (selectedTab == 0)
            items.filterIsInstance<NotaTareaUiModel.Task>()
        else items.filterIsInstance<NotaTareaUiModel.Note>()
        if (searchQuery.isNotBlank()) {
            itemsByType.filter { it.title.contains(searchQuery, ignoreCase = true) }
        } else {
            itemsByType
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.tasks_title)) },
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
                    Text(stringResource(R.string.tasks_tab), modifier = Modifier.padding(8.dp))
                }
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 }
                ) {
                    Text(stringResource(R.string.notes_tab), modifier = Modifier.padding(8.dp))
                }
            }

            //CONTENIDO ADAPTATIVO: USANDO EL WHEN DENTRO DEL COLUMN
            Box(
                modifier = Modifier
                    //Ocupa el espacio restante
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (layoutType) {
                    LayoutType.COMPACT -> CompactTasksLayout(
                        filteredItems,
                        viewModel,
                        onDetailClick
                    )
                    LayoutType.MEDIUM, LayoutType.EXPANDED -> ExpandedTasksLayout(
                        filteredItems,
                        viewModel,
                        onDetailClick
                    )
                }
            }

            //Barra de busqueda y boton flotante
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Transparent)
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text(stringResource(R.string.search_placeholder)) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = stringResource(R.string.search_placeholder)) },
                    modifier = Modifier.weight(1f)
                )
                FloatingActionButton(
                    onClick = {
                        val type = if (selectedTab == 0) "task" else "note"
                        onAddClick(type)
                    }
                ) {
                    Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_button_description))
                }
            }
        }
    }
}

@Composable
fun CompactTasksLayout(
    filteredItems: List<NotaTareaUiModel>,
    viewModel: ItemsListViewModel,
    onDetailClick: (String) -> Unit
) {
    if (filteredItems.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(stringResource(R.string.empty_list_message))
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
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
}

@Composable
fun ExpandedTasksLayout(
    filteredItems: List<NotaTareaUiModel>,
    viewModel: ItemsListViewModel,
    onDetailClick : (String) -> Unit
) {
    //Diseño de dos columnas o rejilla para mejor UX
    LazyVerticalGrid(
        //Ajusta automáticamente 2 o 3 columnas
        columns = GridCells.Adaptive(minSize = 300.dp),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
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