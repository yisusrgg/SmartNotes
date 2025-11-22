package com.example.smartnotes.ui.components

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import coil.compose.AsyncImage
import com.example.smartnotes.R
import com.example.smartnotes.ui.viewmodels.AddNoteTaskViewModel

@Composable
fun AttachmentsSection(
    viewModel: AddNoteTaskViewModel,
    onTakePhotoClick: () -> Unit,
    onSelectFileClick: () -> Unit,
    onRecordAudioClick: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity
    
    // Extraer el estado (para evitar usar viewModel.propiedad en cada lugar)
    val attachmentsList = viewModel.attachments
    val audiosList = viewModel.audios

    // Función auxiliar para abrir configuración
    fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
        }
        context.startActivity(intent)
    }

    // --- PERMISOS DE CÁMARA ---
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            onTakePhotoClick()
        } else {
            // Si 'shouldShowRequestPermissionRationale' es false DESPUÉS de pedir el permiso y ser denegado,
            // significa que el usuario eligió "No volver a preguntar" o el sistema lo bloqueó.
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
        } else {
            val showRationale = activity?.let {
                ActivityCompat.shouldShowRequestPermissionRationale(it, Manifest.permission.RECORD_AUDIO)
            } ?: false

            if (!showRationale) {
                Toast.makeText(context, "Permiso de audio denegado. Ve a Ajustes para activarlo.", Toast.LENGTH_LONG).show()
                openAppSettings()
            } else {
                Toast.makeText(context, "Se requiere permiso de micrófono", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // --- GALERÍA (PHOTO PICKER) ---
    // 1. Lanzador del selector de fotos
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        // Cuando el usuario selecciona una imagen/video, recibimos la URI
        uri?.let { 
            // TEMPORALMENTE DESHABILITADO:
            // No agregamos la URI al ViewModel hasta que se implemente FileProvider para separar archivos por nota.
            // viewModel.addAttachment(it.toString()) 
        }
    }

    // 2. Definir qué permisos pedir según la versión de Android
    val galleryPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        // Android 13+ necesita permisos granulares
        listOf(Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO)
    } else {
        // Android 12 e inferiores usan almacenamiento externo
        listOf(Manifest.permission.READ_EXTERNAL_STORAGE)
    }

    // 3. Lanzador de Permisos para Galería
    val galleryPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        // Verificamos si se concedió algún permiso necesario (lectura de imagenes o video)
        val isGranted = permissions.values.any { it }
        
        if (isGranted) {
            // Si hay permiso, abrimos el selector nativo (Fotos y Videos)
            galleryLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
            )
        } else {
            // Lógica de denegación permanente para múltiples permisos
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

    Text(stringResource(R.string.attachments_label), style = MaterialTheme.typography.titleMedium)

    Spacer(Modifier.height(8.dp))
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        // Botón CÁMARA
        IconButton(onClick = { 
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }) {
            Icon(Icons.Default.PhotoCamera, contentDescription = stringResource(R.string.take_photo_description))
        }
        
        // Botón GALERÍA
        IconButton(onClick = {
            // Solicitamos permisos y abrimos galería, pero NO guardamos el resultado por ahora.
            galleryPermissionLauncher.launch(galleryPermissions.toTypedArray())
        }) {
            Icon(Icons.Default.Image, contentDescription = "Seleccionar de Galería")
        }

        // Botón ARCHIVO
        IconButton(onClick = { onSelectFileClick() }) {
            Icon(Icons.Default.AttachFile, contentDescription = stringResource(R.string.select_file_description))
        }

        // Botón AUDIO
        IconButton(onClick = { 
            audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }) {
            Icon(Icons.Default.Mic, contentDescription = stringResource(R.string.record_audio_description))
        }
    }

    // TEMPORALMENTE COMENTADO: Visualización de adjuntos
    // Se oculta para evitar confusión de ver las mismas imágenes en todas las notas
    // hasta que se implemente la lógica de FileProvider individual.
    /*
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
    */
}
