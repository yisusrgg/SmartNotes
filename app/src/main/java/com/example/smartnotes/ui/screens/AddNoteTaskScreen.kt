package com.example.smartnotes.ui.screens


import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.smartnotes.R
import com.example.smartnotes.ui.components.AttachmentsSection
import com.example.smartnotes.ui.navigation.LayoutType
import com.example.smartnotes.ui.viewmodels.AddNoteTaskViewModel
import com.example.smartnotes.ui.viewmodels.NotaTareaDetails
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddNoteTaskScreen(
    viewModel: AddNoteTaskViewModel,
    type: String,
    onDone: () -> Unit,
    onBack: () -> Unit,
    layoutType: LayoutType
){
    LaunchedEffect(Unit) {
        viewModel.setType(type)
    }
    // El estado del UI se extrae para que los campos de texto reaccionen.
    val uiState = viewModel.notaTareaUiState
    val notaTareaDetails = uiState.notaTareaDetails
    val isEntryValid = uiState.isEntryValid

    val titleBar =  if (type == "task") stringResource(R.string.new_task_title)
                    else stringResource(R.string.new_note_title)

    val coroutineScope = rememberCoroutineScope()

    //tomar fotos
    val context = LocalContext.current
    // 1. Declaración del Launcher de Cámara (NECESITA ESTAR AQUÍ)
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        bitmap?.let {
            // Lógica para guardar la imagen en un archivo y obtener la ruta
            val file = File(context.getExternalFilesDir(null), "img_${System.currentTimeMillis()}.jpg")
            FileOutputStream(file).use { out -> it.compress(android.graphics.Bitmap.CompressFormat.JPEG, 100, out) }
            // 2. Usar la función del ViewModel para guardar la ruta
            viewModel.addAttachment(file.absolutePath)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(titleBar) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                ),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back_button_description))
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            coroutineScope.launch {
                                viewModel.saveNotaTarea()
                                onBack() // Navegar atrás después de guardar
                            }
                        },
                        enabled = isEntryValid
                    ){
                        Icon(Icons.Default.Save,
                            contentDescription = stringResource(R.string.save_button_description))
                    }
                }
            )
        }
    ) { padding ->
        NoteTaskBody(
            onValueChange = viewModel::updateUiState, // Pasar la función de actualización
            modifier = Modifier.padding(padding),
            viewModel = viewModel,
            tipo = notaTareaDetails.tipo, // Pasar el TIPO (0 o 1) del estado
            onDone = onDone,
            //onTakePhoto = { cameraLauncher.launch(null) },
            onTakePhoto = { /* Handle camara photo */ },
            onSelectFile = { /* Handle file selection */ },
            onRecordAudio = { /* Handle audio recording */ }
        )
    }
    // --- ESTADOS DE COMPOSE PARA LOS PICKERS ---
    // NO DEBEN ESTAR EN EL VM
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = viewModel.notaTareaUiState.notaTareaDetails.fechaCumplimiento
            ?.atZone(java.time.ZoneId.systemDefault())?.toInstant()?.toEpochMilli()
            ?: java.time.Instant.now().toEpochMilli()
    )
    val timePickerState = rememberTimePickerState(
        initialHour = viewModel.notaTareaUiState.notaTareaDetails.fechaCumplimiento?.hour ?: 0,
        initialMinute = viewModel.notaTareaUiState.notaTareaDetails.fechaCumplimiento?.minute ?: 0
    )


    // --- 1. DatePickerDialog ---
    if (viewModel.showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { viewModel.setDatePickerVisibility(false) },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        //viewModel.selectedDateMillis = it
                        viewModel.updateSelectedDateMillis(it)
                    }
                    viewModel.setDatePickerVisibility(false)
                    viewModel.setTimePickerVisibility(true) // Pasar a la hora
                }) { Text(stringResource(R.string.accept_button)) }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.setDatePickerVisibility(false) }) { Text(stringResource(R.string.cancel_button)) }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // --- 2. TimePickerDialog ---
    if (viewModel.showTimePicker) {
        AlertDialog(
            onDismissRequest = { viewModel.setTimePickerVisibility(false) },
            title = { Text(stringResource(R.string.select_time_title)) },
            text = { TimePicker(state = timePickerState) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.setTimePickerVisibility(false)

                    viewModel.updateDateTimeFromPickers(
                        dateMillis = viewModel.selectedDateMillis,
                        hour = timePickerState.hour,
                        minute = timePickerState.minute
                    )
                }) { Text(stringResource(R.string.accept_button)) }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.setTimePickerVisibility(false) }) { Text(stringResource(R.string.cancel_button)) }
            }
        )
    }
}

@Composable
fun NoteTaskBody(
    onValueChange: (NotaTareaDetails) -> Unit, // Callback para actualizar el estado.
    viewModel: AddNoteTaskViewModel,
    modifier: Modifier = Modifier,
    onDone: () -> Unit,
    tipo: String,
    onTakePhoto: () -> Unit,
    onSelectFile: () -> Unit,
    onRecordAudio: () -> Unit
){
    val notaTareaDetails = viewModel.notaTareaUiState.notaTareaDetails

    Column (modifier = modifier
        .padding(16.dp)
        .fillMaxSize()){
        OutlinedTextField(
            value = notaTareaDetails.titulo,
            onValueChange = { onValueChange(notaTareaDetails.copy(titulo = it)) },
            label = { Text(stringResource(R.string.title_label)) },
            //label = { Text(stringResource(R.string.title_label)) },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = notaTareaDetails.descripcion,
            onValueChange = { onValueChange(notaTareaDetails.copy(descripcion = it)) },
            label = { Text(stringResource(R.string.description_label)) },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            maxLines = 5
        )
        Spacer(Modifier.height(12.dp))

        //SOLO PARA TAREAS. Si es tarea, mostrará fecha/hora y recordatorios
        if (tipo == "task") {
            TaskFieldsSection(viewModel = viewModel)
            Spacer(Modifier.height(12.dp))
            val isDateSet = viewModel.notaTareaUiState.notaTareaDetails.fechaCumplimiento != null
            if(isDateSet) RemindersSection(viewModel = viewModel)
        }
        Spacer(modifier = Modifier.height(16.dp))

        //ARCHIVOS ---------------------
        AttachmentsSection(
            viewModel = viewModel,
            onTakePhotoClick = onTakePhoto,
            onSelectFileClick = onSelectFile,
            onRecordAudioClick = onRecordAudio
        )

        //Botones para tareas. Posponer y cunplida
        Spacer(modifier = Modifier.weight(1f))
        if (tipo == "task") {
            TaskActionButtons(
                onComplete = {
                    viewModel.updateUiState(notaTareaDetails.copy(estaCumplida = true))
                    //viewModel.save(null) { onDone() }
                },
                onPostpone = {
                    viewModel.setDatePickerVisibility(true)
                }
            )
        }
    }
}


@Composable
fun TaskFieldsSection(
    viewModel: AddNoteTaskViewModel
) {
    Spacer(modifier = Modifier.height(12.dp))
    Box {
        OutlinedTextField(
            //value = dateTimeText,
            value = viewModel.fechaCumplimientoText,
            onValueChange = {},
            label = { Text(stringResource(R.string.date_time_label)) },
            modifier = Modifier.fillMaxWidth(),
            readOnly = true
        )
        Box(modifier = Modifier
            .matchParentSize()
            .clickable {
                viewModel.setDatePickerVisibility(true)
            })
        /*Box(modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
            TextButton(onClick = { viewModel.showDatePicker = true }) {
                Text("Seleccionar fecha/hora")
            }
        }*/
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemindersSection(
    viewModel: AddNoteTaskViewModel
) {
    val reminderState = viewModel.reminderDetails
    val isOptionValid = reminderState.selectedReminderOption != R.string.reminder_none

    Spacer(modifier = Modifier.height(12.dp))

    // --- 1. DropdownMenuBox ---
    ExposedDropdownMenuBox(
        expanded = reminderState.isReminderExpanded,
        onExpandedChange = { viewModel.setReminderExpanded(!reminderState.isReminderExpanded) }
    ) {
        // --- 2. OutlinedTextField (Menu Anchor) ---
        OutlinedTextField(
            value = stringResource(id = reminderState.selectedReminderOption),
            onValueChange = {},
            label = { Text(stringResource(R.string.reminder_label)) },
            readOnly = true,
            trailingIcon = {
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {

                    // --- ICONO PARA AÑADIR EL RECORDATORIO ---
                    if (isOptionValid) {
                        IconButton(
                            onClick = {
                                viewModel.addCurrentReminder()
                            },
                            // Un icono de 'Añadir' o 'Check' para confirmar
                            modifier = Modifier
                                .size(24.dp)
                        ) {
                            Icon(
                                Icons.Default.AddCircleOutline,
                                contentDescription = stringResource(R.string.add_reminder_button),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))
                    // Icono de flecha del dropdown
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = reminderState.isReminderExpanded)
                    Spacer(modifier = Modifier.width(6.dp))
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )

        // --- 3. ExposedDropdownMenu ---
        ExposedDropdownMenu(
            expanded = reminderState.isReminderExpanded,
            onDismissRequest = { viewModel.setReminderExpanded(false) }
        ) {
            reminderState.reminderOptions.forEach { option ->
                DropdownMenuItem(
                    text = { Text(stringResource(option)) },
                    onClick = {
                        // Solo actualiza la opción seleccionada
                        viewModel.updateReminderOption(option)
                        // Cierra el menú después de la selección
                        viewModel.setReminderExpanded(false)
                    }
                )
            }
        }
    }

    // --- NUEVO: Mostrar Recordatorios Añadidos (Opcional, pero muy recomendado) ---
    // Esto es vital para que el usuario sepa que SÍ ha añadido un recordatorio
    Spacer(modifier = Modifier.height(8.dp))
    RecordatoriosListDisplay(viewModel = viewModel)
}

// -----------------------------------------------------------------
// COMPONENTE AUXILIAR PARA VISUALIZAR LOS RECORDATORIOS AÑADIDOS
// -----------------------------------------------------------------

@Composable
fun RecordatoriosListDisplay(viewModel: AddNoteTaskViewModel) {
    // Si la lista está vacía, no mostrar nada
    if (viewModel.recordatoriosList.isEmpty()) {
        return
    }

    Column {
        Text(
            text = stringResource(R.string.reminders_added_label), // Ej: "Recordatorios a guardar:"
            style = MaterialTheme.typography.labelMedium
        )
        viewModel.recordatoriosList.forEach { reminder ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // 1. Muestra cuándo debe sonar (Ej: 10 minutos antes)
                Text(
                    text = stringResource(id = reminder.opcionResId),
                    style = MaterialTheme.typography.bodyMedium
                )

                // 2. Muestra la hora exacta calculada
                // Formato la fecha Long (milisegundos) a una cadena legible
                val formattedTime = java.time.Instant.ofEpochMilli(reminder.fechaMillis)
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDateTime()
                    .format(java.time.format.DateTimeFormatter.ofPattern("dd/MM HH:mm"))

                Text(
                    text = "(${formattedTime})",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )

                // Opcional: Botón para eliminar un recordatorio de la lista
                /*
                IconButton(onClick = {
                    // Lógica para eliminar el recordatorio de la lista
                }) {
                    Icon(Icons.Default.Close, contentDescription = "Eliminar recordatorio")
                }
                */
            }
        }
    }
}


@Composable
fun TaskActionButtons(
    onComplete: () -> Unit,
    onPostpone: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Button(
            //onClick = { handleSave(completed = true) },
            onClick = onComplete,
            modifier = Modifier.weight(1f)
        ) {
            Icon(Icons.Filled.Check, contentDescription = null)
            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
            Text(stringResource(R.string.complete_button))
        }
        OutlinedButton(
            //onClick = { showDatePicker = true },
            onClick = onPostpone,
            modifier = Modifier.weight(1f)
        ) {
            Icon(Icons.Filled.Schedule, contentDescription = null)
            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
            Text(stringResource(R.string.postpone_button))
        }
    }
}
