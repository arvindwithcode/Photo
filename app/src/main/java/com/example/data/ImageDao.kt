package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ImageDao {
    @Query("SELECT * FROM uploaded_images ORDER BY id DESC")
    fun getAllImages(): Flow<List<ImageItem>>

    @Query("SELECT * FROM uploaded_images WHERE title LIKE :searchQuery ORDER BY id DESC")
    fun searchImages(searchQuery: String): Flow<List<ImageItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertImage(image: ImageItem)

    @Query("DELETE FROM uploaded_images WHERE id = :id")
    suspend fun deleteImageById(id: Int)

    @Query("SELECT COUNT(*) FROM uploaded_images")
    suspend fun getImageCount(): Int
}
