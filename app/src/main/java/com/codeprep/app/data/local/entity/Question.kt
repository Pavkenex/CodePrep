package com.codeprep.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "questions")
data class Question(
    @PrimaryKey val id: String,
    val text: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String,
    val codeSnippet: String? = null
)
