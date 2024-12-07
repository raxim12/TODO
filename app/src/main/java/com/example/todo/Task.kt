package com.example.todo

data class Task(
    val id: String = "", // Firestore'dagi hujjat ID
    val title: String = "", // Vazifa nomi
    val time: String = "", // Vazifa bajarilishi kerak bo'lgan vaqt
    val completed: Boolean = false, // Vazifa bajarilganmi
    val date: String = "" // Qaysi kun uchun
)
