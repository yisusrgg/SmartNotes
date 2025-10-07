package com.example.smartnotes.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object ItemRepository {
    private val _items = MutableStateFlow<List<Item>>(sampleItems())
    val items: StateFlow<List<Item>> = _items

    private fun sampleItems() = listOf(
        Item.Task(
            title = "Completar informe mensual",
            description = "Revisar datos financieros, redactar conclusiones y enviar a gerente antes de la fecha hoy",
            dateTimeText = "Ayer, 3 PM",
            attachments = listOf("file:///android_asset/sample_image.jpg") // Simulado
        ),
        Item.Task(
            title = "Presentación cliente nuevo",
            description = "Preparar slides y demo",
            dateTimeText = "Hoy, 10 AM",
            attachments = listOf("file:///android_asset/slide.png")
        ),
        Item.Note(
            title = "Comprar suministros oficina",
            description = "Papel, tinta y cables nuevos para el equipo"
        ),
        Item.Task(
            title = "Investigar nuevas tecnologías",
            description = "Leer artículos sobre IA y blockchain",
            dateTimeText = "20/10/2023, 2",
            audios = listOf("file:///android_asset/sample_audio.mp3")
        ),
        Item.Note(
            title = "Idea para proyecto",
            description = "Integrar chatbots en la app de notas",
            attachments = listOf("file:///android_asset/idea_doc.pdf")
        )
    )

    fun addItem(item: Item) {
        _items.value = _items.value + item
    }

    fun updateItem(item: Item) {
        _items.value = _items.value.map { if (it.id == item.id) item else it }
    }

    fun removeItem(itemId: String) {
        _items.value = _items.value.filterNot { it.id == itemId }
    }

    fun getItemById(id: String): Item? = _items.value.find { it.id == id }
}