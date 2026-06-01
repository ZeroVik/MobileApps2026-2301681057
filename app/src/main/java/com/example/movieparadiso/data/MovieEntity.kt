package com.example.movieparadiso.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "movies")
data class MovieEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val title: String,
    val director: String,
    val genre: String,
    val year: String,
    val status: String,
    val rating: Float,
    val notes: String,
    val isFavorite: Boolean,
    val videoUri: String?,
    val createdAt: Long = System.currentTimeMillis()
)