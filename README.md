# MangoCodex

A lightweight Android text editor with regex-based syntax highlighting.

## Features
- Line-by-line regex highlighting — fast, stateless, per-line
- Fully configurable pattern set via a plain CSV file
- Edit patterns directly inside the app (`⋮ → Edit patterns`)
- Hot reload patterns without restarting (`⋮ → Reload patterns`)
- Open any text file, save, save as
- Opens files from file managers via intent

## Pattern format

Patterns live in a CSV file (`patterns.csv`) with three columns:

```
name,color,pattern
comment_line,#6A9955,^\s*//.*
string,#CE9178,"[^"]*"
keyword,#569CD6,\b(if|else|for|return)\b
```

- **name** — identifier, used for readability
- **color** — hex color (`#RRGGBB`)
- **pattern** — Java/Kotlin regex applied per line

Rules are applied in order. First match on any character range wins (no overlaps).

## Customizing patterns

Open the pattern file from within the app, edit, save. Changes take effect immediately via reload.
To back up or share your pattern set, use Save as. To load a new one, open it and save it over the internal file.

## Building

```
gradle assembleDebug
```

Requires JDK 17 and Android SDK. CI via GitHub Actions on every push and release.
