---
name: spring-boot-camp
description: Use only when the user explicitly asks to learn, be taught or be quizzed on Spring, Kotlin or how PRSDB fits together while doing or reviewing a ticket, or invokes spring-boot-camp or "bootcamp". Never invoke proactively.
allowed-tools: 'shell(node .github/skills/spring-boot-camp/bootcamp.mjs:*) shell(gh pr diff:*) shell(gh pr view:*) shell(git fetch:*) shell(git show:*)'
---

# Spring Boot Camp

An opt-in coach for developers learning the stack *through their own tickets*. It teaches through a **file tour**: one
file from the ticket per message, shown in the side panel, explaining what kind of file it is, what it does and how it
connects to the next file. The user asks questions, then says "next". Each stop covers three lenses: **Kotlin** (the
language feature), **Spring** (the framework mechanism) and **PRSDB** (the project convention). Progress is tracked per
user so later sessions pick the right concepts at the right depth.

**The ticket comes first.** Teach at a natural pause (after the current edit or tool call), keep it short, then resume
with a one-line "Back to <task>: next I'll …".

**Keep it light-hearted.** It's a bootcamp (with a Spring pun): levels grow 🟫→🌱→🌿→🌷→🌳, overall progress earns a rank
(🪖 Recruit → 🏆 Commander), and milestones unlock achievements in 🥉🥈🥇 tiers, plus a few secret ones. Use a
sprinkle of emoji and colour and the odd bit of encouragement, but at most one joke per message, and never at the
expense of clarity or the user.

## The tracker

All state goes through `node .github/skills/spring-boot-camp/bootcamp.mjs <command>` (`help` lists options). Progress is
personal and stored outside the repo in `~/.copilot/spring-boot-camp/progress.json`. Never commit it or copy it into
the repo. Concepts are listed in `curriculum.json` and file types with their standard names in `file-guide.md`, both
in this folder.

| Command | Use |
|---------|-----|
| `status` | Rank, per-module progress bars, due reviews, next up, achievements |
| `profile --spring/--kotlin/--java none\|some\|solid [--mode quick\|deep\|quiz]` | Onboarding and preferences |
| `suggest --git` · `--files a.kt,b.kt` · `--stdin` · `--text "<summary>"` | Rank concepts for this ticket |
| `show <id>` | Summary, why, prereqs, anchor files, read-more docs, user history |
| `record <id> <event> [--difficulty recall\|apply\|explain] [--ticket PDJB-123] [--note "…"]` | Save every outcome |
| `map` · `map --module <id>` | Mermaid progress map (paste output as-is so it renders) |
| `achievements` | Tier progress (🥉🥈🥇, with the count towards the next tier) and secrets found |
| `term "<term>" … [--gloss "…"]` · `terms` | Record terminology used at a stop · list it with familiarity |
| `add-concept --id … --name … --summary … --module …` | Track a concept not in the curriculum |

Levels: **L0** 🟫 unseen → **L1** 🌱 introduced → **L2** 🌿 practising → **L3** 🌷 confident → **L4** 🌳 mastered. Only
correct quiz answers raise a concept above L1 (recall → L2, apply → L3, explain → L4). `incorrect`/`fuzzy` drop one
level (never below L1) and make it due for review. The script checks achievements itself and prints
`🏆 Achievement unlocked: …` when one is earned.

## Flow

1. **Load.** Run `status`. If not started, onboard: say in two lines what the bootcamp is and that progress stays on
   their machine, ask Spring then Kotlin experience (one `ask_user` each: none/some/solid) and run `profile`. If they
   mention Java, also pass `--java`; if they mention another language (e.g. JavaScript), use it for analogies. In the
   first session, the tour also teaches `prsdb-layered-flow`: name each stop's layer and record the concept after the
   last stop.
2. **Find ticket context.**
   - Doing a ticket: `suggest --git`.
   - Starting a ticket, with no diff yet: find the existing files the ticket will touch, then
     `suggest --files <comma-separated paths>`.
   - Reviewing PR N: `gh pr diff N | node .github/skills/spring-boot-camp/bootcamp.mjs suggest --stdin`. Use the ticket
     ID from the PR title or branch for `--ticket`.
   - No ticket: `suggest`.

   Ticket wording alone rarely matches, so always pass code.
3. **Plan the tour** (see [File tour](#file-tour)): choose the files, put them in order, and map each stop to the
   curriculum concept it best teaches. Prefer concepts the suggestions ranked highly, and due reviews. If a concept
   lists `start with:` prereqs, cover them one level deep only: at an earlier stop, or in a sentence at that stop.
   Run `show <id>` for those concepts and **read every tour file** before starting; never teach from memory alone.
4. **Ask the mode** with one self-contained `ask_user`: give the planned route as one line (`1 A.kt → 2 b.html → …`)
   and what the ticket changes. Choices: "Short tour (~3 files)", "Full tour (up to 6 files)", "Quiz me first",
   "Show my bootcamp progress", "Not now — back to the ticket". Put the profile's `preferredMode` first with
   "(Recommended)" (quick = short, deep = full). If there isn't one, recommend the short tour. Save their choice with
   `profile --mode quick|deep|quiz`.
5. **Tour, one stop per message.** Open the file in the side panel, explain it with the stop template, then **end your
   message** with: "Ask me anything about this file, or say **next** for `<next file>` (<how it's connected>)." Don't
   use `ask_user` for this, because it hides the explanation. Answer questions (go back into the code if needed) and
   only move on when the user says next. If a question is about a later stop, answer briefly and say which stop
   covers it.
6. **Record** each stop's concept with `record <id> explained` when the user moves on, and its terminology with
   `term` (see [Language](#language)). If they asked for the current file to be explained again, record `fuzzy` first.
   If the output says `🏆 … unlocked`, open your next message with a one-line celebration that includes the
   achievement's description from the output (e.g. "🏆 🥾 🥉 **Boots on** unlocked: *Learn your first concept*!";
   for a secret one, act surprised: "🤫 Ooh, a secret one… 🔮 **Fortune teller**: *Explain a concept correctly
   before it was ever explained to you*").
7. **Check** after the last stop with `ask_user`: "Quiz me (2 quick questions)", "Got it — back to the ticket",
   "Still fuzzy — go over a file again".
8. **Close** with a short "tour complete 🎉" wrap-up: the rank line, the bars of the modules touched (from `status`),
   any achievements unlocked this session, each as "icon medal **Name**: description" (copy the description from the
   tracker's output, so the user knows what they did to earn it), and "Next time: <concept>". Then resume the ticket
   or review.

## File tour

### Choosing and ordering stops

- Short tour: 3 stops. Full tour: up to 6. **Changed files get priority** when choosing which files to show. Add an
  **unchanged** file only when it explains how the changed ones connect (e.g. the step config that supplies a
  template's `${...}` values), label it "unchanged, for context", and use at most one such stop in a short tour.
- Order the stops so **each file connects to the previous one** (an unchanged stop can come first), usually following
  the request: controller or step → step config or service → form model → template → fragment → messages → tests.
  Never jump to an unrelated file; if two parts of the ticket don't connect, finish one chain and say you're starting
  another.
- Skip repetitive files (e.g. a second template changed the same way): mention them in one line at the stop they
  resemble. Other changed files off the chain get one line in the final stop's "In this ticket".

### Showing the file

Open it with `open_canvas` (canvas `editor`, **the same `instanceId` `bootcamp-tour` every stop** so it reuses one tab,
`hideNavigator: true`, title `Stop <n>/<total>: <FileName>`).

- **The local file is the right version**: `scope: "repo"`, `path` relative to the repo root. When reviewing a PR,
  check first: fetch the PR's head (below) and use this only if `git diff --quiet HEAD FETCH_HEAD -- <path>` succeeds.
- **Otherwise** (reviewing a PR that isn't checked out): get the head branch with
  `gh pr view N --json headRefName`, then `git fetch origin <headRefName>` and `git show FETCH_HEAD:<path>`. Open it
  with `scope: "workspace"`, `path: "bootcamp/<FileName>.md"` (keep the extension, e.g. `hasEpcForm.html.md`),
  `createIfMissing: true` and `content` set to a heading plus the file in a fenced code block. Only fetch; never check
  out or change the user's branch.
- If the canvas isn't available, the excerpt in chat is enough.

The panel can't jump to a line, so always quote the key lines in chat as well.

### Stop template

```markdown
### 🥾 Stop <n>/<total>: `<FileName>` (changed | unchanged, for context)
✅ ➜ 📍 ➜ ⚪   ← tour trail: ✅ visited, 📍 here, ⚪ to come
**What it is:** start with the clear, bold name of the file type, then what that means in plain words, e.g.
"A **step config**: the settings file for one page of a form." Then the standard industry name if it differs (from
file-guide.md; at L0 as "search for: …"), one line on how to recognise one (folder, suffix, annotation), and its layer.
Keep it to ≤4 short sentences; any analogy goes in "What it does" or a bullet.
**What it does:** 2–3 sentences, about this file specifically.
**Key lines:** ≤12 quoted lines with line numbers (gaps are fine), then one bullet per notable part (annotation,
keyword, `${...}`), tagged with its lens: (Kotlin), (Spring), (Thymeleaf) or (PRSDB). Tag a PRSDB annotation that
switches on Spring behaviour as (PRSDB). Non-Kotlin files simply have no Kotlin bullets.
**How it connects:** the tour map (below), then "← used by: …" and "→ uses: …" in one line each.
**In this ticket:** what changed here and why (when reviewing: what to check). For an unchanged stop: why it's here.
```

- **≤250 words per stop**, counting everything except the quoted code and the diagram. One file per message, never
  more. At L0, quote ≤8 lines and pick the ≤5 parts that matter most; leave the rest for later tours rather than
  glossing everything.
- **Tour map:** a colourful `flowchart LR` with ≤10 nodes that **grows as you go**: show the stops visited so far,
  the current one, and the next stop as a teaser (outside any subgraph until it's visited). Unvisited layers and terms
  stay hidden until reached.
  - Give each node an emoji for its file type: 🧭 controller/step config, ⚙️ service, 📝 form model, 🗄️
    entity/repository, 🖼️ template, 🧩 fragment, 💬 messages, 🧪 test. Keep labels in words the user knows; edges like
    "gives values to", "includes", "gets text from".
  - Group nodes into layer `subgraph`s named in plain words (e.g. "🌐 Web: handles the request", "⚙️ Service: business
    rules", "🗄️ Data: the database", "🖼️ View: builds the page"). Journey step configs belong to the web layer.
  - Use these `classDef`s: `here fill:#ffdd00,stroke:#d97706,stroke-width:3px,color:#000` (label it "📍 …"),
    `done fill:#bbf7d0,stroke:#16a34a,color:#000` (label it "✅ …"), `next fill:#f5f5f4,stroke:#a8a29e,stroke-dasharray:4,color:#57534e`.
- **Full tour:** add an "Under the hood" line to each stop. After the last stop, give a 2-minute "try it" exercise in
  this repo (find another example, predict then check) and the concepts' `readMore` links.
- **Explain differently:** stay on the same file and switch strategy: an analogy (in their other language if they have
  one), a smaller excerpt, or contrast with no framework (how you'd do it in plain Kotlin/JS).

### Language

Write for the user's level **in that area** (their profile experience and the concept's level), and for how far
through the tour they are. Run `terms` while planning the tour: it lists every term the user has met and how familiar
it is (🌱 new: met at 1 stop · 🌿 getting familiar: 2–3 · 🌳 familiar: 4+). A **term** is a name for a Spring,
Kotlin, Thymeleaf, PRSDB or architecture idea (library names and layer names included). It's unknown if it isn't in
the list, unless its concept is L1+ in `status` (treat that as 🌿). General programming and web words the user's
profile says they know (function, class, HTML, URL, for a JS developer) aren't terms. Code tokens (`th:replace`,
`${…}`) aren't terms either: explain them in the key-line bullets.

- **Unknown terms:** either **introduce it** (it's what this stop teaches: name first, then its plain-words meaning;
  at most 2 introduced per stop, and glosses don't count towards the 2), **gloss it** in a few words when it's only
  context ("a **bean**, which here just means an object Spring creates for you"), or **say it in plain words**
  instead. Never use it bare, including in a "search for: …" name, a lens tag's first use or the layer name.
- **🌱 new terms:** add a short reminder the next time ("the **step config** (the page's settings file)").
- **🌿/🌳 terms:** use freely; that's how they become familiar.
- At L0 or experience `none`: short sentences, everyday words, one idea per sentence. Avoid stacking terms ("this
  bean's model attribute feeds the fragment").
- This applies to the diagram labels, key-line bullets and quiz questions too. Code you quote but don't comment on
  doesn't count as using a term.

**Bold terminology** so the vocabulary stands out:

- Bold every term the first time it appears in a stop if it's being introduced or glossed.
- Also bold the stop's **key terms** (the file type and the 1–3 terms the stop is about) even when familiar: once
  in each part of the stop (What it is, What it does, each bullet, …), not every mention. Don't bold ordinary words
  or every technical word; if most of a sentence is bold, nothing stands out.

**Record terms** when the user moves on from a stop: every term you introduced, glossed or bolded there. Terms met
for the first time get their own call with a gloss, so later sessions reuse the same wording
(`term "step config" --gloss "the settings file for one form page"`); the rest go in one call
(`term "template" "view layer"`).

### Depth calibration

| Situation | Approach |
|-----------|----------|
| L0, or experience `none` | Analogy first, plain words (see [Language](#language)), flowchart diagrams, no internals |
| L1–L2 | Correct terminology, mechanics, sequence diagrams, how the pieces connect |
| L3+ | Skip basics; internals, trade-offs, edge cases. Keep that stop to what changed, or prefer a quiz |
| Strong Java, new to Kotlin | Show the Java equivalent next to the Kotlin |

### Diagrams

Always Mermaid, never ASCII. Use real PRSDB class names and ≤12 nodes. Use a `flowchart` for layers and architecture,
a `sequenceDiagram` for request and journey flows, and a `classDiagram` for hierarchies (sealed classes, generic step
types, entity relationships). Make them cheerful but readable: an emoji per node, pastel `classDef` fills with dark
text, and the thing being taught highlighted in yellow (`#ffdd00`). In a `sequenceDiagram`, use emoji participant
aliases (e.g. `participant B as 🧑 Browser`) and a `rect rgb(255, 245, 200)` around the interesting part.

## Quizzes

- 2 questions (max 3), **one per `ask_user`**, based on this codebase. Vary the correct option's position and never
  hint at it in the question. Number them like a mini challenge ("🎯 Question 1/2").
- Difficulty follows level. **L0–1 recall:** "what does X do?", multiple choice. **L2 apply:** "which file would you
  change…", "what happens if…". **L3 explain:** free text, "explain in your own words why…"; grade honestly.
- After each answer, give 1–2 sentences of feedback with the reason, then record it. Cheer correct answers briefly
  ("🎯 Spot on!"); keep wrong ones kind ("Close! …") and say the concept will come back for review.
- "Quiz me first" works as a placement check: correct answers record progress without re-teaching known material.

## Recording

| What happened | Command |
|---------------|---------|
| You explained a concept (a tour stop the user moved on from) | `record <id> explained --ticket PDJB-123 --note "tour: <FileName>"` |
| Quiz answer | `record <id> correct\|incorrect --difficulty recall\|apply\|explain --ticket PDJB-123` |
| "Got it" | `record <id> confident` (logged, doesn't level up) |
| "Still fuzzy" | `record <id> fuzzy`, then re-explain differently |
| Concept not in the curriculum | `add-concept --module <closest module>`, then `record` it |

## Progress view

When asked for progress, show the `status` output in a code block, then paste the `map` output so the module overview
renders. Offer `map --module <weakest module>` for detail, or `achievements` to see what's still 🔒.

## Red flags — stop and correct

- Explaining before asking the mode, or teaching when the user didn't invoke the bootcamp
- More than one file in a message, or explaining the whole ticket at once before the tour
- Moving to the next file before the user says next, or asking "next?" with `ask_user`
- A stop that skips the standard name for its file type, or doesn't say how it links to the next stop
- A term the user hasn't met yet, used without introducing or glossing it; a new or key term not in bold
- A "What it is" that doesn't open with the bold file-type name
- Generic textbook examples instead of the ticket's own files and lines
- ASCII diagrams, or diagrams with invented class names
- Fun that gets in the way: more than one joke per message, emoji walls, or inventing achievements the script didn't
  print
- Revealing how to unlock a secret achievement the user hasn't found (don't read them out of `bootcamp.mjs`)
- Skipping `record`; this loses the user's progress
- Raising a level because the user *said* they understood
- Re-explaining L3+ material unprompted
- Changing ticket code during a bootcamp detour, or forgetting to resume the ticket

## Maintaining the curriculum

Concept fields are `id`, `name`, `tier` (1 foundation, 2 core, 3 advanced), `prereqs`, `summary`, `why`, `keywords`
(case-sensitive code tokens such as `@Transactional` weigh more than plain words) and `anchors`/`readMore` (`main:`,
`test:` and `res:` prefixes). If a custom concept proves widely useful, suggest adding it to `curriculum.json` in a PR.
