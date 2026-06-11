package com.mangocodex.app

import android.content.Context
import android.net.Uri
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

const val PATTERNS_INTERNAL_PATH = "patterns.csv"

class EditorViewModel : ViewModel() {

    private var lexer: Lexer = Lexer(emptyList())

    private val _lines = MutableStateFlow<List<String>>(listOf(""))
    val lines: StateFlow<List<String>> = _lines

    private val _highlightedLines = MutableStateFlow<List<AnnotatedString>>(listOf(AnnotatedString("")))
    val highlightedLines: StateFlow<List<AnnotatedString>> = _highlightedLines

    private val _currentFileUri = MutableStateFlow<Uri?>(null)
    val currentFileUri: StateFlow<Uri?> = _currentFileUri

    private val _isDirty = MutableStateFlow(false)
    val isDirty: StateFlow<Boolean> = _isDirty

    fun loadPatterns(context: Context) {
        val csv = loadPatternsFromInternal(context)
            ?: context.assets.open(PATTERNS_INTERNAL_PATH).bufferedReader().readText()
        lexer = Lexer.fromCsv(csv)
        rehighlightAll()
    }

    fun reloadPatterns(context: Context) {
        loadPatterns(context)
    }

    private fun loadPatternsFromInternal(context: Context): String? {
        val file = context.getFileStreamPath(PATTERNS_INTERNAL_PATH)
        return if (file.exists()) file.readText() else null
    }

    fun openFile(context: Context, uri: Uri) {
        val text = context.contentResolver.openInputStream(uri)
            ?.bufferedReader()?.readText() ?: return
        _currentFileUri.value = uri
        setContent(text)
        _isDirty.value = false
    }

    fun openInternalPatterns(context: Context) {
        // Ensure internal patterns file exists (copy from assets if not)
        val file = context.getFileStreamPath(PATTERNS_INTERNAL_PATH)
        if (!file.exists()) {
            val default = context.assets.open(PATTERNS_INTERNAL_PATH).bufferedReader().readText()
            context.openFileOutput(PATTERNS_INTERNAL_PATH, Context.MODE_PRIVATE).writer().use {
                it.write(default)
            }
        }
        setContent(file.readText())
        _currentFileUri.value = null // virtual path, not a real URI
        _isDirty.value = false
    }

    fun saveFile(context: Context, uri: Uri? = _currentFileUri.value) {
        uri ?: return
        val text = _lines.value.joinToString("\n")
        context.contentResolver.openOutputStream(uri, "wt")?.writer()?.use {
            it.write(text)
        }
        _isDirty.value = false
    }

    fun saveInternalPatterns(context: Context) {
        val text = _lines.value.joinToString("\n")
        context.openFileOutput(PATTERNS_INTERNAL_PATH, Context.MODE_PRIVATE).writer().use {
            it.write(text)
        }
        _isDirty.value = false
        reloadPatterns(context)
    }

    fun updateLine(index: Int, newText: String) {
        val current = _lines.value.toMutableList()

        // Handle newlines inserted by soft keyboard
        if ('\n' in newText) {
            val parts = newText.split("\n")
            current[index] = parts[0]
            parts.drop(1).forEachIndexed { i, part ->
                current.add(index + 1 + i, part)
            }
        } else {
            current[index] = newText
        }

        _lines.value = current
        _isDirty.value = true
        rehighlightLine(index)
    }

    fun deleteLine(index: Int) {
        if (_lines.value.size <= 1) return
        val current = _lines.value.toMutableList()
        current.removeAt(index)
        _lines.value = current
        _isDirty.value = true
        rehighlightAll()
    }

    private fun setContent(text: String) {
        _lines.value = text.split("\n").ifEmpty { listOf("") }
        rehighlightAll()
    }

    private fun rehighlightAll() {
        _highlightedLines.value = _lines.value.map { highlight(it) }
    }

    private fun rehighlightLine(index: Int) {
        val updated = _highlightedLines.value.toMutableList()
        if (index < updated.size) {
            updated[index] = highlight(_lines.value[index])
            _highlightedLines.value = updated
        }
    }

    private fun highlight(line: String): AnnotatedString {
        val tokens = lexer.tokenize(line)
        return buildAnnotatedString {
            append(line)
            for (token in tokens) {
                addStyle(
                    SpanStyle(color = token.color),
                    token.start,
                    token.end.coerceAtMost(line.length)
                )
            }
        }
    }
}
