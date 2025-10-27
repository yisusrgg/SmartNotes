package com.example.smartnotes.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.smartnotes.R
import com.example.smartnotes.data.Item
import com.example.smartnotes.data.ItemRepository
import com.example.smartnotes.ui.theme.SmartNotesTheme
import com.example.smartnotes.ui.viewmodels.ItemViewModel
import java.io.File
import java.io.FileOutputStream
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

    val isTask = type == "task" || editingItem is Item.Task

    val reminderOptions = listOf(
        stringResource(R.string.reminder_none),
        stringResource(R.string.reminder_on_time),
        stringResource(R.string.reminder_5_min),
        stringResource(R.string.reminder_10_min),
        stringResource(R.string.reminder_30_min),
        stringResource(R.string.reminder_1_hour),
        stringResource(R.string.reminder_1_day)
    )
    var reminderText by remember { mutableStateOf((editingItem as? Item.Task)?.reminderText ?: reminderOptions[6]) }
    var isReminderExpanded by remember { mutableStateOf(false) }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = System.currentTimeMillis())
    val timePickerState = rememberTimePickerState()

    var attachments by remember { mutableStateOf(editingItem?.attachments ?: emptyList()) }
    var audios by remember { mutableStateOf(editingItem?.audios ?: emptyList()) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        bitmap?.let {
            val file = File(context.getExternalFilesDir(null), "img_${System.currentTimeMillis()}.jpg")
            FileOutputStream(file).use { out -> bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 100, out) }
            attachments = attachments + file.absolutePath
        }
    }

    fun handleSave(completed: Boolean = false) {
        if (title.isBlank()) return
        val newItem = if (isTask) {
            Item.Task(
                id = editingItem?.id ?: UUID.randomUUID().toString(),
                title = title,
                description = description,
                dateTimeText = dateTimeText.ifBlank { "Sin fecha" },
                reminderText = reminderText,
                attachments = attachments,
                audios = audios,
                completed = completed || (editingItem as? Item.Task)?.completed ?: false
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
    }
    val titleBar = if (editItemId != null) {
        if (isTask) stringResource(R.string.edit_task_title) else stringResource(R.string.edit_note_title)
    } else {
        if (isTask) stringResource(R.string.new_task_title) else stringResource(R.string.new_note_title)
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
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back_button_description))
                    }
                },
                actions = {
                    IconButton(onClick = { handleSave() }, enabled = title.isNotBlank()) {
                        Icon(Icons.Default.Save, contentDescription = stringResource(R.string.save_button_description))
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
                label = { Text(stringResource(R.string.title_label)) },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text(stringResource(R.string.description_label)) },
                modifier = Modifier.fillMaxWidth().height(120.dp),
                maxLines = 5
            )
            if (isTask) {
                Spacer(modifier = Modifier.height(12.dp))
                Box {
                    OutlinedTextField(
                        value = dateTimeText,
                        onValueChange = {},
                        label = { Text(stringResource(R.string.date_time_label)) },
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
                        label = { Text(stringResource(R.string.reminder_label)) },
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
            Text(stringResource(R.string.attachments_label), style = MaterialTheme.typography.titleMedium)

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = { cameraLauncher.launch(null) }) {
                    Icon(Icons.Default.PhotoCamera, contentDescription = stringResource(R.string.take_photo_description))
                }
                IconButton(onClick = { /* TODO */ }) {
                    Icon(Icons.Default.AttachFile, contentDescription = stringResource(R.string.select_file_description))
                }
                IconButton(onClick = { /* TODO */ }) {
                    Icon(Icons.Default.Mic, contentDescription = stringResource(R.string.record_audio_description))
                }
            }

            if (attachments.isNotEmpty()) {
                LazyRow(modifier = Modifier.fillMaxWidth()) {
                    items(attachments) { path ->
                        AsyncImage(model = path, contentDescription = null, modifier = Modifier.size(60.dp).padding(4.dp))
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

            Spacer(modifier = Modifier.weight(1f))

            if (isTask) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        onClick = { handleSave(completed = true) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Filled.Check, contentDescription = null)
                        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                        Text(stringResource(R.string.complete_button))
                    }
                    OutlinedButton(
                        onClick = { showDatePicker = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Filled.Schedule, contentDescription = null)
                        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                        Text(stringResource(R.string.postpone_button))
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
                    showTimePicker = true
                }) { Text(stringResource(R.string.accept_button)) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text(stringResource(R.string.cancel_button)) }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text(stringResource(R.string.select_time_title)) },
            text = {
                TimePicker(state = timePickerState)
            },
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
                }) { Text(stringResource(R.string.accept_button)) }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text(stringResource(R.string.cancel_button)) }
            }
        )
    }
}

@Composable
fun ItemInputForm(
    modifier : Modifier = Modifier,

){
    Column (modifier = modifier){

    }
}


@Preview(showBackground = true)
@Preview(showBackground = true, locale = "en")
@Composable
fun AddEditScreenPreview() {
    val context = LocalContext.current
    SmartNotesTheme {
        AddEditItemScreen(
            viewModel = ItemViewModel(),
            type = "task",
            onDone = {},
            onBack = {}
        )
    }
}
