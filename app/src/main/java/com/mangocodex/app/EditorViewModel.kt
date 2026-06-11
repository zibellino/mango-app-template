package com.mangocodex.app

import android.content.Context
import android.net.Uri
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

const val PATTERNS_INTERNAL_PATH = "patterns.csv"

class EditorViewModel : ViewModel() {

    private var lexer: Lexer = Lexer(emptyList())

    // Single source of truth: full text as TextFieldValue (cursor, selection included)
    private val _fieldValue = MutableStateFlow(TextFieldValue(""))
    val fieldValue: StateFlow<TextFieldValue> = _fieldValue

    private val _highlighted = MutableStateFlow(AnnotatedString(""))
    val highlighted: StateFlow<AnnotatedString> = _highlighted

    private val _currentFileUri = MutableStateFlow<Uri?>(null)
    val currentFileUri: StateFlow<Uri?> = _currentFileUri

    private val _isDirty = MutableStateFlow(false)
    val isDirty: StateFlow<Boolean> = _isDirty

    private var isPatternFile = false

    fun loadPatterns(context: Context) {
        val csv = loadPatternsFromInternal(context)
            ?: context.assets.open(PATTERNS_INTERNAL_PATH).bufferedReader().readText()
        lexer = Lexer.fromCsv(csv)
        rehighlight()
    }

    fun reloadPatterns(context: Context) = loadPatterns(context)

    private fun loadPatternsFromInternal(context: Context): String? {
        val file = context.getFileStreamPath(PATTERNS_INTERNAL_PATH)
        return if (file.exists()) file.readText() else null
    }

    fun openFile(context: Context, uri: Uri) {
        val text = context.contentResolver.openInputStream(uri)
            ?.bufferedReader()?.readText() ?: return
        _currentFileUri.value = uri
        isPatternFile = false
        setText(text)
        _isDirty.value = false
    }

    fun openInternalPatterns(context: Context) {
        val file = context.getFileStreamPath(PATTERNS_INTERNAL_PATH)
        if (!file.exists()) {
            val default = context.assets.open(PATTERNS_INTERNAL_PATH).bufferedReader().readText()
            context.openFileOutput(PATTERNS_INTERNAL_PATH, Context.MODE_PRIVATE).writer().use {
                it.write(default)
            }
        }
        _currentFileUri.value = null
        isPatternFile = true
        setText(file.readText())
        _isDirty.value = false
    }

    fun saveFile(context: Context, uri: Uri? = _currentFileUri.value) {
        if (isPatternFile) { saveInternalPatterns(context); return }
        uri ?: return
        context.contentResolver.openOutputStream(uri, "wt")?.writer()?.use {
            it.write(_fieldValue.value.text)
        }
        _isDirty.value = false
    }

    fun saveAs(context: Context, uri: Uri) {
        context.contentResolver.openOutputStream(uri, "wt")?.writer()?.use {
            it.write(_fieldValue.value.text)
        }
        _currentFileUri.value = uri
        isPatternFile = false
        _isDirty.value = false
    }

    fun saveInternalPatterns(context: Context) {
        context.openFileOutput(PATTERNS_INTERNAL_PATH, Context.MODE_PRIVATE).writer().use {
            it.write(_fieldValue.value.text)
        }
        _isDirty.value = false
        reloadPatterns(context)
    }

    fun onValueChange(newVal: TextFieldValue) {
        val textChanged = newVal.text != _fieldValue.value.text
        _fieldValue.value = newVal
        if (textChanged) {
            _isDirty.value = true
            rehighlight()
        }
    }

    private fun setText(text: String) {
        _fieldValue.value = TextFieldValue(text)
        rehighlight()
    }

    private fun rehighlight() {
        val text = _fieldValue.value.text
        val lines = text.split("\n")
        _highlighted.value = buildAnnotatedString {
            append(text)
            var offset = 0
            for (line in lines) {
                val tokens = lexer.tokenize(line)
                for (token in tokens) {
                    addStyle(
                        SpanStyle(color = token.color),
                        offset + token.start,
                        (offset + token.end).coerceAtMost(offset + line.length)
                    )
                }
                offset += line.length + 1 // +1 for \n
            }
        }
    }
}
