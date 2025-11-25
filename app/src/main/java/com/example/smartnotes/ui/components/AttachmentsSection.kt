package com.example.smartnotes.ui.components

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import coil.compose.AsyncImage
import com.example.smartnotes.R
import com.example.smartnotes.ui.screens.AudioAttachmentDisplay
import com.example.smartnotes.ui.viewmodels.AddNoteTaskViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun AttachmentsSection(
    viewModel: AddNoteTaskViewModel,
    onTakePhotoClick: () -> Unit,
    onCaptureVideoClick: () -> Unit,
    onSelectFileClick: () -> Unit,
    onRecordAudioClick: () -> Unit
) {
    val context = LocalContext.current
    val isRecording = viewModel.isRecording
    val activity = context as? Activity
    val scope = rememberCoroutineScope()
    
    // Extraer el estado (para evitar usar viewModel.propiedad en cada lugar)
    val attachmentsList = viewModel.attachments

    // PERIMOSOS ==============================================================================-

    // Función auxiliar para abrir configuración
    fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
        }
        context.startActivity(intent)
    }

    // --- PERMISOS DE CÁMARA (Para foto Y video) ---
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            onTakePhotoClick()
        } else {
            val showRationale = activity?.let { 
                ActivityCompat.shouldShowRequestPermissionRationale(it, Manifest.permission.CAMERA) 
            } ?: false

            if (!showRationale) {
                Toast.makeText(context, "Permiso denegado permanentemente. Ve a Ajustes para activarlo.", Toast.LENGTH_LONG).show()
                openAppSettings()
            } else {
                Toast.makeText(context, "Se requiere permiso de cámara", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // --- LANZADOR ESPECÍFICO PARA VIDEO ---
    val videoPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            onCaptureVideoClick()
        } else {
            val showRationale = activity?.let { 
                ActivityCompat.shouldShowRequestPermissionRationale(it, Manifest.permission.CAMERA) 
            } ?: false

            if (!showRationale) {
                Toast.makeText(context, "Permiso denegado permanentemente. Ve a Ajustes para activarlo.", Toast.LENGTH_LONG).show()
                openAppSettings()
            } else {
                Toast.makeText(context, "Se requiere permiso de cámara", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // --- PERMISOS DE MICRÓFONO ---
    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            onRecordAudioClick()
            //viewModel.startRecording(context)
        } else {
            val showRationale = activity?.let {
                ActivityCompat.shouldShowRequestPermissionRationale(it, Manifest.permission.RECORD_AUDIO)
            } ?: false

            if (!showRationale) {
                Toast.makeText(context,
                    "Permiso de audio denegado. Ve a Ajustes para activarlo.",
                    Toast.LENGTH_LONG
                ).show()
                openAppSettings()
            } else {
                Toast.makeText(context,
                    "Se requiere permiso de micrófono",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    // --- GALERÍA (PHOTO PICKER) ---
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let { 
             // Aquí asumimos que es imagen o video. Podemos intentar detectar el tipo mime
             val type = context.contentResolver.getType(it)
             val isVideo = type?.startsWith("video/") == true
             
             // CORRECCIÓN: Usar handleGallerySelection para copiar el archivo
             scope.launch {
                 viewModel.handleGallerySelection(context, it, if (isVideo) "video" else "image")
             }
        }
    }

    val galleryPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        listOf(Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO)
    } else {
        listOf(Manifest.permission.READ_EXTERNAL_STORAGE)
    }

    val galleryPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val isGranted = permissions.values.any { it }
        if (isGranted) {
            galleryLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
            )
        } else {
            val showRationale = galleryPermissions.any { permission ->
                activity?.let { ActivityCompat.shouldShowRequestPermissionRationale(it, permission) } ?: false
            }
            if (!showRationale) {
                Toast.makeText(context, "Permiso de galería denegado. Ve a Ajustes para activarlo.", Toast.LENGTH_LONG).show()
                openAppSettings()
            } else {
                Toast.makeText(context, "Se requiere permiso para acceder a la galería", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // --- DOCUMENTOS (SELECTOR DE ARCHIVOS) ---
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
             //handleGallerySelection también para documentos
             scope.launch {
                viewModel.handleGallerySelection(context, it, "document")
             }
        }
    }

    val filePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted || Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            filePickerLauncher.launch(arrayOf("*/*")) 
        } else {
             val showRationale = activity?.let {
                ActivityCompat.shouldShowRequestPermissionRationale(it, Manifest.permission.READ_EXTERNAL_STORAGE)
            } ?: false

            if (!showRationale) {
                Toast.makeText(context, "Permiso de almacenamiento denegado. Ve a Ajustes.", Toast.LENGTH_LONG).show()
                openAppSettings()
            } else {
                Toast.makeText(context, "Se requiere permiso de almacenamiento", Toast.LENGTH_SHORT).show()
            }
        }
    }
    //=====================================================================================


    Text(stringResource(R.string.attachments_label), style = MaterialTheme.typography.titleMedium)

    Spacer(Modifier.height(8.dp))
    
    // FILA DE BOTONES DE ACCIÓN (en agregar o editar) =======================================
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        // Botón FOTO
        IconButton(onClick = { 
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }) {
            Icon(Icons.Default.PhotoCamera, contentDescription = stringResource(R.string.take_photo_description))
        }

        // Botón VIDEO
        IconButton(onClick = { 
            videoPermissionLauncher.launch(Manifest.permission.CAMERA)
        }) {
            Icon(Icons.Default.Videocam, contentDescription = "Grabar video")
        }
        
        // Botón GALERÍA
        IconButton(onClick = {
            galleryPermissionLauncher.launch(galleryPermissions.toTypedArray())
        }) {
            Icon(Icons.Default.Image, contentDescription = "Seleccionar de Galería")
        }

        // Botón ARCHIVO (DOCUMENTOS)
        IconButton(onClick = { 
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                filePickerLauncher.launch(arrayOf("*/*"))
            } else {
                filePermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }) {
            Icon(Icons.Default.AttachFile, contentDescription = stringResource(R.string.select_file_description))
        }

        // Botón AUDIO
        IconButton(
            onClick = {
                if (isRecording) {
                    viewModel.stopRecording()
                } else {
                    // Si no está grabando, inicia (chequeando/pidiendo permiso)
                    audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                }
            }
        ) {
            Icon(
                // Cambia el icono si está grabando
                imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                contentDescription = if (isRecording) "Detener grabación"
                                    else stringResource(R.string.record_audio_description),
                tint = if (isRecording) Color.Red else Color.Black
            )
        }
    }
    //======================================================================================

    Spacer(Modifier.height(12.dp))

    // --- VISUALIZACIÓN DE ADJUNTOS (MINIATURAS) en agregar o editar ------------------------
    AttachmentsDisplay(viewModel)
}

@Composable
fun AudioAttachmentDisplay(
    audioPath: String,
    audioType: String
) {
    val context = LocalContext.current

    // Estado del reproductor
    val audioPlayer = remember { AndroidAudioPlayer(context) }
    var isPlaying by remember { mutableStateOf(false) }

    // Limpiza automatica: el reproductor se libera cuando se dejan de consumir la app
    DisposableEffect(audioPath) {
        onDispose {
            audioPlayer.stop()
        }
    }

    Row(
        modifier = Modifier
            .widthIn(min = 200.dp) // Ancho mínimo para que se vea bien
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .padding(vertical = 8.dp, horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        //Icono y Nombre del Archivo
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Default.Mic,
                contentDescription = "Audio",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(8.dp))

            Text(
                text = audioPath.substringAfterLast('/'),
                style = MaterialTheme.typography.bodyMedium
            )
        }

        //Botón de Reproducción/Pausa
        IconButton(onClick = {
            if (isPlaying) {
                //Pausar
                audioPlayer.stop()
                isPlaying = false
            } else {
                // Reproducir
                audioPlayer.play(audioPath) { isPlaying = false }
                isPlaying = true
            }
        }) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isPlaying) "Pausar" else "Reproducir"
            )
        }
    }
}

@Composable
fun AttachmentsDisplay(
    viewModel: AddNoteTaskViewModel
) {
    val archivos = viewModel.attachments
    if (archivos.isEmpty()) return

    val context = LocalContext.current
    val audios = archivos.filter { it.tipoArchivo == "audio" }
    val otrosArchivos = archivos.filter { it.tipoArchivo != "audio" }

    if(audios.isNotEmpty()){
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        ) {
            audios.forEach { audio ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // El reproductor
                    Box(modifier = Modifier.weight(1f)) {
                        AudioAttachmentDisplay(
                            audioPath = audio.ruta,
                            audioType = audio.tipoArchivo
                        )
                    }
                    // Botón de eliminar audio
                    IconButton(
                        onClick = { viewModel.removeAttachment(audio) }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Eliminar audio",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }

    if (otrosArchivos.isNotEmpty()) {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(otrosArchivos) { attachment ->
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    // CONTENIDO SEGÚN TIPO
                    when (attachment.tipoArchivo) {
                        "image" -> {
                            AsyncImage(
                                model = attachment.ruta,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.matchParentSize()
                            )
                        }
                        "video" -> {
                            // Usamos el componente personalizado para videos
                            VideoThumbnail(
                                videoUri = attachment.ruta,
                                modifier = Modifier.matchParentSize()
                            )

                            // Icono play overlay
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                                    .padding(4.dp)
                            )
                        }
                        "document" -> {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.Description,
                                    contentDescription = "Documento",
                                    tint = MaterialTheme.colorScheme.secondary
                                )
                                Text(
                                    text = "Archivo",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }

                    // BOTÓN DE ELIMINAR (X)
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .padding(4.dp),
                        contentAlignment = Alignment.TopEnd
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Eliminar adjunto",
                            tint = Color.White,
                            modifier = Modifier
                                .size(20.dp)
                                .background(Color.Red, CircleShape)
                                .padding(2.dp)
                                .clickable {
                                    viewModel.removeAttachment(attachment)
                                }
                        )
                    }
                }
            }
        }
    }
}

// Componente auxiliar para generar thumbnail de video
@Composable
fun VideoThumbnail(
    videoUri: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var bitmap by remember { mutableStateOf<ImageBitmap?>(null) }

    LaunchedEffect(videoUri) {
        withContext(Dispatchers.IO) {
            val retriever = MediaMetadataRetriever()
            try {
                retriever.setDataSource(context, Uri.parse(videoUri))
                // Obtener frame en el tiempo 0 (microsegundos)
                val frame = retriever.getFrameAtTime(0)
                frame?.let { bitmap = it.asImageBitmap() }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                try {
                    retriever.release()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    if (bitmap != null) {
        Image(
            bitmap = bitmap!!,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = modifier
        )
    } else {
        // Mientras carga o si falla, mostramos un fondo (ya manejado por el Box padre)
        Box(modifier = modifier.background(Color.Gray))
    }
}
