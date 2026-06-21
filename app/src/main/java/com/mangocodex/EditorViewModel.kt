package com.mangocodex

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

    // Per-line tokenization cache used by rehighlight() to avoid re-running
    // every regex rule against every line on every keystroke. Reset whenever
    // the lexer's rule set changes, since cached tokens would otherwise be
    // stale relative to the new rules.
    private var prevLines: List<String> = emptyList()
    private var prevTokens: List<List<Token>> = emptyList()

    fun loadPatterns(context: Context) {
        val csv = loadPatternsFromInternal(context)
            ?: context.assets.open(PATTERNS_INTERNAL_PATH).bufferedReader().readText()
        lexer = Lexer.fromCsv(csv)
        invalidateTokenCache()
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
        invalidateTokenCache()
        rehighlight()
    }

    private fun invalidateTokenCache() {
        prevLines = emptyList()
        prevTokens = emptyList()
    }

    private fun rehighlight() {
        val text = _fieldValue.value.text
        val lines = text.split("\n")
        val tokensPerLine = tokenizeIncremental(lines)

        _highlighted.value = buildAnnotatedString {
            append(text)
            var offset = 0
            for (i in lines.indices) {
                val line = lines[i]
                val tokens = tokensPerLine[i]
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

        prevLines = lines
        prevTokens = tokensPerLine
    }

    /**
     * Re-tokenizes only the lines that changed since the last call, reusing
     * cached tokens for everything else. Finds the common prefix and suffix
     * (by content, not index) between the old and new line lists, so a
     * single inserted/deleted line doesn't invalidate the whole cache.
     */
    private fun tokenizeIncremental(lines: List<String>): List<List<Token>> {
        val oldLines = prevLines
        val oldTokens = prevTokens

        val minSize = minOf(lines.size, oldLines.size)

        var prefixLen = 0
        while (prefixLen < minSize && lines[prefixLen] == oldLines[prefixLen]) {
            prefixLen++
        }

        val maxSuffix = minSize - prefixLen
        var suffixLen = 0
        while (suffixLen < maxSuffix &&
            lines[lines.size - 1 - suffixLen] == oldLines[oldLines.size - 1 - suffixLen]
        ) {
            suffixLen++
        }

        val result = ArrayList<List<Token>>(lines.size)

        for (i in 0 until prefixLen) {
            result.add(oldTokens[i])
        }

        val middleEnd = lines.size - suffixLen
        for (i in prefixLen until middleEnd) {
            result.add(lexer.tokenize(lines[i]))
        }

        for (i in 0 until suffixLen) {
            val oldIndex = oldLines.size - suffixLen + i
            result.add(oldTokens[oldIndex])
        }

        return result
    }
}
