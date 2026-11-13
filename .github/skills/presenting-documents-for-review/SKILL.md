---
name: presenting-documents-for-review
description: Use when asking the user to review or approve a design spec, implementation plan, or other generated document saved to the session workspace.
allowed-tools: 'write'
---

# Presenting Documents for Review

The user cannot approve what they cannot see. Saving a file to the session
workspace is not presenting it.

Try each method in order and stop at the first that succeeds. If a tool is
missing or a call errors, move to the next method — do not retry with other
tools.

| # | Method | Use when |
|---|--------|----------|
| 1 | Copilot app canvas + artifact | A tool exists to open a side-panel editor canvas |
| 2 | IntelliJ via JetBrains MCP | JetBrains MCP tools exist and the project is open |
| 3 | Path + summary in chat | Anything else |

Identify tools by what they do, not by exact name — names vary between app,
CLI and MCP versions.

## 1. Copilot App

The app can read session files (`~/.copilot/session-state/<session-id>/files/`)
directly.

1. Open the document in the editor canvas (the user can read and edit it).
   Currently:
   ```
   open_canvas:
     canvasId: "editor"
     instanceId: "<stable id, e.g. design-spec or plan>"
     input: { "scope": "workspace", "path": "plan.md", "title": "Implementation plan" }
   ```
   `scope: "workspace"` resolves `path` relative to the session `files/` folder.
   If the canvas type or schema differs, inspect the canvas capabilities first.
   Re-open with the same `instanceId` after revisions.
2. If a tool exists to publish a chat update with document attachments, publish
   the file with a 3-8 line summary (key decisions, PR breakdown, open
   questions). Republish after each revision — attachments are snapshots.
3. Ask for approval.

## 2. IntelliJ (JetBrains MCP)

**Never use the IntelliJ CLI (`idea64`/`idea`/`idea.cmd <file>`)** — it opens
files in the last-focused window, which is often a different worktree. The
JetBrains MCP targets the exact window via `projectPath`, but refuses files
outside the project, so open a review copy in the project's gitignored `.idea`
folder:

1. `projectPath` = the worktree path (or current working directory).
2. Create `<projectPath>\.idea\copilot-review\<name>-r<N>.md` with the file
   creation tool, using the session file's content. Do not use shell copy
   commands. Increment `N` for each revision so no file is ever overwritten.
3. Call the JetBrains MCP `open_file_in_editor` with `projectPath` and
   `filePath: ".idea/copilot-review/<name>-r<N>.md"`.
4. Ask for approval.

If the user says they edited the IntelliJ copy, read it and apply the changes
to the session file before continuing — the session file is canonical.

## 3. Path + Summary

End your message with:

- the full path of the session file,
- a 3-8 line summary (key decisions, PR breakdown, open questions),
- "Reply *approve*, tell me what to change, or ask me to print it in full."

Then **end the turn without calling `ask_user`**. Print the full document only
if the user asks.

## Asking for Approval

Follow "Asking the User Questions" in `.github/copilot-instructions.md`: the
question must say where the document is, summarise what is being approved, and
offer choices such as `Approve` and `Request changes`.

## Common Mistakes

| Mistake | Fix |
|---------|-----|
| "I've saved the plan to plan.md — approve?" | Present it first |
| `idea64 plan.md` | MCP `open_file_in_editor` with `projectPath` on the `.idea` copy |
| Shell-copying the review copy, or overwriting a previous one | File creation tool with a new `-r<N>` suffix |
| Printing the whole document unprompted | Path + summary; print in full only on request |
| Proceeding with a stale session file after IDE edits | Apply the IDE edits to the session file |
