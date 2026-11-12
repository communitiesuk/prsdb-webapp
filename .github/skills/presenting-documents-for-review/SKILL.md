---
name: presenting-documents-for-review
description: Use when asking the user to review or approve a design spec, implementation plan, or other generated document, or when asking a question whose answer depends on options or content just written in chat.
allowed-tools: 'write shell(Copy-Item:*) shell(New-Item:*) shell(cp:*) shell(mkdir:*)'
---

# Presenting Documents for Review

The user cannot approve what they cannot see. Saving a file to the session
workspace is not presenting it.

**Core rule:** Chat text written in the same turn as an `ask_user` call may be
collapsed or hidden when the question appears. Never rely on it. Everything the
user needs to answer must be in one of:

- the `ask_user` question itself (self-contained summary), or
- a `publish_chat_update` message (Copilot app only — always stays visible), or
- an opened document (app canvas or IntelliJ editor).

## Detect the Environment

| Signal | Environment |
|--------|-------------|
| `publish_chat_update` and `open_canvas` tools are available | Copilot app |
| Otherwise | Copilot CLI |

## Copilot App

Session files (`~/.copilot/session-state/<session-id>/files/`) are directly
accessible to the app.

1. Open the document in the editor canvas (the user can read and edit it):
   ```
   open_canvas:
     canvasId: "editor"
     instanceId: "<stable id, e.g. design-spec or plan>"
     input: { "scope": "workspace", "path": "plan.md", "title": "Implementation plan" }
   ```
   `scope: "workspace"` resolves `path` relative to the session `files/` folder.
   Re-open with the same `instanceId` after revisions.
2. Publish it as an artifact with a short summary (key decisions, PR
   breakdown, open questions):
   ```
   publish_chat_update:
     message: "<3-8 line summary>"
     attachments: [{ "kind": "document",
                     "file": { "location": "session", "path": "plan.md", "title": "Implementation plan" } }]
   ```
   Republish after each revision — attachments are snapshots.
3. Then call `ask_user` for approval.

## Copilot CLI

**Never open the file with `idea64`/`idea`/`idea.cmd <file>`** — the IntelliJ CLI
opens files in the last-focused window, which is often a different worktree.
The JetBrains MCP targets the exact project window via `projectPath`, but
refuses files outside the project, so open a review copy inside the project's
gitignored `.idea` folder:

1. `projectPath` = the worktree path (or current working directory).
2. Copy the session file to `<projectPath>\.idea\copilot-review\<file-name>`
   (create the folder if missing). Never put it anywhere tracked by git.
3. Call the JetBrains MCP `open_file_in_editor` with
   `projectPath` and `filePath: ".idea/copilot-review/<file-name>"`.
4. If the MCP tools are unavailable or the call fails (e.g. "doesn't
   correspond to any open project"), do not retry with the IntelliJ CLI.
   Instead print the **full document** as your final message, end it with a
   2-5 line summary and "Reply *approve* or tell me what to change", and **end
   the turn without calling `ask_user`** — a printed document followed by
   `ask_user` in the same turn may be hidden.
5. Otherwise (opened in IntelliJ), call `ask_user` for approval.

If the user says they edited the IntelliJ copy, or after approval the copy
differs from the session file, copy it back to the session workspace before
continuing — the session file is canonical.

## Writing the Approval Question

The `ask_user` question must make sense even if nothing else on screen is
visible:

- Say where the document is ("opened in the side panel" / "opened in IntelliJ").
- Include a 2-5 line summary of what is being approved.
- Offer choices such as `Approve`, `Request changes`.

The same applies to any question that follows an explanation (e.g. proposed
approaches): put each option's one-line trade-off in the question or choices,
or in the app publish the explanation with `publish_chat_update` first.

## Common Mistakes

| Mistake | Fix |
|---------|-----|
| "I've saved the plan to plan.md — approve?" | Open/publish it first |
| Long explanation then `ask_user("Which option?")` | Make the question self-contained |
| `idea64 plan.md` | MCP `open_file_in_editor` with `projectPath` on the `.idea` copy |
| Printing the full document then calling `ask_user` | End the turn after printing; ask in the closing lines |
| Proceeding with a stale session file after IDE edits | Copy the review copy back |
