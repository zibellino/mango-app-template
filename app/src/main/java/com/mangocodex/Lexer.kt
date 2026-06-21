package com.mangocodex

import androidx.compose.ui.graphics.Color

data class LexerRule(
    val name: String,
    val color: Color,
    val regex: Regex
)

data class Token(
    val start: Int,
    val end: Int,
    val color: Color
)

class Lexer(private val rules: List<LexerRule>) {

    fun tokenize(line: String): List<Token> {
        val tokens = mutableListOf<Token>()

        for (rule in rules) {
            for (match in rule.regex.findAll(line)) {
                val range = match.range
                // Skip if this range overlaps any already claimed token
                if (tokens.none { it.start < range.last + 1 && it.end > range.first }) {
                    tokens.add(Token(range.first, range.last + 1, rule.color))
                }
            }
        }

        // Merging matters for render cost, not correctness: each distinct
        // styled run is a separate paint/layout run downstream. Two adjacent
        // tokens of the same color (e.g. a sign token butting up against a
        // number token) cost as much to paint as one twice the length, so
        // collapsing them directly cuts the per-frame work during scroll.
        return mergeAdjacentSameColor(tokens.sortedBy { it.start })
    }

    private fun mergeAdjacentSameColor(tokens: List<Token>): List<Token> {
        if (tokens.isEmpty()) return tokens
        val merged = mutableListOf<Token>()
        var current = tokens[0]
        for (i in 1 until tokens.size) {
            val next = tokens[i]
            if (next.start == current.end && next.color == current.color) {
                current = current.copy(end = next.end)
            } else {
                merged.add(current)
                current = next
            }
        }
        merged.add(current)
        return merged
    }

    companion object {
        fun fromCsv(csv: String): Lexer {
            val rules = csv.lines()
                .drop(1) // skip header
                .filter { it.isNotBlank() }
                .mapNotNull { line ->
                    val parts = line.split(",", limit = 3)
                    if (parts.size < 3) return@mapNotNull null
                    val name = parts[0].trim()
                    val colorHex = parts[1].trim()
                    val pattern = parts[2].trim()
                    try {
                        val color = parseColor(colorHex)
                        val regex = Regex(pattern)
                        LexerRule(name, color, regex)
                    } catch (e: Exception) {
                        null // skip malformed rules
                    }
                }
            return Lexer(rules)
        }

        private fun parseColor(hex: String): Color {
            val clean = hex.trimStart('#')
            val value = clean.toLong(16)
            return when (clean.length) {
                6 -> Color(((0xFF shl 24) or value.toInt()))
                8 -> Color(value.toInt())
                else -> Color.White
            }
        }
    }
}
