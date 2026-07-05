package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import com.example.data.AppDatabase
import com.example.data.ImageRepository
import com.example.ui.ImageSharingApp
import com.example.ui.ImageSharingViewModel
import com.example.ui.ImageSharingViewModelFactory
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize Room Database, DAO, Repository and ViewModel
        val database = AppDatabase.getDatabase(applicationContext)
        val imageDao = database.imageDao()
        val repository = ImageRepository(imageDao)
        val factory = ImageSharingViewModelFactory(applicationContext, repository)
        val viewModel = ViewModelProvider(this, factory)[ImageSharingViewModel::class.java]

        setContent {
            MyApplicationTheme {
                ImageSharingApp(viewModel = viewModel)
            }
        }
    }
}
