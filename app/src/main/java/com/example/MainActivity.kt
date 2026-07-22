package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.database.AppDatabase
import com.example.data.repository.ObserverRepository
import com.example.ui.screens.MainScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.ObserverViewModel
import com.example.viewmodel.ObserverViewModelFactory

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    // Initialize Room Database and Repository
    val database = AppDatabase.getDatabase(applicationContext)
    val repository = ObserverRepository(
      observationDao = database.observationDao(),
      stickyNoteDao = database.stickyNoteDao(),
      taskItemDao = database.taskItemDao(),
      workspaceDao = database.workspaceDao()
    )

    // Instantiate ViewModel using the Factory
    val viewModelFactory = ObserverViewModelFactory(repository, applicationContext)
    val viewModel = ViewModelProvider(this, viewModelFactory)[ObserverViewModel::class.java]

    setContent {
      val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
      val isDarkTheme = when (themeMode) {
        "dark" -> true
        "light" -> false
        else -> isSystemInDarkTheme()
      }
      MyApplicationTheme(darkTheme = isDarkTheme) {
        MainScreen(viewModel = viewModel)
      }
    }
  }
}

