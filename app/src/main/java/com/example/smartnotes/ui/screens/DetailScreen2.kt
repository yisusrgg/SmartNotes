/*
package com.example.smartnotes.ui.screens

import android.media.MediaPlayer
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.smartnotes.R
import com.example.smartnotes.data.Item
import com.example.smartnotes.ui.navigation.LayoutType
import com.example.smartnotes.ui.viewmodels.ItemViewModel
import com.example.smartnotes.ui.viewmodels.ItemsListViewModel
import com.example.smartnotes.ui.viewmodels.NotaTareaUiModel
import java.text.SimpleDateFormat
import java.util.*


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    viewModel: ItemsListViewModel,
    itemId: String,
    onBack: () -> Unit,
    onEditClick: (String) -> Unit,
    layoutType: LayoutType
) {
    val context = LocalContext.current
    // Accedemos al StateFlow del VM
    val items by viewModel.itemsUiState.collectAsState()
    // Buscamos el ítem por ID (asegúrate de que los IDs sean Strings en el modelo UI)
    val item = items.find { it.id == itemId } ?: return

    val isTask = item is NotaTareaUiModel.Task
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

    // Convertimos a Tarea si es necesario para acceder a campos específicos
    val taskItem = item as? NotaTareaUiModel.Task

    //Dise;o adapatovo para difentes pantallas
    if (layoutType == LayoutType.EXPANDED) {
        Row(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Panel Izquierdo: Título y Descripción
            Column(Modifier.weight(2f)) {
                DetailContent(item) // Contenido principal de la nota/tarea
            }
            // Panel Derecho: Archivos Adjuntos, Audios y Acciones
            Column(Modifier.weight(1f)) {
                ActionsAndMedia(item, viewModel) // Nuevo composable para botones y archivos
            }
        }
    } else {
        // Layout Compacto (diseño actual, una columna vertical)
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            DetailContent(item)
            ActionsAndMedia(item, viewModel)
        }
    }

    // State for Date and Time pickers
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()
    val timePickerState = rememberTimePickerState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(item.title) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                ),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back_button_description))
                    }
                },
                actions = {
                    //BOTÓN DE EDITAR
                    IconButton(onClick = { onEditClick(item.id) }) {
                        Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.edit_button_description))
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    if (isTask && taskItem != null) {
                        // Usar los campos del modelo Task
                        Text("${stringResource(R.string.date_label)}: ${taskItem.dateTimeText}", style = MaterialTheme.typography.bodySmall)
                        // Text("${stringResource(R.string.reminder_detail_label)}: ${item.reminderText}", style = MaterialTheme.typography.bodySmall)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = item.description,
                        style = MaterialTheme.typography.bodyLarge
                    )


                    // Mostrar Archivos Adjuntos
                    if (item.attachments.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("${stringResource(R.string.attachments_label)}:", style = MaterialTheme.typography.bodyLarge)
                        LazyRow {
                            items(item.attachments) { path ->
                                AsyncImage(model = path, contentDescription = null, modifier = Modifier.size(100.dp).padding(4.dp))
                            }
                        }
                    }
                    // Mostrar Audios
                    if (item.audios.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Audios:", style = MaterialTheme.typography.bodyMedium)
                        LazyRow {
                            items(item.audios) { path ->
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(4.dp)) {
                                    IconButton(onClick = {
                                        mediaPlayer?.release()
                                        mediaPlayer = MediaPlayer.create(context, Uri.parse(path)).apply { start() }
                                    }) {
                                        Icon(Icons.Default.PlayArrow, contentDescription = stringResource(R.string.play_audio_description))
                                    }
                                    Text(path.substringAfterLast("/"), style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Botón de Completar Tarea
            if (isTask && taskItem != null) {
                Button(
                    onClick = {
                        // Actualizar la tarea como completada y volver
                        val updated = taskItem.copy(completed = true)
                        viewModel.updateItem(updated)
                        onBack()
                    },
                    enabled = !taskItem.completed, // Deshabilitar si ya está completada
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(stringResource(R.string.complete_button))
                }
            }
        }

        DisposableEffect(Unit) {
            onDispose {
                mediaPlayer?.release()
            }
        }
    }
}


//Contenido Principal de la Tarea/Nota
@Composable
fun DetailContent(item: NotaTareaUiModel) {
    val isTask = item is NotaTareaUiModel.Task
    val taskItem = item as? NotaTareaUiModel.Task

    Card(
        modifier = Modifier
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(10.dp))

            if (isTask && taskItem != null) {
                Text("${stringResource(R.string.date_label)}: ${taskItem.dateTimeText}", style = MaterialTheme.typography.bodySmall)
                // Aquí irían los detalles del recordatorio si estuvieran mapeados
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = item.description,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

// Acciones y Media (Archivos/Audios)
@Composable
fun ActionsAndMedia(
    item: NotaTareaUiModel,
    viewModel: ItemsListViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val isTask = item is NotaTareaUiModel.Task
    val taskItem = item as? NotaTareaUiModel.Task
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) } // Necesitas manejar este estado aquí o elevarlo

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Archivos Adjuntos
        if (item.attachments.isNotEmpty()) {
            Text("${stringResource(R.string.attachments_label)}:", style = MaterialTheme.typography.bodyLarge)
            LazyRow { */
/* ... lógica de AsyncImage ... *//*
 }
        }

        // Audios (con lógica de MediaPlayer)
        if (item.audios.isNotEmpty()) {
            Text("Audios:", style = MaterialTheme.typography.bodyMedium)
            LazyRow { */
/* ... lógica de reproducción ... *//*
 }
        }

        Spacer(modifier = Modifier.weight(1f)) // Empuja los botones hacia abajo

        // Botón de Completar Tarea
        if (isTask && taskItem != null) {
            Button(
                onClick = {
                    val updated = taskItem.copy(completed = true)
                    viewModel.updateItem(updated)
                    onBack()
                },
                enabled = !taskItem.completed,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text(stringResource(R.string.complete_button))
            }
        }

        // Efecto de liberación de MediaPlayer (MUY IMPORTANTE)
        DisposableEffect(item.id) {
            onDispose {
                mediaPlayer?.release()
                mediaPlayer = null
            }
        }
    }
}*/
