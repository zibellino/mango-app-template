package com.mangocodex

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val BG = Color(0xFF1E1E1E)
val FG = Color(0xFFD4D4D4)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(viewModel: EditorViewModel) {
    val context = LocalContext.current
    val fieldValue by viewModel.fieldValue.collectAsState()
    val highlighted by viewModel.highlighted.collectAsState()
    val isDirty by viewModel.isDirty.collectAsState()
    val currentUri by viewModel.currentFileUri.collectAsState()

    var showMenu by remember { mutableStateOf(false) }

    val openLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? -> uri?.let { viewModel.openFile(context, it) } }

    val saveLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/plain")
    ) { uri: Uri? -> uri?.let { viewModel.saveAs(context, it) } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isDirty) "MangoCodex •" else "MangoCodex",
                        color = FG,
                        fontFamily = FontFamily.Monospace
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF252526)),
                actions = {
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Text("⋮", color = FG, fontSize = 20.sp)
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Open file") },
                                onClick = {
                                    openLauncher.launch(arrayOf("*/*"))
                                    showMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Save") },
                                onClick = {
                                    viewModel.saveFile(context)
                                    showMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Save as…") },
                                onClick = {
                                    saveLauncher.launch("untitled.txt")
                                    showMenu = false
                                }
                            )
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text("Edit patterns") },
                                onClick = {
                                    viewModel.openInternalPatterns(context)
                                    showMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Reload patterns") },
                                onClick = {
                                    viewModel.reloadPatterns(context)
                                    showMenu = false
                                }
                            )
                        }
                    }
                }
            )
        },
        containerColor = BG
    ) { padding ->
        // Merge cursor/selection from fieldValue with highlighting from VM
        val displayValue = fieldValue.copy(annotatedString = highlighted)

        BasicTextField(
            value = displayValue,
            onValueChange = { viewModel.onValueChange(it) },
            textStyle = TextStyle(
                color = FG,
                fontFamily = FontFamily.Monospace,
                fontSize = 13.sp,
                lineHeight = 20.sp
            ),
            cursorBrush = SolidColor(FG),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(BG)
                .padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
