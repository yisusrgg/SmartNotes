package com.example.smartnotes.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.smartnotes.R
import com.example.smartnotes.ui.viewmodels.AddNoteTaskViewModel
import coil.compose.AsyncImage
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun AttachmentsSection(
    viewModel: AddNoteTaskViewModel,
    onTakePhotoClick: () -> Unit,
    onSelectFileClick: () -> Unit,
    onRecordAudioClick: () -> Unit
) {
    // Extraer el estado (para evitar usar viewModel.propiedad en cada lugar)
    val attachmentsList = viewModel.attachments
    val audiosList = viewModel.audios

    Text(stringResource(R.string.attachments_label), style = MaterialTheme.typography.titleMedium)

    Spacer(Modifier.height(8.dp))
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        IconButton(onClick = { onTakePhotoClick }) {
            Icon(Icons.Default.PhotoCamera, contentDescription = stringResource(R.string.take_photo_description))
        }
        IconButton(onClick = { onSelectFileClick }) {
            Icon(Icons.Default.AttachFile, contentDescription = stringResource(R.string.select_file_description))
        }
        IconButton(onClick = { onRecordAudioClick }) {
            Icon(Icons.Default.Mic, contentDescription = stringResource(R.string.record_audio_description))
        }
    }

    if (attachmentsList.isNotEmpty()) {
        LazyRow {
            items(attachmentsList) { path ->
                AsyncImage(model = path, contentDescription = null, modifier = Modifier
                    .size(60.dp)
                    .padding(4.dp))
            }
        }
    }

    if (audiosList.isNotEmpty()) {
        LazyRow(modifier = Modifier.fillMaxWidth()) {
            items(audiosList) { path ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Mic, contentDescription = null, modifier = Modifier.size(40.dp))
                    Text(path.substringAfterLast("/"), style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

