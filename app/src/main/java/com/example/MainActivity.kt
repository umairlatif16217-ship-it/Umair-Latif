package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.VideoEditorScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.EditorViewModel

enum class AppScreen {
    HOME, EDITOR
}

class MainActivity : ComponentActivity() {

    private val viewModel: EditorViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                var currentScreen by remember { mutableStateOf(AppScreen.HOME) }

                when (currentScreen) {
                    AppScreen.HOME -> {
                        HomeScreen(
                            viewModel = viewModel,
                            onOpenEditor = { currentScreen = AppScreen.EDITOR },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    AppScreen.EDITOR -> {
                        VideoEditorScreen(
                            viewModel = viewModel,
                            onNavigateBack = { currentScreen = AppScreen.HOME },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}
