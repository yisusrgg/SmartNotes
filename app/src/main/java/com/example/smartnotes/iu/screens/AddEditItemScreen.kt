package com.example.smartnotes.iu.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
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
import com.example.smartnotes.viewmodel.ItemViewModel
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import android.media.MediaRecorder
import android.os.Environment
import androidx.activity.result.ActivityResult
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.Alignment
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditItemScreen(
    viewModel: ItemViewModel,
    type: String,
    editItemId: String? = null,
    onDone: () -> Unit
) {
    val context = LocalContext.current
    val items by viewModel.items.collectAsState()
    val editingItem = editItemId?.let { id -> ItemRepository.getItemById(id) }

    var title by remember { mutableStateOf(editingItem?.title ?: "") }
    var description by remember { mutableStateOf(editingItem?.description ?: "") }
    var dateTimeText by remember { mutableStateOf((editingItem as? Item.Task)?.dateTimeText ?: "") }
    var reminderText by remember { mutableStateOf((editingItem as? Item.Task)?.reminderText ?: "1 día antes") }

    val isTask = type == "task" || editingItem is Item.Task
    val titleBar = if (editItemId != null) "Editar ${if (isTask) "tarea" else "nota"}" else "Nueva ${if (isTask) "tarea" else "nota"}"

    // Estado para multimedia
    var attachments by remember { mutableStateOf(editingItem?.attachments ?: emptyList()) }
    var audios by remember { mutableStateOf(editingItem?.audios ?: emptyList()) }

    // Launcher para cámara
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        bitmap?.let {
            val file = File(context.getExternalFilesDir(null), "img_${System.currentTimeMillis()}.jpg")
            FileOutputStream(file).use { out -> bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 100, out) }
            attachments = attachments + file.absolutePath
        }
    }

    // Launcher para galería
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { attachments = attachments + uri.toString() }
    }

    // Launcher para grabar audio
    var isRecording by remember { mutableStateOf(false) }
    var mediaRecorder: MediaRecorder? by remember { mutableStateOf(null) }
    val audioLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { _ -> } // No usado directamente

    // Launcher para picker de archivos
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { attachments = attachments + uri.toString() } // Trata como attachment genérico
    }

    // Permisos
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        // Maneja grants/denials si necesitas
    }

    LaunchedEffect(Unit) {
        val permissions = mutableListOf<String>()
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.CAMERA)
        }
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.RECORD_AUDIO)
        }
        if (permissions.isNotEmpty()) {
            permissionLauncher.launch(permissions.toTypedArray())
        }
    }

    // Función para grabar audio
    fun startRecording() {
        val file = File(context.getExternalFilesDir(null), "audio_${System.currentTimeMillis()}.mp3")
        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(file.absolutePath)
            prepare()
            start()
        }
        isRecording = true
    }

    fun stopRecording() {
        mediaRecorder?.apply {
            stop()
            release()
        }
        mediaRecorder = null
        isRecording = false
        val filePath = File(context.getExternalFilesDir(null), "audio_${SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.US).format(Date())}.mp3").absolutePath
        // Nota: Ajusta path real del file grabado
        audios = audios + filePath
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(titleBar) },
                actions = {
                    IconButton(
                        onClick = {
                            if (title.isBlank()) return@IconButton
                            val newItem = if (isTask) {
                                Item.Task(
                                    id = editingItem?.id ?: "",
                                    title = title,
                                    description = description,
                                    dateTimeText = dateTimeText.ifBlank { "Sin fecha" },
                                    reminderText = reminderText,
                                    attachments = attachments,
                                    audios = audios,
                                    completed = editingItem?.completed ?: false
                                )
                            } else {
                                Item.Note(
                                    id = editingItem?.id ?: "",
                                    title = title,
                                    description = description,
                                    attachments = attachments,
                                    audios = audios,
                                    completed = editingItem?.completed ?: false
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
                OutlinedTextField(
                    value = dateTimeText,
                    onValueChange = { dateTimeText = it },
                    label = { Text("Fecha / Hora") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = reminderText,
                    onValueChange = { reminderText = it },
                    label = { Text("Recordatorio") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("Adjuntos:", style = MaterialTheme.typography.titleMedium)

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Botón Cámara
                IconButton(onClick = { cameraLauncher.launch(null) }) {
                    Icon(Icons.Default.PhotoCamera, contentDescription = "Tomar foto")
                }
                // Botón Galería (para attachments genéricos)
                IconButton(onClick = { galleryLauncher.launch("image/*") }) {
                    Icon(Icons.Default.AttachFile, contentDescription = "Seleccionar imagen/archivo")
                }
                // Botón Micrófono
                IconButton(onClick = { if (isRecording) stopRecording() else startRecording() }) {
                    Icon(Icons.Default.Mic, contentDescription = "Grabar audio")
                    if (isRecording) Text("⏹️", Modifier.padding(top = 4.dp))
                }
                // Botón Archivo
                IconButton(onClick = { filePickerLauncher.launch("*/*") }) {
                    Icon(Icons.Default.AttachFile, contentDescription = "Archivo")
                }
            }

            // Lista de attachments actuales (previews simples)
            if (attachments.isNotEmpty()) {
                LazyRow(modifier = Modifier.fillMaxWidth()) {
                    items(attachments) { path ->
                        AsyncImage(
                            model = path,
                            contentDescription = null,
                            modifier = Modifier
                                .size(60.dp)
                                .padding(4.dp)
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
}