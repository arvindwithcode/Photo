package com.example.data

import kotlinx.coroutines.flow.Flow

class ImageRepository(private val imageDao: ImageDao) {
    val allImages: Flow<List<ImageItem>> = imageDao.getAllImages()

    fun searchImages(query: String): Flow<List<ImageItem>> {
        return if (query.isEmpty()) {
            imageDao.getAllImages()
        } else {
            imageDao.searchImages("%$query%")
        }
    }

    suspend fun insertImage(image: ImageItem) {
        imageDao.insertImage(image)
    }

    suspend fun deleteImageById(id: Int) {
        imageDao.deleteImageById(id)
    }

    suspend fun getImageCount(): Int {
        return imageDao.getImageCount()
    }
}
