---
name: receiving-code-review
description: Use when receiving code review feedback on a PR. Requires technical evaluation and verification before implementing suggestions.
allowed-tools: 'write shell(git status) shell(git diff) shell(git log) shell(git show) shell(git branch) shell(git fetch) shell(git rev-parse) shell(gh pr view) shell(gh api) jetbrains'
---

# Receiving Code Review

Evaluate review feedback technically before implementing. Do not blindly agree
or blindly implement.

**Core principle:** Verify before implementing. Ask before assuming. Technical
correctness over social comfort.

## Response Process

1. **READ** — Complete feedback without reacting
2. **UNDERSTAND** — Restate each item in own words (or ask for clarification)
3. **VERIFY** — Check suggestion against codebase reality
4. **EVALUATE** — Is it technically sound for THIS codebase and its conventions?
5. **RESPOND** — Technical acknowledgment or reasoned pushback
6. **IMPLEMENT** — One item at a time, verify each

## Handling Unclear Feedback

If any item is unclear, stop and ask before implementing anything. Items may be
related — partial understanding leads to wrong implementation.

## Evaluating Suggestions

Before implementing a suggestion, check:

- Does it follow project conventions? (custom annotations, validation framework,
  journey patterns, entity base classes)
- Does it break existing functionality?
- Is there a reason for the current implementation? (check git history)
- Does the reviewer have full context?
- Does it conflict with prior architectural decisions?

## When to Push Back

Push back (with technical reasoning) when:
- Suggestion breaks existing functionality
- Reviewer lacks context (explain what they missed)
- Violates YAGNI (feature not used)
- Contradicts established project patterns
- Technically incorrect for this stack

How:
- Reference specific code, tests, or patterns
- Ask clarifying questions
- Propose alternatives
- Escalate to user if architectural disagreement

## When Feedback Is Correct

Acknowledge briefly and fix:
- "Fixed — [brief description]"
- "Good catch. [What was wrong and what changed]."
- Or just fix it without commentary.

Do NOT use performative agreement ("Great point!", "You're absolutely right!").

## Before Responding

After evaluating all feedback items, ask the user:

> "I've reviewed the comments. Would you like me to respond to them on GitHub,
> or would you prefer to respond yourself?"

If the user wants to respond themselves, summarise your evaluation of each item
(agree/disagree/need clarification) so they can use it as input.

## Implementation Order

For multi-item feedback:
1. Clarify anything unclear FIRST
2. Implement in order: blocking issues → simple fixes → complex changes
3. Verify each fix individually (run relevant tests)
4. Check for regressions

## Replying on GitHub

Reply in the comment thread, not as top-level PR comments:

```bash
gh api repos/{owner}/{repo}/pulls/{pr}/comments/{id}/replies -f body="Fixed — ..."
```

```powershell
gh api repos/{owner}/{repo}/pulls/{pr}/comments/{id}/replies -f body="Fixed — ..."
```
