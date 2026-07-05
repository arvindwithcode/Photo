package com.example.ui

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.ImageItem
import com.example.data.ImageRepository
import com.example.data.SampleImageGenerator
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ImageSharingViewModel(
    private val context: Context,
    private val repository: ImageRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _uploadStatus = MutableStateFlow<UploadStatus>(UploadStatus.Idle)
    val uploadStatus: StateFlow<UploadStatus> = _uploadStatus.asStateFlow()

    init {
        // Preload sample images if database is empty
        viewModelScope.launch {
            SampleImageGenerator.preloadIfNeeded(context, repository)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val imageList: StateFlow<List<ImageItem>> = _searchQuery
        .flatMapLatest { query ->
            repository.searchImages(query)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun onSearchQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun verifyPassword(password: String): Boolean {
        return password == "xarvind07"
    }

    fun uploadImage(title: String, uri: Uri) {
        viewModelScope.launch {
            _uploadStatus.value = UploadStatus.Loading
            try {
                val contentResolver = context.contentResolver
                
                // 1. Get original file size and verify under 10MB
                var sizeInBytes = 0L
                contentResolver.openAssetFileDescriptor(uri, "r")?.use { fd ->
                    sizeInBytes = fd.length
                }

                if (sizeInBytes > 10 * 1024 * 1024) {
                    _uploadStatus.value = UploadStatus.Error("File is too large. Max size is 10MB.")
                    return@launch
                }

                // 2. Decode bounds to get width/height
                var width = 800
                var height = 800
                contentResolver.openInputStream(uri)?.use { inputStream ->
                    val options = BitmapFactory.Options().apply {
                        inJustDecodeBounds = true
                    }
                    BitmapFactory.decodeStream(inputStream, null, options)
                    if (options.outWidth > 0 && options.outHeight > 0) {
                        width = options.outWidth
                        height = options.outHeight
                    }
                }

                // 3. Copy image to "uploads" directory
                val uploadsDir = File(context.filesDir, "uploads")
                if (!uploadsDir.exists()) {
                    uploadsDir.mkdirs()
                }

                val ext = getFileExtension(uri) ?: "png"
                val destFile = File(uploadsDir, "upload_${System.currentTimeMillis()}.$ext")

                contentResolver.openInputStream(uri)?.use { input ->
                    FileOutputStream(destFile).use { output ->
                        input.copyTo(output)
                    }
                }

                if (sizeInBytes <= 0) {
                    sizeInBytes = destFile.length()
                }

                // 4. Save metadata to database
                val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                val uploadDateStr = dateFormat.format(Date())

                val newImage = ImageItem(
                    title = title.ifBlank { "Untitled Image" },
                    filePath = destFile.absolutePath,
                    uploadDate = uploadDateStr,
                    sizeInBytes = sizeInBytes,
                    width = width,
                    height = height
                )

                repository.insertImage(newImage)
                _uploadStatus.value = UploadStatus.Success
                Log.d("ViewModel", "Successfully saved uploaded image to: ${destFile.absolutePath}")
            } catch (e: Exception) {
                Log.e("ViewModel", "Upload error", e)
                _uploadStatus.value = UploadStatus.Error("Failed to upload image: ${e.localizedMessage}")
            }
        }
    }

    fun deleteImage(image: ImageItem) {
        viewModelScope.launch {
            try {
                // Delete actual local file
                val file = File(image.filePath)
                if (file.exists()) {
                    file.delete()
                }
                // Delete from DB
                repository.deleteImageById(image.id)
            } catch (e: Exception) {
                Log.e("ViewModel", "Delete error", e)
            }
        }
    }

    fun clearUploadStatus() {
        _uploadStatus.value = UploadStatus.Idle
    }

    private fun getFileExtension(uri: Uri): String? {
        return context.contentResolver.getType(uri)?.let { mimeType ->
            when {
                mimeType.contains("jpeg", true) || mimeType.contains("jpg", true) -> "jpg"
                mimeType.contains("png", true) -> "png"
                mimeType.contains("webp", true) -> "webp"
                else -> null
            }
        }
    }
}

sealed class UploadStatus {
    object Idle : UploadStatus()
    object Loading : UploadStatus()
    object Success : UploadStatus()
    data class Error(val message: String) : UploadStatus()
}

class ImageSharingViewModelFactory(
    private val context: Context,
    private val repository: ImageRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ImageSharingViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ImageSharingViewModel(context, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
