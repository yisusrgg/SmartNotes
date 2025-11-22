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
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let { 
            // TEMPORALMENTE DESHABILITADO
            // viewModel.addAttachment(it.toString()) 
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
    // 1. Lanzador del selector de documentos
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            // TEMPORALMENTE DESHABILITADO (Igual que la galería, esperando FileProvider)
            // viewModel.addAttachment(it.toString())
            // Si quieres que funcione temporalmente, descomenta la línea de arriba
        }
    }

    // 2. Lanzador de Permisos para Archivos (Solo necesario en Android < 13 para acceso general)
    // En Android 13+, OpenDocument no requiere permisos, pero mantenemos la estructura si se solicita.
    val filePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted || Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Abrimos el selector para todo tipo de archivos
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
            galleryPermissionLauncher.launch(galleryPermissions.toTypedArray())
        }) {
            Icon(Icons.Default.Image, contentDescription = "Seleccionar de Galería")
        }

        // Botón ARCHIVO (DOCUMENTOS)
        IconButton(onClick = { 
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // En Android 13+ no necesitamos permiso para OpenDocument
                filePickerLauncher.launch(arrayOf("*/*"))
            } else {
                // En versiones anteriores pedimos almacenamiento
                filePermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }) {
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
