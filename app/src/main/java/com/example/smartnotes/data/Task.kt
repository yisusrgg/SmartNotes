package com.example.smartnotes.data

import java.util.*

data class Task(
    val id: String = UUID.randomUUID().toString(),
    var title: String,
    var dateTimeText: String, // "Hoy, 10 AM" o "20/10/2023, 2"
    var completed: Boolean = false,
    var hasAttachment: Boolean = false,
    var hasAudio: Boolean = false
)
