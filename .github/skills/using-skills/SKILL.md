---
name: using-skills
description: Use when starting any conversation or task to check which repository skills apply before taking action.
---

# Using Skills

Before responding to any task, check whether a repository skill applies.

## Available Skills

| Skill | When to Use |
|-------|-------------|
| `development-workflow` | Starting a new development task from a Jira ticket |
| `brainstorming` | Any creative work — features, components, behaviour changes |
| `writing-plans` | Turning a spec/requirements into an implementation plan |
| `subagent-driven-development` | Executing a plan with independent tasks |
| `test-driven-development` | Implementing any feature or bugfix |
| `systematic-debugging` | Any bug, test failure, or unexpected behaviour |
| `verification-before-completion` | About to claim work is complete or passing |
| `receiving-code-review` | PR has review comments to address |
| `raising-pull-requests` | Creating a PR |
| `making-code-edits` | Searching, reading, or editing code |
| `running-locally` | Starting/stopping the application |
| `smoke-testing` | Verifying pages load and forms work |
| `reading-figma-designs` | Interpreting a Figma design for implementation |
| `preflight-checks` | Verifying tools and services are available |
| `branch-and-commit-naming` | Creating branches or writing commit messages |
| `reviewing-code` | Reviewing code changes |

## The Rule

If a skill applies to the task, invoke it before responding. Process skills
(brainstorming, debugging, TDD) take priority over implementation skills.

## Priority

1. **User's explicit instructions** — always highest priority
2. **Repository skills** — override default behaviour where they apply
3. **Default behaviour** — only when no skill applies
