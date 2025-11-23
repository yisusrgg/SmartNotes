@file:Suppress("PreviewAnnotationInFunctionWithParameters")

package com.example.smartnotes.ui.screens

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import com.example.smartnotes.R
import com.example.smartnotes.ui.viewmodels.ItemsListViewModel
import com.example.smartnotes.ui.viewmodels.NotaTareaUiModel
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ui.PlayerView
import android.media.MediaPlayer
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Videocam

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
                title = { Text(item?.title ?: "") },
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
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = item.description,
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(12.dp))
            if (item.attachments.isNotEmpty()) {
                Text(
                    "${stringResource(R.string.attachments_label)}:",
                    style = MaterialTheme.typography.bodyLarge
                )
                LazyRow {
                    items(item.attachments) { path ->
                        AsyncImage(
                            model = path,
                            contentDescription = null,
                            modifier = Modifier.size(100.dp).padding(4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun VideoPlayer(videoUri: Uri, modifier: Modifier = Modifier.fillMaxWidth()) {
    val context = LocalContext.current
    val exoPlayer = remember {
        SimpleExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(videoUri))
            prepare()
        }
    }
    val playbackState = exoPlayer
    val isPlaying = playbackState?.isPlaying ?: false

    AndroidView(
        factory = { context ->
            PlayerView(context).apply {
                player = exoPlayer
            }
        },
        modifier = modifier
    )

    IconButton(
        onClick = {
            if (isPlaying) {
                exoPlayer.pause()
            } else {
                exoPlayer.play()
            }
        },
        modifier = Modifier
            //.align(Alignment.BottomEnd)
            .padding(16.dp)
    ) {
        Icon(
            imageVector = if (isPlaying) Icons.Filled.Refresh else Icons.Filled.PlayArrow,
            contentDescription = if (isPlaying) "Pause" else "Play",
            tint = Color.White,
            modifier = Modifier.size(48.dp)
        )
    }
}


@Composable
fun AudioPlayer(audioUri: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var isPlaying by remember { mutableStateOf(false) }

    DisposableEffect(audioUri) {
        mediaPlayer = MediaPlayer.create(context, Uri.parse(audioUri))
        mediaPlayer?.setOnCompletionListener { isPlaying = false }
        onDispose {
            mediaPlayer?.release()
            mediaPlayer = null
        }
    }

    Row(
        modifier = modifier.padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Mic, contentDescription = "Audio", tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(8.dp))
        Text(audioUri.substringAfterLast('/'))

        IconButton(onClick = {
            if (isPlaying) {
                mediaPlayer?.pause()
                isPlaying = false
            } else {
                mediaPlayer?.start()
                isPlaying = true
            }
        }) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = (if (isPlaying) R.string.pause
                else R.string.play_audio_description).toString()
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
    val scrollState = rememberScrollState()

    Text(
        "${stringResource(R.string.attachments_label)}:",
        style = MaterialTheme.typography.bodyLarge,
        modifier = Modifier.padding(bottom = 8.dp)
    )

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(archivos) { archivo ->
            // Usamos un Box para poder superponer elementos (como el icono de Video o el botón de Play)
            Box(
                modifier = Modifier
                    .size(100.dp),
                contentAlignment = Alignment.Center
            ) {
                // Usamos 'when' para decidir qué mostrar según el tipo de archivo
                when (archivo.tipoArchivo) {
                    "image", "video" -> {
                        // Usamos AsyncImage para mostrar el thumbnail o frame del video/imagen
                        AsyncImage(
                            model = archivo.ruta,
                            contentDescription = archivo.tipoArchivo,
                            modifier = Modifier
                                .matchParentSize()
                        )
                        // Si es video, superponemos un icono de reproducción
                        if (archivo.tipoArchivo == "video") {
                            //reproducir video
                            val action = VideoPlayer(videoUri = Uri.parse(archivo.ruta))
                            IconButton(
                                onClick = {
                                    action
                                },
                                modifier = Modifier
                                    .size(24.dp)
                            ) {
                                Icon(
                                    Icons.Filled.Videocam,
                                    contentDescription = "Video",
                                    tint = Color.White,
                                    modifier = Modifier.size(48.dp)
                                )
                            }
                        }
                    }
                    "audio" -> {
                        // Para audios, solo mostramos el icono de Micrófono como thumbnail
                        Icon(
                            Icons.Default.Mic,
                            contentDescription = "Audio",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(60.dp)
                        )
                        // NOTA: Para reproducir, se lanzaría AudioPlayer en un diálogo.
                    }
                    "document" -> {

                        Icon(
                            Icons.Default.Description,
                            contentDescription = "Documento",
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(60.dp)
                        )
                    }
                }
            }
        }
    }
}