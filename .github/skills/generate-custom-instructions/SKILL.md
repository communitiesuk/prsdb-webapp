---
name: generate-custom-instructions
description: Use when setting up a new dev environment or worktree that needs instruction files generated. Analyzes the codebase and creates path-specific instruction files in .github/instructions/.
---

# Generate Custom Instructions

Generate path-specific instruction files in `.github/instructions/` by parsing the directory table in
`.github/copilot-instructions.md` and analyzing the codebase for each domain.

## When to use

Trigger this skill when:
- Setting up a new development environment and need instruction files
- A new worktree needs instruction files generated
- The user asks to "generate instructions", "create instruction files", or "set up copilot instructions"
- Instruction files are missing and need to be created from scratch

This skill **creates** instruction files. To **update** existing instruction files, use the `updating-custom-instructions`
skill instead.

## Prerequisites

The main `.github/copilot-instructions.md` file must exist. If it does not:
1. Check if `.github/copilot-instructions.template.md` exists
2. If it does, tell the user to copy it: "Copy `.github/copilot-instructions.template.md` to `.github/copilot-instructions.md` first, then re-run this skill."
3. If the template doesn't exist either, stop and tell the user that the main instructions file is required.

## Process

Follow these steps in order:

### 1. Parse the directory table

Read `.github/copilot-instructions.md` and find the **Path-Specific Instructions** table. For each row, extract:
- **Filename**: the instruction file name (e.g. `journeys.instructions.md`)
- **Applies To**: the directories/domains it covers (e.g. `journeys/`, `forms/`)

Build a list of instruction files to generate.

### 2. Check for existing files

Check which files already exist in `.github/instructions/`. If any exist, ask the user:
- "Overwrite all existing files"
- "Skip existing files (only generate missing ones)"
- "Let me choose which to overwrite"

If the user chooses per-file selection, present the list of existing files and let them pick which to overwrite.

### 3. Determine the applyTo glob pattern

For each instruction file, derive the `applyTo` glob pattern from the "Applies To" column:
- Single directory like `controllers/` → `"**/controllers/**"`
- Multiple directories like `helpers/`, `extensions/`, `converters/` → `"**/helpers/**,**/extensions/**,**/converters/**"`
- Special cases like `src/test/` → `"src/test/**"`

If unsure, look at the directory paths in the actual codebase to confirm the correct glob pattern.

### 4. Generate each instruction file sequentially

For each file in the list (processing one at a time):

#### a. Explore the relevant source directories

Search the codebase directories that the instruction file covers. For each directory, analyze:
- **File structure**: What files and subdirectories exist
- **Annotations**: Custom annotations used (e.g. `@PrsdbController`, `@PrsdbWebService`)
- **Naming conventions**: How files, classes, methods, and variables are named
- **Class hierarchies**: Base classes, interfaces, and inheritance patterns
- **Key abstractions**: Important classes, interfaces, and patterns that define the domain
- **Code examples**: Representative snippets that illustrate the conventions
- **Test patterns**: How the code in this domain is tested (if applicable)
- **Cross-references**: How this domain interacts with other domains (e.g. services → repositories)

#### b. Write the instruction file

Generate the instruction file with the following structure:

```markdown
---
applyTo: "{glob pattern}"
---

# {Domain} Instructions

## Overview

Brief description of what this domain covers and its role in the application.

## {Pattern sections}

Document the key patterns and conventions found in the codebase.
Include code examples using fenced code blocks with language identifiers.
Use tables for reference material (e.g. listing all annotations, all base classes).

## {Testing section (if applicable)}

How code in this domain is tested. Include example test structure.
```

#### c. Write the file

Save the generated content to `.github/instructions/{filename}`.

### 5. Summary

After all files are generated, present a summary:
- How many files were generated
- How many were skipped (if any)
- Any files that could not be generated (with reasons)

## Guidelines for generated content

- **Be precise**: Only document patterns that are consistently used across the codebase, not one-off implementations.
- **Use real examples**: Reference actual class names, annotations, and file paths from the codebase.
- **Keep it concise**: Instruction files should be a quick reference, not exhaustive documentation. Aim for 60–120 lines.
- **Include code examples**: Every significant pattern should have at least one code example.
- **Focus on conventions**: Document what a developer (or AI) needs to follow when working in that area — naming, structure,
  annotations, testing approach.
- **Note cross-references**: When a domain depends on or interacts with another domain, mention it briefly.
- **Don't duplicate the main file**: Path-specific files cover patterns unique to their directory. General project
  conventions belong in `copilot-instructions.md`.

## Instruction file format

Each instruction file must follow this format:

```markdown
---
applyTo: "{glob pattern}"
---

# {Title} Instructions

## Overview

{Brief description}

## {Section}

{Content with code examples}
```

The `applyTo` field is a glob pattern that tells Copilot when to apply the instructions. Use `**` to match across
directory levels. Multiple patterns can be comma-separated.
