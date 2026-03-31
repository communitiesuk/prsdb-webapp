---
name: updating-custom-instructions
description: Use when asked to update the instructions, refresh custom instructions, or sync instruction files with the codebase. Reviews all instruction files against the actual code and proposes updates.
---

# Updating Custom Instructions

Update all path-specific instruction files in `.github/instructions/` and the directory table in `.github/copilot-instructions.md`
to reflect the current state of the codebase.

## When to use

Trigger this skill when the user asks to "update the instructions", "refresh the instructions", "sync the instruction files",
or similar requests.

## Process

Follow these steps in order:

### 1. Read the current state

- Read `.github/copilot-instructions.md` and note the directory mapping table.
- Read every instruction file in `.github/instructions/`. For each file, note:
  - The `applyTo` glob pattern from the frontmatter
  - The conventions, patterns, and examples it documents

### 2. Explore the codebase

For each instruction file, explore the directories it covers. Look for:
- **New patterns** not yet documented (new annotations, base classes, naming conventions, utility methods)
- **Changed patterns** where the documented approach no longer matches the code (renamed classes, deprecated patterns, updated conventions)
- **Removed patterns** where documented code no longer exists
- **New examples** that better illustrate current conventions than the existing ones

Also scan the codebase for directories or packages that are **not covered by any instruction file**. Consider whether they
are significant enough to warrant a new instruction file (i.e. they contain a distinct set of conventions or patterns).

### 3. Present proposed changes

Present a summary to the user organised as follows:

```
## Proposed Instruction Updates

### Files to update
- **controllers.instructions.md**: Added section on [new pattern]. Updated example for [changed pattern]. Removed reference to [deleted class].
- **services.instructions.md**: No changes needed.
- ...

### New files to create
- **new-package.instructions.md**: Would cover `new-package/` directory. Key patterns: [summary].

### Directory table changes
- Added row for `new-package.instructions.md`
- Updated scope for `helpers.instructions.md` to include `new-directory/`

### No changes needed
- services.instructions.md, validation.instructions.md, ...
```

Ask the user for confirmation before proceeding. The user may approve all changes, select specific changes, or request
modifications.

### 4. Apply changes

On confirmation:
- Update existing instruction files with the approved changes. Preserve the existing structure and style of each file —
  add new sections, update examples, and remove outdated content without rewriting sections that haven't changed.
- Create any approved new instruction files following the same format (YAML frontmatter with `applyTo`, then markdown content).
- Update the directory mapping table in `.github/copilot-instructions.md` if files were added or scopes changed.

## Instruction file format

Each instruction file must follow this format:

```markdown
---
applyTo: "**/directory-name/**"
---

# Section Title

Content describing patterns and conventions.

## Subsection

Code examples in fenced code blocks.
```

The `applyTo` field is a glob pattern that tells Copilot when to apply the instructions. Use `**` to match across
directory levels.

## Guidelines

- **Be precise**: Only document patterns that are consistently used across the codebase, not one-off implementations.
- **Use real examples**: Reference actual class names, annotations, and file paths from the codebase.
- **Keep it concise**: Instruction files should be a quick reference, not exhaustive documentation. Focus on conventions
  that a developer (or AI) would need to follow when working in that area.
- **Preserve style**: Each instruction file has its own voice and structure. When updating, match the existing style rather
  than imposing a uniform format.
- **Don't duplicate**: If a convention is already covered in the main `copilot-instructions.md`, don't repeat it in the
  path-specific files. The path-specific files should cover patterns unique to their directory.
