package com.example.smartnotes.ui.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.ViewModel
import com.example.smartnotes.data.entities.NotasTareas
import com.example.smartnotes.data.repository.NotasTareasRepository
import java.text.NumberFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import com.example.smartnotes.R
import com.example.smartnotes.data.entities.Recordatorios
import com.example.smartnotes.data.repository.ArchivosRepository
import com.example.smartnotes.data.repository.RecordatoriosRepository

/**
 * ViewModel to validate and insert items in the Room database.
 */
class AddNoteTaskViewModel (
    private val repository: NotasTareasRepository,
    private val recordatorioRepository: RecordatoriosRepository,
    private val archivosRepository: ArchivosRepository
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

    // FUNCIÓN NECESARIA: Para actualizar el tipo al abrir la pantalla.
    fun setType(type: String) {
        // 1. Crear un estado base LIMPIO, copiando los valores iniciales.
        // 2. Sobreescribir solo el campo 'tipo' con el valor recibido.
        val newDetails = INITIAL_STATE.copy(tipo = type)

        // 3. Llamar a updateUiState con los detalles limpios.
        updateUiState(newDetails)

        // 4. Limpiar listas de adjuntos/audios
        attachments = emptyList()
        audios = emptyList()

        // 5. Reiniciar el estado de recordatorios (si es necesario)
        reminderDetails = ReminderDetails() // Asume que ReminderDetails() es el estado por defecto.
    }

    suspend fun saveNotaTarea() {
        if (validateInput()) {
            val details = notaTareaUiState.notaTareaDetails.copy(
                fechaRegistro = LocalDateTime.now()
            )

            //Guardar la Nota/Tarea. Asumimos que insertItem devuelve el Long ID.
            val notaTareaId = repository.insertItem(details.toNotaTarea())

            //LÓGICA PARA GUARDAR LOS RECORDATORIOS ASOCIADOS
            recordatoriosList.forEach { reminder ->
                val recordatorioEntity = Recordatorios(
                    // Usa tu entidad de Room
                    tareaId = notaTareaId.toInt(), // Convertir el ID
                    fecha = reminder.fechaMillis,
                    opcion = reminder.opcionResId, // Necesitarás un contexto para stringResource O guardar el String directamente.

                )

                // RecordatorioRepository
                recordatorioRepository.insertItem(recordatorioEntity)
            }

            // Limpiar el estado de recordatorios después de guardar
            recordatoriosList = emptyList()
        }
    }


    // Fecha de cunmplimienmto y recordaotrios =====================================================
    // 1. Manejo del Picker de fecha/hora
    // ESTADOS PARA DATE PICKER
    var showDatePicker by mutableStateOf(false)
        private set
    var showTimePicker by mutableStateOf(false)
        private set

    // --- ESTADOS SIMPLES PARA EL VALOR SELECCIONADO ---
    var selectedDateMillis: Long? by mutableStateOf(null)
        private set
    var selectedHour: Int by mutableStateOf(0)
        private set
    var selectedMinute: Int by mutableStateOf(0)
        private set


    // --- FUNCIONES DE VISIBILIDAD---
    fun setDatePickerVisibility(show: Boolean) {
        showDatePicker = show
    }

    fun setTimePickerVisibility(show: Boolean) {
        showTimePicker = show
    }
    fun updateSelectedDateMillis(millis: Long?) {
        selectedDateMillis = millis
    }


    // --- FUNCIÓN PARA ACTUALIZAR LA FECHA COMPLETA ---
    fun updateDateTimeFromPickers(
        dateMillis: Long?, // Usamos Long? para permitir nulo si el picker lo permite
        hour: Int,
        minute: Int
    ) {
        if (dateMillis == null) return // No hacer nada si no hay fecha

        // 1. Convertir a LocalDateTime
        val newDateTime = convertMillisToLocalDateTime(dateMillis, hour, minute)

        // 2. Actualizar el estado del UI
        updateUiState(notaTareaUiState.notaTareaDetails.copy(
            fechaCumplimiento = newDateTime
        ))

        // Opcional: limpiar los valores temporales si no se van a usar más
        this.selectedDateMillis = null
        this.selectedHour = 0
        this.selectedMinute = 0
    }

    // --- FUNCIÓN DE UTILIDAD DE CONVERSIÓN---
    private fun convertMillisToLocalDateTime(
        dateMillis: Long,
        hour: Int,
        minute: Int
    ): java.time.LocalDateTime {
        // Necesitas convertir Long (milisegundos) a LocalDateTime y agregar hora/minuto.
        // Usamos el sistema de zona horaria por defecto.
        val localDate = java.time.Instant.ofEpochMilli(dateMillis)
            .atZone(java.time.ZoneId.systemDefault())
            .toLocalDate()

        return java.time.LocalDateTime.of(localDate, java.time.LocalTime.of(hour, minute))
    }

    //Propiedad Calculada para mostrar la fecha formateada
    val fechaCumplimientoText: String
        get() = notaTareaUiState.notaTareaDetails.fechaCumplimiento?.let {
            // Usar el patrón de formato deseado
            val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
            it.format(formatter)
        } ?: "--------------"


    //Manejo de Recordatorios (Se puede encapsular en una clase)
    var reminderDetails by mutableStateOf(ReminderDetails())
        private set
    //ESTADO QUE ALMACENA TODOS LOS RECORDATORIOS LISTOS PARA GUARDAR
    var recordatoriosList by mutableStateOf(emptyList<RecordatorioDetails>())
        private set

    fun updateReminderOption(option: Int) {
        reminderDetails = reminderDetails.copy(
            selectedReminderOption = option,
            isReminderExpanded = false
        )
    }

    // FUNCIÓN QUE CALCULA EL TIEMPO REAL DEL RECORDATORIO
    private fun calcularFechaRecordatorio(opcionResId: Int, fechaCumplimiento: LocalDateTime): Long {
        var recordatorioDateTime = fechaCumplimiento

        when (opcionResId) {
            R.string.reminder_5_min -> recordatorioDateTime = fechaCumplimiento.minusMinutes(5)
            R.string.reminder_10_min -> recordatorioDateTime = fechaCumplimiento.minusMinutes(10)
            R.string.reminder_30_min -> recordatorioDateTime = fechaCumplimiento.minusMinutes(30)
            R.string.reminder_1_hour -> recordatorioDateTime = fechaCumplimiento.minusHours(1)
            R.string.reminder_1_day -> recordatorioDateTime = fechaCumplimiento.minusDays(1)
        }

        // Convertir LocalDateTime a milisegundos (Long) para guardar en la BD
        return recordatorioDateTime
            .atZone(java.time.ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    }

    //FUNCIÓN PARA AÑADIR EL RECORDATORIO CONFIGURADO A LA LISTA
    fun addCurrentReminder() {
        val selectedOptionResId = reminderDetails.selectedReminderOption
        val cumplimiento = notaTareaUiState.notaTareaDetails.fechaCumplimiento

        //Validar que la tarea tiene fecha y que no es 'Ninguno'
        if (cumplimiento == null || selectedOptionResId == R.string.reminder_none) {
            return
        }

        // Calcular el tiempo exacto
        val recordatorioMillis = calcularFechaRecordatorio(selectedOptionResId, cumplimiento)

        //Añadir a la lista
        val nuevoRecordatorio = RecordatorioDetails(
            opcionResId = selectedOptionResId,
            fechaMillis = recordatorioMillis
        )
        recordatoriosList = recordatoriosList + nuevoRecordatorio

        //Resetear la selección del dropdown para que el usuario añada otro
        reminderDetails = reminderDetails.copy(selectedReminderOption = R.string.reminder_none)
    }

    fun setReminderExpanded(expanded: Boolean) {
        reminderDetails = reminderDetails.copy(isReminderExpanded = expanded)
    }




    //ARCHIVOS -=---------------------------------------------------------
    // Listas de rutas de archivos
    var attachments by mutableStateOf(emptyList<String>())
        private set
    var audios by mutableStateOf(emptyList<String>())
        private set

    // Funciones para actualizar (se usarán en la UI al seleccionar/tomar un archivo)
    fun addAttachment(path: String) {
        attachments = attachments + path
    }

    fun addAudio(path: String) {
        audios = audios + path
    }
}

/**
 * Represents Ui State for an Item.
 */
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

//para el detalle que se guarda temporalmente en la lista
data class RecordatorioDetails(
    val opcionResId: Int, // R.string del recordatorio (Ej: R.string.reminder_5_min)
    val fechaMillis: Long // La fecha/hora exacta en milisegundos calculada
)


/**
 * Extension function to convert [ItemDetails] to [Item]. If the value of [ItemDetails.price] is
 * not a valid [Double], then the price will be set to 0.0. Similarly if the value of
 * [ItemDetails.quantity] is not a valid [Int], then the quantity will be set to 0
 */
fun NotaTareaDetails.toNotaTarea(): NotasTareas = NotasTareas(
    id = id,
    titulo = titulo,
    descripcion = descripcion,
    tipo = tipo,
    estaCumplida = estaCumplida,
    fechaRegistro = fechaRegistro ?: LocalDateTime.now(),
    fechaCumplimiento = fechaCumplimiento
)

/**
 * Extension function to convert [Item] to [ItemUiState]
 */
fun NotasTareas.toNotaTareaUiState(isEntryValid: Boolean = false): NotaTareaUiState = NotaTareaUiState(
    notaTareaDetails = this.toNotaTareaDetails(),
    isEntryValid = isEntryValid
)

/**
 * Extension function to convert [Item] to [ItemDetails]
 */
fun NotasTareas.toNotaTareaDetails(): NotaTareaDetails = NotaTareaDetails(
    id = id,
    //price = price.toString(),
    //quantity = quantity.toString()
    titulo = titulo,
    descripcion = descripcion,
    tipo = tipo,
    estaCumplida = estaCumplida,
    fechaRegistro = fechaRegistro,
    fechaCumplimiento = fechaCumplimiento
)

private val INITIAL_STATE = NotaTareaDetails(
    // Define aquí los valores predeterminados (título vacío, descripción vacía, etc.)
    titulo = "",
    descripcion = "",
    tipo = "task", // O "note", se sobrescribe en setType
    estaCumplida = false,
    fechaCumplimiento = null,
)

