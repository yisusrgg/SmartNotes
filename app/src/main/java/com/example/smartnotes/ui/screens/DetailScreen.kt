package com.example.smartnotes.ui.screens

import android.media.MediaPlayer
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.smartnotes.data.Item
import com.example.smartnotes.data.ItemRepository
import com.example.smartnotes.ui.viewmodels.ItemViewModel
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    viewModel: ItemViewModel,
    itemId: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val items by viewModel.items.collectAsState()
    val item = items.find { it.id == itemId } ?: return

    val isTask = item is Item.Task
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(item.title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
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
                modifier = Modifier.fillMaxWidth() .padding(8.dp),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    if (isTask) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Fecha: ${(item as Item.Task).dateTimeText}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "Recordatorio: ${(item as Item.Task).reminderText}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = item.description,
                        style = MaterialTheme.typography.bodyLarge
                    )


                    // Multimedia
                    if (item.attachments.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Adjuntos:", style = MaterialTheme.typography.bodyLarge)
                        LazyRow {
                            items(item.attachments) { path ->
                                AsyncImage(
                                    model = path,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(100.dp)
                                        .padding(4.dp)
                                )
                            }
                        }
                    }
                    if (item.audios.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Audios:", style = MaterialTheme.typography.bodyMedium)
                        LazyRow {
                            items(item.audios) { path ->
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(4.dp)) {
                                    IconButton(onClick = {
                                        mediaPlayer?.release()
                                        mediaPlayer = MediaPlayer.create(context, Uri.parse(path)).apply {
                                            start()
                                        }
                                    }) {
                                        Icon(Icons.Default.PlayArrow, contentDescription = "Reproducir")
                                    }
                                    Text(path.substringAfterLast("/"), style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isTask) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            val updated = (item as Item.Task).copy(completed = true)
                            viewModel.updateItem(updated)
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Completar")
                    }
                    OutlinedButton(
                        onClick = {
                            val updated = (item as Item.Task).copy(dateTimeText = "Postergada al ${System.currentTimeMillis()}")
                            viewModel.updateItem(updated)
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Schedule, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Postergar")
                    }
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