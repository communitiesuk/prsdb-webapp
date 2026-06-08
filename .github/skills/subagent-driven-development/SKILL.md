---
name: subagent-driven-development
description: Use when executing implementation plans with independent tasks. Dispatches fresh sub-agents per task with two-stage review.
allowed-tools: 'write jetbrains'
---

# Subagent-Driven Development

Execute a plan by dispatching a fresh sub-agent per task, with two-stage review
after each: spec compliance first, then code quality.

**Core principle:** Fresh sub-agent per task + two-stage review = high quality,
fast iteration, no context pollution.

**Continuous execution:** Do not pause between tasks to check in with the user.
Execute all tasks without stopping. Only stop for: BLOCKED status, genuine
ambiguity, or all tasks complete.

## Process

For each task in the plan:

### 1. Dispatch Implementer

Launch a `general-purpose` sub-agent with:
- The task description from the plan (verbatim)
- The worktree path as `projectPath`
- Instructions to use `making-code-edits` skill (JetBrains MCP as default)
- The full file list for the task
- Any code examples from the plan
- Verification command to run after implementation

The implementer prompt must include:

```
## Project Conventions
- Use @PrsdbController, @PrsdbWebService, @PrsdbWebComponent (not bare Spring)
- Entities extend AuditableEntity or ModifiableAuditableEntity
- Validation uses @IsValidPrioritised + @ValidatedBy (not @NotBlank, @Size)
- Constructor injection only (private val parameters)
- Test methods use backtick-quoted names
- Use JetBrains MCP tools for all code operations (projectPath: "<worktree>")

## Your Task
[task content from plan]

## Verification
Run: [command from plan]
Expected: [expected outcome]
```

### 2. Spec Review

After the implementer completes, launch an `explore` sub-agent to review:
- Does the implementation match what the plan specified?
- Are all files listed in the task present and correct?
- Does the verification command pass?

If gaps found: re-dispatch implementer with specific fixes.

### 3. Code Quality Review

Launch an `explore` sub-agent to check:
- Custom annotations used correctly (`@PrsdbController` not `@Controller`, etc.)
- Entity auditing base classes used
- Constructor injection (no `@Autowired`)
- Validation framework (`@ValidatedBy` not Bean Validation)
- Journey framework hierarchy (if applicable)
- `@Transactional` on write methods
- `@PreAuthorize` on controllers

If issues found: re-dispatch implementer with specific fixes.

### 4. Record Result

Extract and record only:
- **Status:** DONE / DONE_WITH_CONCERNS / BLOCKED
- **Files changed:** list of file paths
- **Concerns:** any issues (empty if none)

Write one-line summary to checkpoint. Do NOT carry forward full sub-agent output.

## Sub-Agent Tool Instructions

Every implementer prompt must include:

```
## Tools
- Use JetBrains MCP tools for searching, reading, and editing code
  (jetbrains-search_text, jetbrains-read_file, jetbrains-replace_text_in_file, etc.)
- Always pass projectPath: "<worktree-path>"
- After edits: jetbrains-reformat_file, then jetbrains-build_project
- For new files: jetbrains-create_new_file
- For renames: jetbrains-rename_refactoring
- Fall back to CLI tools only for files outside the repository
```

## Error Handling

- If an implementer returns BLOCKED: assess whether you can provide the missing
  context. If yes, re-dispatch with context. If no, stop and report to user.
- If spec review fails twice: stop task, report to user with details.
- If code quality review fails twice: accept with DONE_WITH_CONCERNS, note issues.

## Progress Monitoring

When a sub-agent is running in background mode, check its status every 60-120
seconds using `read_agent`. Report progress to the user:

```
Sub-agent (Task 3): Running — last output: "Running test class FooServiceTests..."
```

If no new output for 3+ minutes, investigate:
- Check if the sub-agent is stuck on a prompt or confirmation
- Check if tests are hanging (database locks, Docker issues)
- If genuinely stuck, stop the agent and re-dispatch with adjusted instructions

## Parallelisation

Tasks that have no dependency between them may be dispatched in parallel. Check
the plan's task dependency structure before parallelising.
