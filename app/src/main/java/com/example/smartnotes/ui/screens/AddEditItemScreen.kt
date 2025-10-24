package com.example.smartnotes.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.smartnotes.data.Item
import com.example.smartnotes.data.ItemRepository
import com.example.smartnotes.ui.viewmodels.ItemViewModel
import java.io.File
import java.io.FileOutputStream
import android.media.MediaRecorder
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.Alignment
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditItemScreen(
    viewModel: ItemViewModel,
    type: String,
    editItemId: String? = null,
    onDone: () -> Unit,
    onBack: () -> Unit 
) {
    val context = LocalContext.current
    val editingItem = editItemId?.let { id -> ItemRepository.getItemById(id) }

    var title by remember { mutableStateOf(editingItem?.title ?: "") }
    var description by remember { mutableStateOf(editingItem?.description ?: "") }
    var dateTimeText by remember { mutableStateOf((editingItem as? Item.Task)?.dateTimeText ?: "") }
    var reminderText by remember { mutableStateOf((editingItem as? Item.Task)?.reminderText ?: "1 día antes") }

    val isTask = type == "task" || editingItem is Item.Task
    val titleBar = if (editItemId != null) "Editar ${if (isTask) "tarea" else "nota"}" else "Nueva ${if (isTask) "tarea" else "nota"}"

    // Estado para el menú desplegable de recordatorio
    val reminderOptions = listOf("No recordar", "Al momento", "5 minutos antes", "10 minutos antes", "30 minutos antes", "1 hora antes", "1 día antes")
    var isReminderExpanded by remember { mutableStateOf(false) }

    // Estado para los selectores de fecha y hora
    val calendar = Calendar.getInstance()
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = System.currentTimeMillis())
    val timePickerState = rememberTimePickerState(
        initialHour = calendar.get(Calendar.HOUR_OF_DAY),
        initialMinute = calendar.get(Calendar.MINUTE),
        is24Hour = true
    )

    // Estado para multimedia
    var attachments by remember { mutableStateOf(editingItem?.attachments ?: emptyList()) }
    var audios by remember { mutableStateOf(editingItem?.audios ?: emptyList()) }

    // Launcher para cámara
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        bitmap?.let {
            val file = File(context.getExternalFilesDir(null), "img_${System.currentTimeMillis()}.jpg")
            FileOutputStream(file).use { out -> bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out) }
            attachments = attachments + file.absolutePath
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(titleBar) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retroceder")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (title.isBlank()) return@IconButton
                            val newItem = if (isTask) {
                                Item.Task(
                                    id = editingItem?.id ?: UUID.randomUUID().toString(),
                                    title = title,
                                    description = description,
                                    dateTimeText = dateTimeText.ifBlank { "Sin fecha" },
                                    reminderText = reminderText,
                                    attachments = attachments,
                                    audios = audios,
                                    completed = (editingItem as? Item.Task)?.completed ?: false
                                )
                            } else {
                                Item.Note(
                                    id = editingItem?.id ?: UUID.randomUUID().toString(),
                                    title = title,
                                    description = description,
                                    attachments = attachments,
                                    audios = audios
                                )
                            }
                            if (editItemId != null) {
                                viewModel.updateItem(newItem)
                            } else {
                                viewModel.addItem(newItem)
                            }
                            onDone()
                        },
                        enabled = title.isNotBlank()
                    ) {
                        Icon(Icons.Default.Save, contentDescription = "Guardar")
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
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Título") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Descripción") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                maxLines = 5
            )
            if (isTask) {
                Spacer(modifier = Modifier.height(12.dp))

                // --- Campo de Fecha / Hora ---
                Box {
                    OutlinedTextField(
                        value = dateTimeText,
                        onValueChange = {},
                        label = { Text("Fecha / Hora") },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true
                    )
                    Box(modifier = Modifier.matchParentSize().clickable { showDatePicker = true })
                }

                Spacer(modifier = Modifier.height(12.dp))

                ExposedDropdownMenuBox(
                    expanded = isReminderExpanded,
                    onExpandedChange = { isReminderExpanded = !isReminderExpanded }
                ) {
                    OutlinedTextField(
                        value = reminderText,
                        onValueChange = {},
                        label = { Text("Recordatorio") },
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isReminderExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = isReminderExpanded,
                        onDismissRequest = { isReminderExpanded = false }
                    ) {
                        reminderOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    reminderText = option
                                    isReminderExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("Adjuntos:", style = MaterialTheme.typography.titleMedium)

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = { cameraLauncher.launch(null) }) {
                    Icon(Icons.Default.PhotoCamera, contentDescription = "Tomar foto")
                }
                IconButton(onClick = { /* galleryLauncher.launch("image/*") */ }) {
                    Icon(Icons.Default.AttachFile, contentDescription = "Seleccionar imagen/archivo")
                }
                IconButton(onClick = { /* if (isRecording) stopRecording() else startRecording() */ }) {
                    Icon(Icons.Default.Mic, contentDescription = "Grabar audio")
                    // if (isRecording) Text("⏹️", Modifier.padding(top = 4.dp))
                }
                IconButton(onClick = { /* filePickerLauncher.launch("*/*") */ }) {
                    Icon(Icons.Default.AttachFile, contentDescription = "Archivo")
                }
            }

            if (attachments.isNotEmpty()) {
                LazyRow(modifier = Modifier.fillMaxWidth()) {
                    items(attachments) { path ->
                        AsyncImage(
                            model = path,
                            contentDescription = null,
                            modifier = Modifier.size(60.dp).padding(4.dp)
                        )
                    }
                }
            }
            if (audios.isNotEmpty()) {
                LazyRow(modifier = Modifier.fillMaxWidth()) {
                    items(audios) { path ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Mic, contentDescription = null, modifier = Modifier.size(40.dp))
                            Text(path.substringAfterLast("/"), style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    showDatePicker = false
                    showTimePicker = true // Muestra el selector de hora después de confirmar la fecha
                }) { Text("Aceptar") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancelar") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        TimePickerDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    showTimePicker = false
                    datePickerState.selectedDateMillis?.let { selectedDateMillis ->
                        val selectedCalendar = Calendar.getInstance().apply {
                            timeInMillis = selectedDateMillis
                            set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                            set(Calendar.MINUTE, timePickerState.minute)
                        }
                        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                        dateTimeText = sdf.format(selectedCalendar.time)
                    }
                }) { Text("Aceptar") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("Cancelar") }
            }
        ) {
            TimePicker(state = timePickerState)
        }
    }
}


@Composable
fun TimePickerDialog(
    title: String = "Select Time",
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    dismissButton: @Composable (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = 6.dp,
            modifier = Modifier
                .width(IntrinsicSize.Min)
                .height(IntrinsicSize.Min)
                .wrapContentHeight()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp),
                    text = title,
                    style = MaterialTheme.typography.labelMedium
                )
                content()
                Row(
                    modifier = Modifier
                        .height(40.dp)
                        .fillMaxWidth()
                ) {
                    Spacer(modifier = Modifier.weight(1f))
                    dismissButton?.invoke()
                    confirmButton()
                }
            }
        }
    }
}
