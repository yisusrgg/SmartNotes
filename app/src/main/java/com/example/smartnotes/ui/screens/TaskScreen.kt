package com.example.smartnotes.ui.screens

import android.annotation.SuppressLint
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.smartnotes.data.Item
import com.example.smartnotes.ui.components.ItemCard
import com.example.smartnotes.ui.viewmodels.ItemViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(
    viewModel: ItemViewModel,
    onAddClick: (String) -> Unit,
    onDetailClick: (String) -> Unit
) {
    val items by viewModel.items.collectAsState()
    var selectedTab by remember { mutableStateOf(0) } //0:Tareas, 1:Notas

    val filteredItems = remember(items, selectedTab) {
        if (selectedTab == 0) items.filterIsInstance<Item.Task>() else items.filterIsInstance<Item.Note>()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Tareas") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                ),
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
                },
                //containerColor = MaterialTheme.colorScheme.tertiary,
                //contentColor = MaterialTheme.colorScheme.onTertiary
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

            Spacer(modifier = Modifier.height(8.dp))

            if (filteredItems.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No hay elementos aún. ¡Agrega uno!")
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
                            onClick = { onDetailClick(item.id) }
                        )
                    }
                }
            }
        }
    }
}




// --- Pega este código al final de tu archivo TasksScreen.kt ---

// 1. Crea una implementación falsa del ViewModel para usarla en la vista previa
class FakeItemViewModel : ItemViewModel() {
    private val _items = MutableStateFlow<List<Item>>(emptyList())
    override val items: StateFlow<List<Item>> = _items

    init {
        // Llena el ViewModel falso con datos de ejemplo
        _items.value = listOf(
            Item.Task(title = "no", description = "Hacer la compra semanal", dateTimeText = "Mañana a las 10:00", completed = false, attachments = listOf("lista.pdf"), audios = listOf("audio1.mp3")),
            Item.Note( title="Idea para proyecto", description = "Investigar Firebase Realtime Database"),
            Item.Task( title="cita medica", description = "Llamar al dentista", dateTimeText = "Hoy a las 15:00", completed =  true, audios = listOf("audio2.mp3")),
            Item.Task( title = "terminar tarea", description = "Terminar el informe de Compose", dateTimeText = "Viernes", completed = false)
        )
    }

    // Las funciones de actualización no necesitan hacer nada en la vista previa
    override fun updateItem(item: Item) {
        println("Preview: updateItem llamado con $item")
    }

    override fun getItemById(id: String): Item? {
        println("Preview: getItemById llamado con id $id")
        return _items.value.find { it.id.toString() == id }
    }
}

// 2. Crea la función de Vista Previa (Preview)
@SuppressLint("ViewModelConstructorInComposable")
@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
fun TasksScreenPreview() {
    // Usamos el ViewModel falso y lambdas vacías para las acciones
    TasksScreen(
        viewModel = FakeItemViewModel(),
        onAddClick = { type -> println("Preview: onAddClick llamado con tipo: $type") },
        onDetailClick = { id -> println("Preview: onDetailClick llamado con id: $id") }
    )
}

// 3. (Opcional) Una vista previa para el estado vacío
@Preview(showBackground = true, name = "Estado Vacío")
@Composable
fun TasksScreenEmptyPreview() {
    // Creamos una instancia del ViewModel falso pero no le damos datos
    val emptyViewModel = object : ItemViewModel() {
        override val items: StateFlow<List<Item>> = MutableStateFlow(emptyList())
    }

    TasksScreen(
        viewModel = emptyViewModel,
        onAddClick = {},
        onDetailClick = {}
    )
}
