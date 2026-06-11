package com.mangocodex.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Color

class MainActivity : ComponentActivity() {

    private val viewModel: EditorViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.loadPatterns(this)

        // Handle files opened via intent (e.g. from a file manager)
        intent?.data?.let { uri ->
            viewModel.openFile(this, uri)
        }

        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    background = Color(0xFF1E1E1E),
                    surface = Color(0xFF252526),
                    onBackground = Color(0xFFD4D4D4),
                    onSurface = Color(0xFFD4D4D4),
                )
            ) {
                EditorScreen(viewModel)
            }
        }
    }
}
