package com.example.smartnotes.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartnotes.data.Item
import com.example.smartnotes.ui.viewmodels.NotaTareaUiModel

@Composable
fun ItemCard(
    item: NotaTareaUiModel,
    onCheckedChange: (NotaTareaUiModel) -> Unit,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val isTask = item is NotaTareaUiModel.Task
    val isCompleted = if (isTask) (item as NotaTareaUiModel.Task).completed else false

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCompleted) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(12.dp)
        ) {
            when (item) {
                is NotaTareaUiModel.Task -> {
                    // Solo se muestra el Checkbox para Tareas
                    Checkbox(
                        checked = item.completed,
                        onCheckedChange = { isChecked ->
                            onCheckedChange(item.copy(completed = isChecked))
                        },
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
                is NotaTareaUiModel.Note -> {}
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp)
            ) {
                Text(
                    text = item.title,
                    maxLines = 2,
                    style = if (isCompleted) MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    ) else MaterialTheme.typography.bodyLarge,
                    fontWeight = if (isCompleted) FontWeight.Normal else FontWeight.Bold
                )
                if (item is NotaTareaUiModel.Task) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = item.dateTimeText,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (item.attachments.isNotEmpty()) {
                    BadgedBox(
                        badge = {
                            if (item.attachments.size > 1) {
                                Badge(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ) { Text("${item.attachments.size}", fontSize = 10.sp) }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.AttachFile,
                            contentDescription = "Adjuntos (${item.attachments.size})",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                if (item.audios.isNotEmpty()) {
                    BadgedBox(
                        badge = {
                            if (item.audios.size > 1) {
                                Badge(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ) { Text("${item.audios.size}", fontSize = 10.sp) }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Audios (${item.audios.size})",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Borrar")
                }
            }
        }
    }
}

// EN: .../ui/components/ItemCard.kt

// PRIMERA PREVIEW (Esta ya está correcta)
@Preview(showBackground = true)
@Composable
fun ItemCardPreview() {
    // Creas una instancia de NotaTareaUiModel.Task, lo cual es correcto.
    val sampleTask = NotaTareaUiModel.Task(
        id="0",
        title = "Hacer la compra de la semana",
        description = "no se",
        dateTimeText = "Mañana a las 10:00 AM",
        completed = false,
        attachments = listOf("lista_compra.pdf"), // Ejemplo con un adjunto
        audios = listOf("recordatorio.mp3", "otro_audio.mp3") // Ejemplo con dos audios
    )

    ItemCard(
        item = sampleTask,
        onCheckedChange = {},
        onClick = {},
        onDelete = {}
    )
}

// SEGUNDA PREVIEW (CORREGIDA)
@Preview(showBackground = true)
@Composable
fun NoteCardPreview() {
    // CORRECCIÓN: Crea una instancia de 'NotaTareaUiModel.Note'
    val sampleNote = NotaTareaUiModel.Note(
        id="0",
        title = "Idea para nuevo proyecto",
        description = "Investigar Firebase Realtime Database",
        attachments = emptyList(),
        audios = emptyList()
    )

    // Ahora le estás pasando el tipo correcto a la función
    ItemCard(
        item = sampleNote,
        onCheckedChange = {},
        onClick = {},
        onDelete = {}
    )
}
