package com.example.smartnotes.ui.viewmodels

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.smartnotes.R
import com.example.smartnotes.alarmas.AlarmItem
import com.example.smartnotes.alarmas.AlarmScheduler
import com.example.smartnotes.data.entities.ArchivosAdjuntos
import com.example.smartnotes.data.entities.NotasTareas
import com.example.smartnotes.data.entities.Recordatorios
import com.example.smartnotes.data.repository.ArchivosRepository
import com.example.smartnotes.data.repository.NotasTareasRepository
import com.example.smartnotes.data.repository.RecordatoriosRepository
import com.example.smartnotes.providers.FileProvider
import com.example.smartnotes.ui.components.AndroidAudioPlayer
import com.example.smartnotes.ui.components.AndroidAudioRecorder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

/**
 * ViewModel to validate and insert items in the Room database.
 */
class AddNoteTaskViewModel (
    private val repository: NotasTareasRepository,
    private val recordatorioRepository: RecordatoriosRepository,
    private val archivosRepository: ArchivosRepository,
    private val context: Context,
    private val alarmScheduler: AlarmScheduler
) : ViewModel() {
    /**
     * Holds current item ui state
     */
    var notaTareaUiState by mutableStateOf(NotaTareaUiState())
        private set

    /**
     * Updates the [notaTareaUiState] with the value provided in the argument. This method also triggers
     * a validation for input values.
     */
    fun updateUiState(notaTareaDetails: NotaTareaDetails) {
        notaTareaUiState =
            NotaTareaUiState(
                notaTareaDetails = notaTareaDetails,
                isEntryValid = validateInput(notaTareaDetails)
            )
    }

    private fun validateInput(uiState: NotaTareaDetails = notaTareaUiState.notaTareaDetails): Boolean {
        return with(uiState) {
            titulo.isNotBlank()
        }
    }

    // Actualizar el tipo al abrir la pantalla.
    fun setType(type: String) {
        // 1.Crear un estado base LIMPIO
        val newDetails = INITIAL_STATE.copy(tipo = type)
        updateUiState(newDetails)

        // 2. Limpiar listas de adjuntos y recordatorios
        attachments = emptyList()
        reminderDetails = ReminderDetails() 
        recordatoriosList = emptyList()
        selectedDateMillis = null
        selectedHour = 0
        selectedMinute = 0
    }

    // Cargar item existente para editar
    suspend fun loadItem(itemId: String) {
        val id = itemId.toIntOrNull() ?: return
        val item = repository.getNotaTareaById(id) ?: return

        // 1. Cargar detalles de la nota/tarea
        val details = item.toNotaTareaDetails()
        updateUiState(details)

        // 2. Si es tarea, cargar sus recordatorios
        if (item.tipo == "task") {
             val recordatorios = recordatorioRepository.getAllRecordatoriosStream(id).first()
             recordatoriosList = recordatorios.map { 
                 RecordatorioDetails(
                     opcionResId = it.opcion, 
                     fechaMillis = it.fecha
                 ) 
             }
             // Configurar fecha inicial para los pickers si ya existe
             details.fechaCumplimiento?.let {
                 val zoneId = java.time.ZoneId.systemDefault()
                 selectedDateMillis = it.atZone(zoneId).toInstant().toEpochMilli()
                 selectedHour = it.hour
                 selectedMinute = it.minute
             }
        }

        // CARGAR ADJUNTOS EXISTENTES DE LA BD
        // Esto permite ver las fotos/videos que ya estaban guardados
        val existingFiles = archivosRepository.getAllArchivosStream(id).first()
        attachments = existingFiles.map {
            ArchivoAdjuntoDetails(ruta = it.ruta, tipoArchivo = it.tipoArchivo)
        }
    }

    //Guardar nota/tarea ============================================================================
    suspend fun saveNotaTarea() {
        if (validateInput()) {
            val currentDetails = notaTareaUiState.notaTareaDetails
            val isEdit = currentDetails.id != 0
            
            val detailsToSave = currentDetails.copy(
                 // Si es edicion mantenemos la fecha registro original, si es nuevo ponemos now()
                 fechaRegistro = currentDetails.fechaRegistro ?: LocalDateTime.now()
            )
            
            val itemToSave = detailsToSave.toNotaTarea()
            val savedId: Long

            if (isEdit) {
                repository.updateItem(itemToSave)
                savedId = itemToSave.id.toLong()

                // ESTRATEGIA DE EDICIÓN: Borrar antiguos y reinsertar los actuales
                
                // Borrar archivos adjuntos previos en BD
                // Esto asegura que si eliminaste un adjunto de la lista, se borre de la BD
                val existingFiles = archivosRepository.getAllArchivosStream(savedId.toInt()).first()
                existingFiles.forEach { archivosRepository.deleteItem(it) }
                
                // Si es tarea, borrar recordatorios previos en BD
                if (itemToSave.tipo == "task") {
                    val existingReminders = recordatorioRepository.getAllRecordatoriosStream(savedId.toInt()).first()
                    existingReminders.forEach { recordatorioRepository.deleteItem(it) }
                }
            } else {
                //se guarda la entidad principal (notaTarea) y se obtiene su id
                savedId = repository.insertItem(itemToSave)
            }

            // Insertar los nuevos recordatorios
            if (itemToSave.tipo == "task") {
                recordatoriosList.forEach { reminder ->
                    val recordatorioEntity = Recordatorios(
                        tareaId = savedId.toInt(),
                        fecha = reminder.fechaMillis,
                        opcion = reminder.opcionResId
                    )

                    // Ahora esto funcionará correctamente:
                    val insertReminder = recordatorioRepository.insertItem(recordatorioEntity)
                    val reminderId = insertReminder.toInt()

                    //Recordatroios
                    // Programas la alarma
                    val alarmTime = java.time.Instant.ofEpochMilli(reminder.fechaMillis)
                        .atZone(java.time.ZoneId.systemDefault())
                        .toLocalDateTime()

                    // Solo programar si la fecha es futura
                    if (alarmTime.isAfter(LocalDateTime.now())) {
                        val prefix = context.getString(R.string.reminder_message)

                        val alarmItem = AlarmItem(
                            alarmTime = alarmTime,
                            message = "$prefix: ${itemToSave.descripcion.take(20)}...",
                            title = itemToSave.titulo,
                            taskId = savedId.toInt(),
                            reminderId = reminderId
                        )
                        alarmScheduler.schedule(alarmItem)
                    }
                }
            }

            // Insertar archivos adjuntos
            // La lista 'attachments' contiene el estado final deseado
            attachments.forEach { adjunto ->
                val archivoEntity = ArchivosAdjuntos(
                    notaTareaId = savedId.toInt(),
                    tipoArchivo = adjunto.tipoArchivo,
                    ruta = adjunto.ruta 
                )
                archivosRepository.insertItem(archivoEntity)
            }

            // Limpiar el estado después de guardar
            recordatoriosList = emptyList()
            attachments = emptyList()
        }
    }


    // Fecha de cunmplimienmto y recordaotrios =====================================================
    var showDatePicker by mutableStateOf(false)
        private set
    var showTimePicker by mutableStateOf(false)
        private set

    var selectedDateMillis: Long? by mutableStateOf(null)
        private set
    var selectedHour: Int by mutableStateOf(0)
        private set
    var selectedMinute: Int by mutableStateOf(0)
        private set


    fun setDatePickerVisibility(show: Boolean) {
        showDatePicker = show
    }
    fun setTimePickerVisibility(show: Boolean) {
        showTimePicker = show
    }
    fun updateSelectedDateMillis(millis: Long?) {
        selectedDateMillis = millis
    }

    fun updateDateTimeFromPickers(
        dateMillis: Long?, 
        hour: Int,
        minute: Int
    ) {
        if (dateMillis == null) return 

        val newDateTime = convertMillisToLocalDateTime(dateMillis, hour, minute)

        updateUiState(notaTareaUiState.notaTareaDetails.copy(
            fechaCumplimiento = newDateTime
        ))

        this.selectedHour = hour
        this.selectedMinute = minute

        // RECALCULAR RECORDATORIOS AL CAMBIAR LA FECHA
        recalcularRecordatorios(newDateTime)
    }

    // Función para recalcular todos los recordatorios existentes con la nueva fecha base
    private fun recalcularRecordatorios(newCumplimiento: LocalDateTime) {
        if (recordatoriosList.isEmpty()) return

        // Mapeamos la lista actual a una nueva lista con las fechas actualizadas
        recordatoriosList = recordatoriosList.map { reminder ->
            val newMillis = calcularFechaRecordatorio(reminder.opcionResId, newCumplimiento)
            reminder.copy(fechaMillis = newMillis)
        }
    }

    private fun convertMillisToLocalDateTime(
        dateMillis: Long,
        hour: Int,
        minute: Int
    ): java.time.LocalDateTime {
        val localDate = java.time.Instant.ofEpochMilli(dateMillis)
            .atZone(java.time.ZoneOffset.UTC)
            .toLocalDate()

        return java.time.LocalDateTime.of(localDate, java.time.LocalTime.of(hour, minute))
    }

    val fechaCumplimientoText: String
        get() = notaTareaUiState.notaTareaDetails.fechaCumplimiento?.let {
            val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
            it.format(formatter)
        } ?: "--------------"


    //Manejo de Recordatorios
    var reminderDetails by mutableStateOf(ReminderDetails())
        private set
    var recordatoriosList by mutableStateOf(emptyList<RecordatorioDetails>())
        private set

    fun updateReminderOption(option: Int) {
        reminderDetails = reminderDetails.copy(
            selectedReminderOption = option,
            isReminderExpanded = false
        )
    }

    private fun calcularFechaRecordatorio(opcionResId: Int, fechaCumplimiento: LocalDateTime): Long {
        var recordatorioDateTime = fechaCumplimiento

        when (opcionResId) {
            R.string.reminder_5_min -> recordatorioDateTime = fechaCumplimiento.minusMinutes(5)
            R.string.reminder_10_min -> recordatorioDateTime = fechaCumplimiento.minusMinutes(10)
            R.string.reminder_30_min -> recordatorioDateTime = fechaCumplimiento.minusMinutes(30)
            R.string.reminder_1_hour -> recordatorioDateTime = fechaCumplimiento.minusHours(1)
            R.string.reminder_1_day -> recordatorioDateTime = fechaCumplimiento.minusDays(1)
        }

        return recordatorioDateTime
            .atZone(java.time.ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    }

    fun addCurrentReminder() {
        val selectedOptionResId = reminderDetails.selectedReminderOption
        val cumplimiento = notaTareaUiState.notaTareaDetails.fechaCumplimiento

        if (cumplimiento == null || selectedOptionResId == R.string.reminder_none) {
            return
        }

        val recordatorioMillis = calcularFechaRecordatorio(selectedOptionResId, cumplimiento)
        
        if (recordatoriosList.any { it.opcionResId == selectedOptionResId }) {
            return 
        }

        val nuevoRecordatorio = RecordatorioDetails(
            opcionResId = selectedOptionResId,
            fechaMillis = recordatorioMillis
        )
        recordatoriosList = recordatoriosList + nuevoRecordatorio
        reminderDetails = reminderDetails.copy(selectedReminderOption = R.string.reminder_none)
    }
    
    fun removeReminder(reminder: RecordatorioDetails) {
        recordatoriosList = recordatoriosList - reminder
    }

    fun setReminderExpanded(expanded: Boolean) {
        reminderDetails = reminderDetails.copy(isReminderExpanded = expanded)
    }


    // ARCHIVOS ADJUNTOS =====================================================================
    var attachments by mutableStateOf(emptyList<ArchivoAdjuntoDetails>())
        private set

    // Audios -------------
    private val recordet by lazy {
        AndroidAudioRecorder(context)
    }

    // Estado para rastrear si se está grabando
    var isRecording by mutableStateOf(false)
        private set

    // Almacenar el archivo de audio actual (el objeto File antes de guardarse)
    private var currentAudioFile: File? = null

    fun startRecording(context: Context) {
        if (isRecording) return

        // Obtiene el objeto File del FileProvider
        val fileAudio = FileProvider.getAudioUri(context)

        currentAudioFile = fileAudio
        recordet.start(fileAudio)
        isRecording = true
    }
    fun stopRecording() {
        if (!isRecording) return

        recordet.stop()

        currentAudioFile?.let { file ->
            // Guarda la RUTA ABSOLUTA (String) del archivo local.
            val absolutePath = file.absolutePath

            // addAttachment lo guarda para la BD
            addAttachment(absolutePath, "audio")
        }

        currentAudioFile = null
        isRecording = false
    }


    // Agregar adjunto simple
    fun addAttachment(path: String, tipo: String) {
        attachments = attachments + ArchivoAdjuntoDetails(ruta = path, tipoArchivo = tipo)
    }

    // Eliminar adjunto de la lista visual
    fun removeAttachment(attachment: ArchivoAdjuntoDetails) {
        attachments = attachments - attachment
    }

    // Manejo especial para Galería/Documentos (copia a interno para evitar grises)
    suspend fun handleGallerySelection(context: Context, uri: Uri, tipo: String) {
        withContext(Dispatchers.IO) {
            val copiedPath = copiarArchivoAlAppDirInterno(context, uri, tipo)
            if (copiedPath != null) {
                withContext(Dispatchers.Main) {
                    addAttachment(copiedPath, tipo)
                }
            }
        }
    }

    // Función interna para copiar archivos (Soluciona imágenes grises/transparentes)
    private fun copiarArchivoAlAppDirInterno(ctx: Context, sourceUri: Uri, tipo: String): String? {
        try {
            val contentResolver = ctx.contentResolver
            
            val directorioBase = File(ctx.filesDir, "attachments")
            if (!directorioBase.exists()) directorioBase.mkdirs()

            val extension = when(tipo) {
                "image" -> ".jpg"
                "video" -> ".mp4"
                "audio" -> ".mp3"
                else -> ".bin"
            }

            val nombreArchivo = "note_file_${UUID.randomUUID()}$extension"
            val archivoDestino = File(directorioBase, nombreArchivo)

            val inputStream: InputStream? = contentResolver.openInputStream(sourceUri)
            val outputStream = FileOutputStream(archivoDestino)

            inputStream?.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }

            return archivoDestino.absolutePath

        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
}

// CLASES DE DATOS AUXILIARES
data class NotaTareaUiState(
    val notaTareaDetails: NotaTareaDetails = NotaTareaDetails(),
    val isEntryValid: Boolean = false
)

data class NotaTareaDetails(
    val id: Int = 0,
    val titulo: String = "",
    val descripcion: String = "",
    val tipo: String = "task", // "1 nota" o "0 tarea"
    val estaCumplida: Boolean = false,
    val fechaRegistro: LocalDateTime? = null,
    val fechaCumplimiento: LocalDateTime? = null
)


data class ReminderDetails(
    val isReminderExpanded: Boolean = false,
    val selectedReminderOption: Int = R.string.reminder_none, // Valor inicial
    val reminderOptions: List<Int> = listOf(
        R.string.reminder_none,
        R.string.reminder_on_time,
        R.string.reminder_5_min,
        R.string.reminder_10_min,
        R.string.reminder_30_min,
        R.string.reminder_1_hour,
        R.string.reminder_1_day
    ) // Opciones
)

data class RecordatorioDetails(
    val opcionResId: Int, 
    val fechaMillis: Long 
)

data class ArchivoAdjuntoDetails(
    val ruta: String,
    val tipoArchivo: String 
)

// EXTENSION FUNCTIONS
fun NotaTareaDetails.toNotaTarea(): NotasTareas = NotasTareas(
    id = id,
    titulo = titulo,
    descripcion = descripcion,
    tipo = tipo,
    estaCumplida = estaCumplida,
    fechaRegistro = fechaRegistro ?: LocalDateTime.now(),
    fechaCumplimiento = fechaCumplimiento
)

fun NotasTareas.toNotaTareaUiState(isEntryValid: Boolean = false): NotaTareaUiState = NotaTareaUiState(
    notaTareaDetails = this.toNotaTareaDetails(),
    isEntryValid = isEntryValid
)

fun NotasTareas.toNotaTareaDetails(): NotaTareaDetails = NotaTareaDetails(
    id = id,
    titulo = titulo,
    descripcion = descripcion,
    tipo = tipo,
    estaCumplida = estaCumplida,
    fechaRegistro = fechaRegistro,
    fechaCumplimiento = fechaCumplimiento
)

private val INITIAL_STATE = NotaTareaDetails(
    titulo = "",
    descripcion = "",
    tipo = "task",
    estaCumplida = false,
    fechaCumplimiento = null,
)
