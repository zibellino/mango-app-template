package com.mangocodex.app

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val BG = Color(0xFF1E1E1E)
val LINE_NUM = Color(0xFF858585)
val FG = Color(0xFFD4D4D4)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(viewModel: EditorViewModel) {
    val context = LocalContext.current
    val lines by viewModel.lines.collectAsState()
    val highlighted by viewModel.highlightedLines.collectAsState()
    val isDirty by viewModel.isDirty.collectAsState()
    val currentUri by viewModel.currentFileUri.collectAsState()

    var showMenu by remember { mutableStateOf(false) }
    var isPatternFile by remember { mutableStateOf(false) }

    val openLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? -> uri?.let { viewModel.openFile(context, it); isPatternFile = false } }

    val saveLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/plain")
    ) { uri: Uri? -> uri?.let { viewModel.saveFile(context, it) } }

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
                                    openLauncher.launch(arrayOf("text/*"))
                                    showMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Save") },
                                onClick = {
                                    if (isPatternFile) viewModel.saveInternalPatterns(context)
                                    else currentUri?.let { viewModel.saveFile(context, it) }
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
                                    isPatternFile = true
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(BG)
        ) {
            itemsIndexed(lines) { index, line ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    // Line number
                    Text(
                        text = "%4d".format(index + 1),
                        color = LINE_NUM,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )

                    // Local cursor state per line
                    val annotated = highlighted.getOrElse(index) { androidx.compose.ui.text.AnnotatedString(line) }
                    var fieldValue by remember(index) { mutableStateOf(TextFieldValue(annotated)) }

                    BasicTextField(
                        value = fieldValue,
                        onValueChange = { newVal ->
                            if (!newVal.text.contains('\n')) {
                                fieldValue = newVal
                                viewModel.updateLine(index, newVal.text)
                            }
                        },
                        textStyle = TextStyle(
                            color = FG,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp,
                            lineHeight = 20.sp
                        ),
                        cursorBrush = SolidColor(FG),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                    )
                }
            }
        }
    }
}
