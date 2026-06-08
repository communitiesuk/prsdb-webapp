---
name: brainstorming
description: Use before any creative work — creating features, building components, adding functionality, or modifying behaviour. Explores intent, requirements, and design before implementation.
allowed-tools: 'write'
---

# Brainstorming

Turn ideas into fully formed designs through collaborative dialogue. Understand
what is being built before writing any code.

**Hard gate:** Do NOT invoke any implementation skill, write any code, or take
any implementation action until a design has been presented and the user has
approved it.

## Process

1. **Explore context** — check relevant files, docs, recent commits
2. **Ask clarifying questions** — one at a time, prefer multiple choice
3. **Propose 2-3 approaches** — with trade-offs and a recommendation
4. **Present design** — in sections scaled to complexity, get approval after each
5. **Stress-test the design** — walk every decision branch, resolve gaps
6. **Write design spec** — save to session workspace (`~/.copilot/session-state/<session-id>/files/design-spec.md`)
7. **Self-review** — check for placeholders, contradictions, ambiguity
8. **User reviews spec** — wait for approval before proceeding
9. **Transition** — invoke `writing-plans` skill

## Asking Questions

- One question per message
- Prefer multiple choice (with freeform allowed)
- Focus on: purpose, constraints, success criteria, user-visible behaviour
- If the request spans multiple independent subsystems, flag decomposition first

## Exploring Approaches

- Always propose 2-3 options with trade-offs
- Lead with the recommended option and explain why
- Consider existing patterns in the codebase before suggesting new ones

## Presenting the Design

Scale each section to its complexity — a few sentences if straightforward, a
paragraph if nuanced. Cover:

- Architecture and component boundaries
- Data flow (entities, services, controllers)
- Journey/form flow (if applicable)
- Validation approach
- Error handling
- Feature flag strategy (if user-visible behaviour change)
- Testing approach (which layers need tests)

## Stress-Testing the Design

After the user approves the high-level design, walk every branch of the decision
tree to surface gaps before writing the spec. For each open question:

1. State the decision point clearly
2. Provide your recommended answer (with reasoning)
3. Ask the user to confirm or redirect

Rules:
- One question at a time
- If the answer can be found by exploring the codebase, check the codebase
  instead of asking
- Keep going until every branch is resolved — do not move on with "TBD" items
- Cover: edge cases, error states, empty states, permissions, concurrency,
  migration paths, feature flag transitions

Only proceed to writing the spec once all branches are resolved.

## Working in This Codebase

Before proposing changes:
- Search for existing patterns that solve a similar problem
- Follow established conventions (custom annotations, journey framework, etc.)
- If existing code has problems that affect the work, include targeted
  improvements — do not propose unrelated refactoring

## Design Spec Content

The spec should include:

- **Goal** — one sentence
- **Approach** — architecture decisions and why
- **Components** — what is created/modified and its responsibility
- **Data model** — entities, relationships, migrations needed
- **User flow** — step-by-step from user's perspective
- **Validation** — what is validated, error messages
- **Feature flags** — if applicable, which flag, which release
- **Testing strategy** — which layers, what coverage
- **Out of scope** — explicitly state what is NOT included

## Self-Review

After writing the spec:
1. **Placeholder scan** — any "TBD", "TODO", vague sections? Fix them.
2. **Internal consistency** — do sections contradict each other?
3. **Scope check** — focused enough for a single plan, or needs decomposition?
4. **Ambiguity check** — could any requirement be interpreted two ways?

Fix inline. Do not re-review.

## After Approval

Invoke the `writing-plans` skill. Do NOT invoke any other implementation skill
directly from brainstorming.
