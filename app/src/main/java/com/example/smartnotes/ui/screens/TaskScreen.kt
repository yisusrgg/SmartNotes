package com.example.smartnotes.ui.screens

import android.media.MediaPlayer
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.smartnotes.R
import com.example.smartnotes.ui.components.ItemCard
import com.example.smartnotes.ui.navigation.LayoutType
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
    val items by viewModel.itemsUiState.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }

    // State for expanded layout selection
    var selectedItemId by remember { mutableStateOf<String?>(null) }
    val selectedItem = remember(selectedItemId, items) {
        items.find { it.id == selectedItemId }
    }
    // Reset selection when switching tabs
    LaunchedEffect(selectedTab) {
        selectedItemId = null
    }

    val filteredItems = remember(items, selectedTab, searchQuery) {
        val itemsByType =
            if (selectedTab == 0)
                items.filterIsInstance<NotaTareaUiModel.Task>()
            else
                items.filterIsInstance<NotaTareaUiModel.Note>()
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

            Box(
                modifier = Modifier
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
                        filteredItems = filteredItems,
                        viewModel = viewModel,
                        selectedItem = selectedItem,
                        onItemClick = { itemId ->
                            selectedItemId = itemId
                        },
                        onTaskCompleted = {
                            selectedItemId = null // Clear selection
                        }
                    )
                }
            }

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
    selectedItem: NotaTareaUiModel?,
    onItemClick: (String) -> Unit,
    onTaskCompleted: () -> Unit
) {
    Row(Modifier.fillMaxSize()) {
        // Panel izquierdo: Lista de ítems
        Box(modifier = Modifier.weight(0.4f)) {
            CompactTasksLayout(
                filteredItems = filteredItems,
                viewModel = viewModel,
                onDetailClick = onItemClick
            )
        }

        // Separador vertical
        VerticalDivider(
            modifier = Modifier
                .fillMaxHeight()
                .width(1.dp),
            color = MaterialTheme.colorScheme.outlineVariant
        )

        // Panel derecho: Vista de detalle
        Box(modifier = Modifier.weight(0.6f)) {
            if (selectedItem != null) {
                ItemDetailView(
                    item = selectedItem,
                    viewModel = viewModel,
                    onTaskCompleted = onTaskCompleted
                )
            } else {
                // Placeholder cuando no hay nada seleccionado
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Select an item to see details.")
                }
            }
        }
    }
}


// --- Composables de Detalle (extraídos de DetailScreen) ---
@Composable
fun ItemDetailView(
    item: NotaTareaUiModel,
    viewModel: ItemsListViewModel,
    onTaskCompleted: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        DetailContent(item)
        ActionsAndMedia(item, viewModel, onTaskCompleted)
    }
}


@Composable
fun ActionsAndMedia(
    item: NotaTareaUiModel,
    viewModel: ItemsListViewModel,
    onTaskCompleted: () -> Unit
) {
    val context = LocalContext.current
    val isTask = item is NotaTareaUiModel.Task
    val taskItem = item as? NotaTareaUiModel.Task
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {


        Spacer(modifier = Modifier.weight(1f))

        if (isTask && taskItem != null) {
            Button(
                onClick = {
                    val updated = taskItem.copy(completed = true)
                    viewModel.updateItem(updated)
                    onTaskCompleted()
                },
                enabled = !taskItem.completed,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text(stringResource(R.string.complete_button))
            }
        }

        DisposableEffect(item.id) {
            onDispose {
                mediaPlayer?.release()
                mediaPlayer = null
            }
        }
    }
}
