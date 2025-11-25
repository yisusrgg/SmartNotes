@file:Suppress("PreviewAnnotationInFunctionWithParameters")

package com.example.smartnotes.ui.screens

import android.R.attr.contentDescription
import android.R.attr.tint
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.smartnotes.R
import com.example.smartnotes.ui.viewmodels.ItemsListViewModel
import com.example.smartnotes.ui.viewmodels.NotaTareaUiModel
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ui.PlayerView
import android.media.MediaPlayer
import androidx.compose.foundation.layout.widthIn
import androidx.compose.ui.text.style.TextOverflow
import com.example.smartnotes.ui.components.AndroidAudioPlayer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    viewModel: ItemsListViewModel,
    itemId: String,
    onBack: () -> Unit,
    onEditClick: (String) -> Unit
) {
    val items by viewModel.itemsUiState.collectAsState()
    val item = items.find { it.id == itemId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        item?.title ?: "",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis //"..."
                    )
                },
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
                    //Boton de editar
                    IconButton(onClick = { onEditClick(itemId) }) {
                        Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.edit_button_description))
                    }
                }
            )
        }
    ) { padding ->
        if (item != null) {
            ItemDetailView(
                item = item,
                viewModel = viewModel,
                onTaskCompleted = onBack,
                modifier = Modifier.padding(padding)
            )
        }
    }
}


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
                Text("${stringResource(R.string.date_label)}: ${taskItem.dateTimeText}",
                    style = MaterialTheme.typography.bodySmall
                )
                
                // --- Visualización de Recordatorios ---
                if (taskItem.reminders.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "${stringResource(R.string.reminder_detail_label)}:",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    taskItem.reminders.forEach { reminder ->
                        Row(
                            modifier = Modifier.padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Recordatorio",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.secondary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            // Opción (ej: "10 min antes")
                            Text(
                                text = stringResource(id = reminder.opcionResId),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            // Hora exacta
                            val formattedTime = java.time.Instant.ofEpochMilli(reminder.fechaMillis)
                                .atZone(java.time.ZoneId.systemDefault())
                                .format(java.time.format.DateTimeFormatter.ofPattern("dd/MM HH:mm"))
                                
                            Text(
                                text = "($formattedTime)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = item.description,
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(12.dp))
            AttachmentsDisplay(item = item)
        }
    }
}


// Componente para generar thumbnail de video (Igual que en AttachmentsSection)
@Composable
fun DetailVideoThumbnail(
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
                // Obtener frame en el tiempo 0
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
        // Fondo gris mientras carga o si falla
        Box(modifier = modifier.background(Color.DarkGray))
    }
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
    item : NotaTareaUiModel
) {
    val archivos = item.attachments
    if (archivos.isEmpty()) return

    val context = LocalContext.current

    val audios = archivos.filter { it.tipoArchivo == "audio" }
    val otrosArchivos = archivos.filter { it.tipoArchivo != "audio" }

    Text(
        "${stringResource(R.string.attachments_label)}:",
        //style = MaterialTheme.typography.titleMedium,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.secondary
    )

    //Mostrar audios
    if (audios.isNotEmpty()) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp) // Espacio entre audios y otros adjuntos
        ) {
            audios.forEach { audio ->
                AudioAttachmentDisplay(
                    audioPath = audio.ruta,
                    audioType = audio.tipoArchivo
                )
            }
        }
    }

    // (Imágenes, Videos, Docs)
    if (otrosArchivos.isNotEmpty()) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            otrosArchivos.forEach { archivo ->
                // Lógica para abrir archivo al hacer click
                val onAttachmentClick = {
                    try {
                        val path = archivo.ruta
                        val uri: Uri
                        val isContentUri = path.startsWith("content://")

                        if (isContentUri) {
                            uri = Uri.parse(path)
                        } else {
                            val file = File(path)
                            if (file.exists()) {
                                uri = FileProvider.getUriForFile(
                                    context,
                                    "${context.packageName}.fileprovider",
                                    file
                                )
                            } else {
                                uri = Uri.parse(path)
                            }
                        }

                        val mimeType = when (archivo.tipoArchivo) {
                            "image" -> "image/*"
                            //"video" -> "video/*"
                            "audio" -> "audio/*"
                            "document" -> "application/pdf"
                            else -> "*/*"
                        }

                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            setDataAndType(uri, mimeType)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(Intent.createChooser(intent, "Abrir con..."))

                    } catch (e: Exception) {
                        Toast.makeText(context, "No se puede abrir el archivo: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                    }
                }
                //------------------------------------------------------------------------

                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onAttachmentClick),
                    contentAlignment = Alignment.Center
                ) {
                    when (archivo.tipoArchivo) {
                        "image" -> {
                            AsyncImage(
                                model = archivo.ruta,
                                contentDescription = archivo.tipoArchivo,
                                modifier = Modifier.matchParentSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                        "video" -> {
                            // MOSTRAR MINIATURA DEL VIDEO EN LUGAR DE FONDO NEGRO
                            DetailVideoThumbnail(
                                videoUri = archivo.ruta,
                                modifier = Modifier.matchParentSize()
                            )

                            // Icono de Play encima
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .background(Color.Black.copy(alpha = 0.3f)), // Sombra ligera para contraste
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.PlayArrow,
                                    contentDescription = "Video",
                                    tint = Color.White,
                                    modifier = Modifier
                                        .size(48.dp)
                                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                                        .padding(8.dp)
                                )
                            }
                        }
                        "document" -> {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.matchParentSize().background(MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Icon(
                                    Icons.Default.Description,
                                    contentDescription = "Documento",
                                    tint = MaterialTheme.colorScheme.tertiary,
                                    modifier = Modifier.size(48.dp)
                                )
                                Text("PDF/Doc", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }
    }
}
