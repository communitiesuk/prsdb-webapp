# Generate Custom Instructions — Design

## Problem

Path-specific instruction files (`.github/instructions/*.instructions.md`) are gitignored — they're local dev config. New developers or new worktrees need a way to generate them from scratch. The `updating-custom-instructions` skill updates existing files but can't create them from nothing.

## Approach

A new skill (`generate-custom-instructions`) that:
1. Parses the Path-Specific Instructions table from `.github/copilot-instructions.md`
2. For each listed instruction file, explores the relevant codebase directories
3. Analyzes patterns (annotations, naming, class hierarchies, test patterns)
4. Generates the instruction file with YAML frontmatter and markdown content
5. Writes it to `.github/instructions/`

## Key Decisions

- **Source of truth**: The table in `copilot-instructions.md` determines which files to generate
- **Generation strategy**: Sequential (one file at a time) for consistency
- **Existing files**: Ask user whether to overwrite or skip
- **Content**: AI-driven analysis of the codebase, not hardcoded templates
- **Scope**: Only generates path-specific files; the main `copilot-instructions.md` comes from the template

## Relationship to Other Skills

- `generate-custom-instructions`: Creates instruction files from scratch (initial setup)
- `updating-custom-instructions`: Updates existing instruction files to match codebase changes (maintenance)
