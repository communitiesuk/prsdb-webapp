---
name: making-code-edits
description: Use when searching, reading, or editing code files. Prefers JetBrains MCP tools for IDE-aware operations with semantic understanding, automatic formatting, build verification, and LF line ending enforcement.
---

# Making Code Edits

Instructions for searching, reading, and editing code using JetBrains MCP tools.

## ALWAYS Use JetBrains MCP for Code

**JetBrains MCP tools are the default and preferred approach for ALL code operations
in this repository.** Do not use CLI tools (`grep`, `glob`, `view`, `edit`) for code
files when the JetBrains MCP server is available. JetBrains tools understand Kotlin
semantics, respect project settings, and produce correct formatting automatically.

## Searching and Reading

| Task | Tool |
|------|------|
| Find a symbol by name | `jetbrains-search_symbol` — semantic, understands overloads and extensions |
| Search file contents by text | `jetbrains-search_text` or `jetbrains-search_in_files_by_text` |
| Search file contents by regex | `jetbrains-search_regex` or `jetbrains-search_in_files_by_regex` |
| Find files by name | `jetbrains-find_files_by_name_keyword` — indexed, fast |
| Find files by glob pattern | `jetbrains-search_file` or `jetbrains-find_files_by_glob` |
| Read file content | `jetbrains-read_file` — supports line ranges, JARs, decompiled classes |
| Understand a symbol | `jetbrains-get_symbol_info` — type, docs, declaration location |
| Browse directory structure | `jetbrains-list_directory_tree` |

## Editing

| Task | Tool |
|------|------|
| Replace text in a file | `jetbrains-replace_text_in_file` — auto-saves, respects `.editorconfig` |
| Rename a symbol | `jetbrains-rename_refactoring` — updates all references across project |
| Reformat after edits | `jetbrains-reformat_file` — applies project code style rules |
| Create a new file | `jetbrains-create_new_file` — creates parent directories automatically |

## Edit Workflow

1. **Make the edit** — use `jetbrains-replace_text_in_file` with the exact text to
   replace and the new text. Set `caseSensitive: true` and use enough surrounding
   context to ensure a unique match.

2. **Reformat** — run `jetbrains-reformat_file` on the modified file to ensure code
   style compliance (indentation, spacing, imports).

3. **Build** — run `jetbrains-build_project` to verify the edit compiles. Check the
   output for errors.

4. **Inspect** — run `jetbrains-get_file_problems` to catch warnings or issues the
   build alone might not surface.

## CLI Fallback

Use CLI tools only when:
- The JetBrains MCP server is unavailable and the user cannot start it when prompted
- The file is outside the repository (e.g. session workspace files, system config)

CLI equivalents (Copilot built-in / PowerShell):

| JetBrains MCP | Copilot built-in | PowerShell |
|---------------|-----------------|------------|
| `jetbrains-search_text` | `grep` | `Select-String` |
| `jetbrains-find_files_by_name_keyword` | `glob` | `Get-ChildItem` |
| `jetbrains-read_file` | `view` | `Get-Content` |
| `jetbrains-replace_text_in_file` | `edit` | n/a |
| `jetbrains-create_new_file` | `create` | `Set-Content` |

When falling back to CLI tools on Windows, convert code files to LF before committing:

```powershell
(Get-Content .\path\to\file.kt -Raw) -replace "`r`n", "`n" | Set-Content -NoNewline .\path\to\file.kt
```

```bash
sed -i 's/\r$//' path/to/file.kt
```

JetBrains MCP tools handle line endings automatically via `.editorconfig`.

## Kotlin-Specific Tips

- Use `jetbrains-rename_refactoring` for renaming classes, methods, or variables —
  it handles companion objects, extension functions, named arguments, etc.
- Use `jetbrains-get_symbol_info` to understand a symbol before modifying it
- Use `jetbrains-search_symbol` over grep for Kotlin symbols (understands overloads)

## Project Path

Always pass `projectPath` to JetBrains MCP tools. When working in a worktree, this
is the worktree path (e.g. `C:\Work\Projects\MHCLG\pdjb-819`), NOT the main
repository path.

Incorrect:
```
projectPath: "C:\Work\Projects\MHCLG\prsdb-webapp"
```

Correct:
```
projectPath: "C:\Work\Projects\MHCLG\pdjb-819"
```
