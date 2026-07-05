package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "uploaded_images")
data class ImageItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val filePath: String,
    val uploadDate: String,
    val sizeInBytes: Long,
    val width: Int,
    val height: Int
)
