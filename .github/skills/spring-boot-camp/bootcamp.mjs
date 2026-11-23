#!/usr/bin/env node
// Spring Boot Camp progress tracker. Zero dependencies; Node 18+.
// Progress is personal and lives outside the repo: $SPRING_BOOT_CAMP_HOME or ~/.copilot/spring-boot-camp/progress.json
// Run `node bootcamp.mjs help` for usage.

import { execSync } from "node:child_process";
import fs from "node:fs";
import os from "node:os";
import path from "node:path";
import { fileURLToPath } from "node:url";

const SCRIPT_DIR = path.dirname(fileURLToPath(import.meta.url));
const CURRICULUM_PATH = path.join(SCRIPT_DIR, "curriculum.json");
const HOME = process.env.SPRING_BOOT_CAMP_HOME || path.join(os.homedir(), ".copilot", "spring-boot-camp");
const PROGRESS_PATH = path.join(HOME, "progress.json");

const DAY = 24 * 60 * 60 * 1000;
const LEVEL_NAMES = ["unseen", "introduced", "practising", "confident", "mastered"];
const REVIEW_DAYS = { 1: 2, 2: 7, 3: 21, 4: 60 };
const EVENTS = ["explained", "correct", "incorrect", "fuzzy", "confident"];
const DIFFICULTIES = ["recall", "apply", "explain"];
const EXPERIENCE = ["none", "some", "solid"];
const LEARNING_PATH = ["prsdb-architecture", "kotlin", "foundations", "web-mvc", "data", "security", "testing", "beyond-web"];
const CUSTOM_MODULE = { id: "custom", name: "Discovered along the way", concepts: [] };
// Levels grow like a plant (it's Spring, after all).
const LEVEL_ICONS = ["🟫", "🌱", "🌿", "🌷", "🌳"];
const MAP_COLOURS = ["#e7e5e4", "#d9f99d", "#99f6e4", "#f9a8d4", "#4ade80"];
const MODULE_ICONS = {
    "prsdb-architecture": "🏘️",
    kotlin: "🧩",
    foundations: "🧱",
    "web-mvc": "🌐",
    data: "🗄️",
    security: "🔐",
    testing: "🧪",
    "beyond-web": "⏰",
    custom: "🧭",
};
const RANKS = [
    [0, "🪖", "Recruit"],
    [0.05, "🎒", "Cadet"],
    [0.15, "🎖️", "Corporal"],
    [0.3, "⭐", "Sergeant"],
    [0.5, "🌟", "Lieutenant"],
    [0.75, "🏆", "Commander"],
];

const quizAnswers = (p) => p.log.filter((e) => e.event === "correct" || e.event === "incorrect");
const localTimes = (p) => p.log.map((e) => new Date(e.at));
const termsWith = (p, minUses) => Object.values(p.terms ?? {}).filter((t) => t.uses >= minUses).length;
const SPRING_MODULES = ["foundations", "web-mvc", "data", "security", "testing", "beyond-web"];

// Kinds of bean, matched against term names and toured file names.
const BEAN_KINDS = {
    controller: (s) => /controller/i.test(s) && !/rest\s*controller|controller\s*advice/i.test(s),
    restController: (s) => /rest\s*controller/i.test(s),
    controllerAdvice: (s) => /controller\s*advice/i.test(s),
    service: (s) => /service/i.test(s),
    repository: (s) => /repository/i.test(s),
    component: (s) => /component/i.test(s),
    configuration: (s) => /configuration|(?<!step)config\.kt/i.test(s),
    beanMethod: (s) => /@bean\b|bean method/i.test(s),
    mockBean: (s) => /mock(ito)?\s*bean/i.test(s),
};

function beanKindsSeen(p) {
    const seen = [...Object.values(p.terms ?? {}).map((t) => t.name), ...p.log.map((e) => e.note ?? "").filter((n) => n.startsWith("tour:"))];
    return Object.values(BEAN_KINDS).filter((matches) => seen.some(matches)).length;
}

function longestStreak(p, event) {
    let run = 0;
    let best = 0;
    for (const e of quizAnswers(p)) {
        run = e.event === event ? run + 1 : 0;
        best = Math.max(best, run);
    }
    return best;
}

// Each tier is [threshold, name, description]; tiers unlock as count() reaches each threshold.
// A tier's stored key is the id for bronze, then `<id>-2` and `<id>-3`.
const MEDALS = ["🥉", "🥈", "🥇"];
const TIERED_ACHIEVEMENTS = [
    {
        id: "boots-on",
        icon: "🥾",
        count: (p) => Object.values(p.concepts).filter((s) => s.level >= 1).length,
        tiers: [
            [1, "Boots on", "Learn your first concept"],
            [10, "Quick march", "Learn 10 concepts"],
            [30, "Long march", "Learn 30 concepts"],
        ],
    },
    {
        id: "tour-guide",
        icon: "🗺️",
        count: (p) => p.log.filter((e) => e.note?.startsWith("tour:")).length,
        tiers: [
            [5, "Day tripper", "Finish 5 file tour stops"],
            [25, "Tour guide", "Finish 25 file tour stops"],
            [100, "Cartographer", "Finish 100 file tour stops"],
        ],
    },
    {
        id: "bullseye",
        icon: "🎯",
        count: (p) => p.log.filter((e) => e.event === "correct").length,
        tiers: [
            [1, "Bullseye", "Get a quiz question right"],
            [15, "Sharpshooter", "Get 15 quiz questions right"],
            [50, "Marksman", "Get 50 quiz questions right"],
        ],
    },
    {
        id: "hat-trick",
        icon: "🎩",
        count: (p) => longestStreak(p, "correct"),
        tiers: [
            [3, "Hat trick", "Get 3 quiz questions right in a row"],
            [7, "On a roll", "Get 7 quiz questions right in a row"],
            [15, "Unstoppable", "Get 15 quiz questions right in a row"],
        ],
    },
    {
        id: "comeback",
        icon: "🔁",
        count: (p) => Object.values(p.concepts).filter((s) => {
            const history = s.history ?? [];
            const slip = history.findIndex((h) => h.event === "incorrect" || h.event === "fuzzy");
            return slip !== -1 && history.slice(slip).some((h) => h.event === "correct");
        }).length,
        tiers: [
            [1, "Comeback kid", "Get a concept right after it was fuzzy or wrong"],
            [5, "Bouncing back", "Win back 5 concepts that were fuzzy or wrong"],
            [15, "Phoenix", "Win back 15 concepts that were fuzzy or wrong"],
        ],
    },
    {
        id: "big-brain",
        icon: "🧠",
        count: (p) => p.log.filter((e) => e.event === "correct" && e.difficulty === "explain").length,
        tiers: [
            [1, "Big brain", "Explain a concept in your own words and get it right"],
            [5, "Galaxy brain", "Explain 5 concepts in your own words"],
            [15, "Professor", "Explain 15 concepts in your own words"],
        ],
    },
    {
        id: "explorer",
        icon: "🧭",
        count: (p, c) => c.modules.filter((m) => m.concepts.some((x) => levelOf(p, x.id) > 0)).length,
        tiers: [
            [3, "Explorer", "Learn something in 3 different modules"],
            [6, "Navigator", "Learn something in 6 different modules"],
            [8, "Globetrotter", "Learn something in all 8 modules"],
        ],
    },
    {
        id: "ticket-collector",
        icon: "🎫",
        count: (p) => new Set(p.log.map((e) => e.ticket).filter(Boolean)).size,
        tiers: [
            [3, "Ticket collector", "Learn on 3 different tickets"],
            [10, "Ticket hoarder", "Learn on 10 different tickets"],
            [50, "Ticket tycoon", "Learn on 50 different tickets"],
        ],
    },
    {
        id: "regular",
        icon: "📅",
        count: (p) => new Set(p.log.map((e) => e.at.slice(0, 10))).size,
        tiers: [
            [3, "Regular", "Learn on 3 different days"],
            [10, "Creature of habit", "Learn on 10 different days"],
            [30, "Devotee", "Learn on 30 different days"],
        ],
    },
    {
        id: "green-thumb",
        icon: "🌳",
        count: (p) => Object.values(p.concepts).filter((s) => s.level >= 4).length,
        tiers: [
            [1, "Green thumb", "Grow a concept all the way to mastered"],
            [5, "Gardener", "Grow 5 concepts to mastered"],
            [20, "Forest ranger", "Grow 20 concepts to mastered"],
        ],
    },
    {
        id: "graduate",
        icon: "🎓",
        count: (p, c) => c.modules.filter((m) => m.concepts.length && m.concepts.every((x) => levelOf(p, x.id) >= 2)).length,
        tiers: [
            [1, "Graduate", "Reach practising or better on every concept in a module"],
            [3, "Honours", "Reach practising or better on every concept in 3 modules"],
            [8, "Valedictorian", "Reach practising or better on every concept in all 8 modules"],
        ],
    },
    {
        id: "trailblazer",
        icon: "🔭",
        count: (p) => (p.customConcepts ?? []).length,
        tiers: [
            [1, "Trailblazer", "Add a concept that wasn't in the curriculum"],
            [3, "Pathfinder", "Add 3 concepts that weren't in the curriculum"],
            [10, "Pioneer", "Add 10 concepts that weren't in the curriculum"],
        ],
    },
    {
        id: "word-hoard",
        icon: "📖",
        count: (p) => termsWith(p, 1),
        tiers: [
            [15, "Word hoard", "Meet 15 pieces of terminology"],
            [50, "Walking dictionary", "Meet 50 pieces of terminology"],
            [150, "Lexicon legend", "Meet 150 pieces of terminology"],
        ],
    },
    {
        id: "fluent",
        icon: "🗣️",
        count: (p) => termsWith(p, 4),
        tiers: [
            [5, "Getting chatty", "Make 5 terms 🌳 familiar"],
            [20, "Fluent", "Make 20 terms 🌳 familiar"],
            [60, "Native speaker", "Make 60 terms 🌳 familiar"],
        ],
    },
];

// Hidden until unlocked: `achievements` only shows how many are left to find.
const HIDDEN_ACHIEVEMENTS = [
    {
        id: "what-time",
        icon: "⏰",
        name: "What time do you call this?",
        desc: "Learn something between 6pm and 8:30am",
        test: (p) => localTimes(p).some((d) => d.getHours() >= 18 || d.getHours() * 60 + d.getMinutes() < 8 * 60 + 30),
    },
    { id: "friday-13th", icon: "🐈‍⬛", name: "Unlucky for some", desc: "Learn something on Friday the 13th", test: (p) => localTimes(p).some((d) => d.getDay() === 5 && d.getDate() === 13) },
    {
        id: "spring-in-spring",
        icon: "🌸",
        name: "Spring in spring",
        desc: "Learn a Spring concept between March and May",
        test: (p, c) => p.log.some((e) => [2, 3, 4].includes(new Date(e.at).getMonth()) && SPRING_MODULES.includes(c.byId.get(e.concept)?.module)),
    },
    { id: "barista", icon: "☕", name: "Barista", desc: "Meet 5 different kinds of bean", test: (p) => beanKindsSeen(p) >= 5 },
    { id: "not-that-bean", icon: "🫘", name: "Not that kind of bean", desc: "Meet the term 'bean'", test: (p) => Object.keys(p.terms ?? {}).some((t) => /\bbeans?\b/.test(t)) },
    {
        id: "kotlin-master",
        icon: "🏝️",
        name: "Kotlin master",
        desc: "Learn every concept in the Kotlin module",
        test: (p, c) => c.modules.some((m) => m.id === "kotlin" && m.concepts.every((x) => levelOf(p, x.id) >= 1)),
    },
    {
        id: "deja-vu",
        icon: "🔂",
        name: "Déjà vu",
        desc: "Have the same concept explained 3 times",
        test: (p) => Object.values(p.concepts).some((s) => (s.history ?? []).filter((h) => h.event === "explained").length >= 3),
    },
    {
        id: "fortune-teller",
        icon: "🔮",
        name: "Fortune teller",
        desc: "Explain a concept correctly before it was ever explained to you",
        test: (p) => Object.values(p.concepts).some((s) => s.history?.[0]?.event === "correct" && s.history[0].difficulty === "explain"),
    },
    { id: "never-give-up", icon: "🧗", name: "Never give up", desc: "Keep answering after 3 wrong answers in a row", test: (p) => /i{3}./.test(quizAnswers(p).map((e) => e.event[0]).join("")) },
    {
        id: "full-circle",
        icon: "💫",
        name: "Full circle",
        desc: "Master a concept you once got wrong",
        test: (p) => Object.entries(p.concepts).some(([id, s]) => levelOf(p, id) >= 4 && (s.history ?? []).some((h) => h.event === "incorrect")),
    },
    {
        id: "mixed-bouquet",
        icon: "💐",
        name: "Mixed bouquet",
        desc: "Have concepts at every growth stage (🌱🌿🌷🌳) at the same time",
        test: (p) => [1, 2, 3, 4].every((level) => Object.keys(p.concepts).some((id) => levelOf(p, id) === level)),
    },
    {
        id: "alphabet-soup",
        icon: "🔤",
        name: "Alphabet soup",
        desc: "Meet 5 acronyms",
        test: (p) => Object.values(p.terms ?? {}).filter((t) => /^[A-Z][A-Z0-9]{1,5}s?$/.test(t.name)).length >= 5,
    },
    {
        id: "flawless",
        icon: "💎",
        name: "Flawless",
        desc: "Get 5 or more quiz questions right in one day without a wrong answer",
        test: (p) => {
            const days = new Map();
            for (const e of quizAnswers(p)) {
                const day = days.get(e.at.slice(0, 10)) ?? { correct: 0, incorrect: 0 };
                day[e.event] += 1;
                days.set(e.at.slice(0, 10), day);
            }
            return [...days.values()].some((d) => d.correct >= 5 && d.incorrect === 0);
        },
    },
    {
        id: "anniversary",
        icon: "🎂",
        name: "Happy bootcamp birthday",
        desc: "Learn something a year after you started",
        test: (p) => p.log.some((e) => Date.parse(e.at) - Date.parse(p.createdAt) >= 365 * DAY),
    },
];

const tierKey = (id, index) => (index === 0 ? id : `${id}-${index + 1}`);

const ACHIEVEMENTS = [
    ...TIERED_ACHIEVEMENTS.flatMap((a) =>
        a.tiers.map(([threshold, name, desc], i) => ({
            key: tierKey(a.id, i),
            group: a.id,
            icon: a.icon,
            medal: MEDALS[i],
            name,
            desc,
            test: (p, c) => a.count(p, c) >= threshold,
        })),
    ),
    ...HIDDEN_ACHIEVEMENTS.map((a) => ({ ...a, key: a.id, hidden: true })),
];

// Familiarity with a term grows with the number of separate stops it's been used in.
const TERM_FAMILIARITY = [
    [4, "🌳", "familiar"],
    [2, "🌿", "getting familiar"],
    [1, "🌱", "new"],
];

// ---------- arguments ----------

function parseArgs(argv) {
    const positional = [];
    const options = {};
    for (let i = 0; i < argv.length; i++) {
        const arg = argv[i];
        if (arg.startsWith("--")) {
            const key = arg.slice(2);
            const next = argv[i + 1];
            if (next === undefined || next.startsWith("--")) {
                options[key] = true;
            } else {
                options[key] = next;
                i++;
            }
        } else {
            positional.push(arg);
        }
    }
    return { positional, options };
}

function fail(message) {
    console.error(`Error: ${message}`);
    process.exit(1);
}

function list(value) {
    if (!value || value === true) return [];
    return String(value).split(",").map((s) => s.trim()).filter(Boolean);
}

// ---------- data ----------

function loadProgress() {
    if (!fs.existsSync(PROGRESS_PATH)) return null;
    return JSON.parse(fs.readFileSync(PROGRESS_PATH, "utf8"));
}

function newProgress() {
    return { version: 1, createdAt: new Date().toISOString(), profile: {}, concepts: {}, customConcepts: [], log: [] };
}

function saveProgress(progress) {
    fs.mkdirSync(HOME, { recursive: true });
    const tmp = `${PROGRESS_PATH}.tmp`;
    fs.writeFileSync(tmp, `${JSON.stringify(progress, null, 2)}\n`);
    fs.renameSync(tmp, PROGRESS_PATH);
}

function loadCurriculum(progress) {
    const curriculum = JSON.parse(fs.readFileSync(CURRICULUM_PATH, "utf8"));
    const modules = curriculum.modules.map((m) => ({ ...m, concepts: [...m.concepts] }));
    const custom = { ...CUSTOM_MODULE, concepts: [] };
    for (const concept of progress?.customConcepts ?? []) {
        const target = modules.find((m) => m.id === concept.module) ?? custom;
        target.concepts.push({ ...concept, custom: true });
    }
    if (custom.concepts.length) modules.push(custom);
    const byId = new Map();
    for (const module of modules) {
        for (const concept of module.concepts) byId.set(concept.id, { ...concept, module: module.id });
    }
    return { ...curriculum, modules, byId };
}

function stateOf(progress, id) {
    return progress?.concepts?.[id] ?? { level: 0 };
}

function levelOf(progress, id) {
    return stateOf(progress, id).level ?? 0;
}

function isDue(state, now = Date.now()) {
    return state.level > 0 && state.due && Date.parse(state.due) <= now;
}

function resolveAnchor(curriculum, anchor) {
    for (const [prefix, base] of Object.entries(curriculum.anchorPrefixes ?? {})) {
        if (anchor.startsWith(prefix)) return base + anchor.slice(prefix.length);
    }
    return anchor;
}

// ---------- levelling ----------

function applyEvent(state, event, difficulty) {
    const before = state.level ?? 0;
    let level = before;
    let dueNow = false;
    switch (event) {
        case "explained":
            level = Math.max(level, 1);
            break;
        case "correct":
            if (difficulty === "explain") level = Math.max(level, before >= 3 ? 4 : before >= 2 ? 3 : 2);
            else if (difficulty === "apply") level = Math.max(level, before >= 2 ? 3 : 2);
            else level = Math.max(level, 2);
            break;
        case "incorrect":
        case "fuzzy":
            level = Math.max(1, level - 1);
            dueNow = true;
            break;
        case "confident":
            // Self-rating is recorded but never raises the level - only correct answers do.
            level = Math.max(level, 1);
            state.selfRatedConfident = true;
            break;
    }
    state.level = level;
    state.lastSeen = new Date().toISOString();
    state.due = new Date(Date.now() + (dueNow ? 0 : REVIEW_DAYS[level] * DAY)).toISOString();
    return { before, after: level };
}

// ---------- formatting ----------

function bar(fraction, width = 10) {
    const filled = Math.round(fraction * width);
    return "🟩".repeat(filled) + "⬜".repeat(width - filled);
}

function levelLabel(level) {
    return `${LEVEL_ICONS[level]} L${level} ${LEVEL_NAMES[level]}`;
}

function levelKey() {
    return `Key: ${LEVEL_NAMES.map((n, i) => `${LEVEL_ICONS[i]} ${n}`).join(" · ")}`;
}

function moduleLabel(module) {
    return `${MODULE_ICONS[module.id] ?? "📦"} ${module.name}`;
}

function rankOf(fraction) {
    const index = RANKS.findLastIndex(([min]) => fraction >= min);
    return { current: RANKS[index], next: RANKS[index + 1] };
}

// Saves newly earned achievements onto progress; the caller persists it.
function checkAchievements(curriculum, progress) {
    progress.achievements = progress.achievements ?? {};
    const unlocked = ACHIEVEMENTS.filter((a) => !progress.achievements[a.key] && a.test(progress, curriculum));
    for (const a of unlocked) progress.achievements[a.key] = new Date().toISOString();
    return unlocked;
}

function achievementLabel(a) {
    return `${a.icon}${a.medal ? ` ${a.medal}` : ""} ${a.name}`;
}

function announce(unlocked) {
    for (const a of unlocked) console.log(`🏆 ${a.hidden ? "Secret achievement" : "Achievement"} unlocked: ${achievementLabel(a)} — ${a.desc}`);
}

// One badge per tiered achievement (its best medal so far), plus each secret found.
function badges(progress) {
    const earned = ACHIEVEMENTS.filter((a) => progress.achievements?.[a.key]);
    const tiered = TIERED_ACHIEVEMENTS.map((t) => earned.filter((a) => a.group === t.id).at(-1))
        .filter(Boolean)
        .map((a) => `${a.icon}${a.medal}`);
    return { earned, list: [...tiered, ...earned.filter((a) => a.hidden).map((a) => a.icon)] };
}

function pct(fraction) {
    return `${Math.round(fraction * 100)}%`;
}

function moduleStats(module, progress) {
    const levels = module.concepts.map((c) => levelOf(progress, c.id));
    const points = levels.reduce((a, b) => a + b, 0);
    return {
        total: module.concepts.length,
        seen: levels.filter((l) => l > 0).length,
        confident: levels.filter((l) => l >= 3).length,
        fraction: module.concepts.length ? points / (4 * module.concepts.length) : 0,
        points,
    };
}

function relativeDays(iso) {
    const days = Math.round((Date.parse(iso) - Date.now()) / DAY);
    if (days === 0) return "today";
    return days < 0 ? `${-days}d overdue` : `in ${days}d`;
}

function escapeMermaid(text) {
    return text.replace(/"/g, "#quot;");
}

function nodeId(id) {
    return id.replace(/[^A-Za-z0-9]/g, "_");
}

// ---------- suggestion ----------

function gitContext(base) {
    const run = (cmd) => {
        try {
            return execSync(cmd, { encoding: "utf8", stdio: ["ignore", "pipe", "ignore"], maxBuffer: 64 * 1024 * 1024 });
        } catch {
            return "";
        }
    };
    // Exclude the bootcamp's own files: the curriculum contains every keyword.
    const exclude = `-- . ":(exclude).github/skills/spring-boot-camp"`;
    const committed = run(`git diff ${base}...HEAD ${exclude}`);
    const uncommitted = run(`git diff HEAD ${exclude}`);
    const untracked = run(`git ls-files --others --exclude-standard ${exclude}`);
    return `${committed}\n${uncommitted}\n${untracked}`;
}

function relevantText(raw) {
    // Keep file names and added/removed lines from diffs; keep plain text as-is.
    const lines = raw.split(/\r?\n/);
    const isDiff = lines.some((l) => l.startsWith("diff --git"));
    if (!isDiff) return raw;
    return lines
        .filter((l) => l.startsWith("diff --git") || ((l.startsWith("+") || l.startsWith("-")) && !l.startsWith("+++") && !l.startsWith("---")))
        .join("\n");
}

function scoreConcepts(curriculum, text) {
    const lower = text.toLowerCase();
    const results = [];
    for (const concept of curriculum.byId.values()) {
        // Code-like keywords (annotations, paths, CamelCase) are strong, case-sensitive signals;
        // plain words are weak, case-insensitive signals mainly useful for ticket descriptions.
        let relevance = 0;
        const hits = [];
        for (const keyword of concept.keywords ?? []) {
            const specific = /[A-Z@./(:_{#$<!?]/.test(keyword);
            if (specific ? text.includes(keyword) : lower.includes(keyword.toLowerCase())) {
                relevance += specific ? 2 : 0.5;
                hits.push(keyword);
            }
        }
        for (const anchor of concept.anchors ?? []) {
            const file = resolveAnchor(curriculum, anchor);
            if (file.includes(".") && text.includes(file)) relevance += 3;
        }
        if (relevance > 0) results.push({ concept, relevance, hits });
    }
    return results;
}

function learningValue(concept, progress) {
    const state = stateOf(progress, concept.id);
    if (isDue(state)) return 3;
    return [4, 3, 2, 0.5, 0.1][state.level ?? 0];
}

function unmetPrereqs(concept, curriculum, progress) {
    return (concept.prereqs ?? []).filter((p) => curriculum.byId.has(p) && levelOf(progress, p) < 1);
}

function suggest(curriculum, progress, { text, concepts, limit }) {
    let candidates;
    if (concepts.length) {
        candidates = concepts.map((id) => {
            const concept = curriculum.byId.get(id);
            if (!concept) fail(`Unknown concept '${id}'. Use 'concepts --search' or 'add-concept'.`);
            return { concept, relevance: 3, hits: [] };
        });
    } else if (text) {
        candidates = scoreConcepts(curriculum, text);
    } else {
        candidates = [...curriculum.byId.values()].map((concept) => ({ concept, relevance: 1, hits: [] }));
    }

    return candidates
        .map(({ concept, relevance, hits }) => {
            const startWith = unmetPrereqs(concept, curriculum, progress);
            const readiness = startWith.length ? 0.6 : 1;
            const score = relevance * learningValue(concept, progress) * (1 + 1 / concept.tier) * readiness;
            return { concept, score, hits, startWith };
        })
        .filter((s) => s.score > 0.5)
        .sort((a, b) => b.score - a.score)
        .slice(0, limit);
}

// ---------- commands ----------

function cmdStatus(curriculum, progress) {
    if (!progress) {
        console.log("Spring Boot Camp not started yet. Run: profile --spring <none|some|solid> --kotlin <...> --java <...>");
        return;
    }
    const modules = [...curriculum.modules].sort((a, b) => order(a.id) - order(b.id));
    const all = modules.flatMap((m) => m.concepts);
    const totals = moduleStats({ concepts: all }, progress);
    const states = Object.entries(progress.concepts);
    const due = states.filter(([, s]) => isDue(s));
    const unlocked = checkAchievements(curriculum, progress);
    if (unlocked.length) saveProgress(progress);
    const { current, next: nextRank } = rankOf(totals.fraction);
    console.log(`🏕️ Spring Boot Camp — ${current[1]} ${current[2]} · ${pct(totals.fraction)} overall · ${totals.seen}/${totals.total} concepts seen · ${totals.confident} confident+ · ${due.length} due for review`);
    if (nextRank) console.log(`   Next rank: ${nextRank[1]} ${nextRank[2]} at ${pct(nextRank[0])}`);
    const width = Math.max(...modules.map((m) => m.name.length));
    for (const module of modules) {
        const s = moduleStats(module, progress);
        console.log(`  ${MODULE_ICONS[module.id] ?? "📦"} ${module.name.padEnd(width)}  ${bar(s.fraction)} ${pct(s.fraction).padStart(4)}  (${s.seen}/${s.total} seen)`);
    }
    if (due.length) {
        const items = due
            .sort(([, a], [, b]) => Date.parse(a.due) - Date.parse(b.due))
            .slice(0, 5)
            .map(([id, s]) => `${id} (${LEVEL_ICONS[s.level]} L${s.level}, ${relativeDays(s.due)})`);
        console.log(`⏰ Due for review: ${items.join(", ")}`);
    }
    const next = suggest(curriculum, progress, { text: "", concepts: [], limit: 3 }).map((s) => s.concept.id);
    if (next.length) console.log(`👉 Next up (no ticket context): ${next.join(", ")}`);
    const { earned, list } = badges(progress);
    console.log(`🏅 Achievements ${earned.length}/${ACHIEVEMENTS.length}: ${list.join(" ") || "none yet"}`);
    announce(unlocked);
    console.log(levelKey());
}

function order(moduleId) {
    const index = LEARNING_PATH.indexOf(moduleId);
    return index === -1 ? LEARNING_PATH.length : index;
}

function cmdProfile(progress, options) {
    progress = progress ?? newProgress();
    let changed = false;
    for (const key of ["spring", "kotlin", "java"]) {
        if (options[key] === undefined) continue;
        if (!EXPERIENCE.includes(options[key])) fail(`--${key} must be one of ${EXPERIENCE.join("|")}`);
        progress.profile[key] = options[key];
        changed = true;
    }
    if (options.mode !== undefined) {
        progress.profile.preferredMode = options.mode;
        changed = true;
    }
    if (changed || !fs.existsSync(PROGRESS_PATH)) saveProgress(progress);
    console.log(JSON.stringify({ progressFile: PROGRESS_PATH, profile: progress.profile }, null, 2));
}

function cmdRecord(curriculum, progress, positional, options) {
    const [id, event] = positional;
    if (!id || !event) fail("Usage: record <concept-id> <event> [--difficulty recall|apply|explain] [--ticket PDJB-123] [--note text]");
    if (!curriculum.byId.has(id)) fail(`Unknown concept '${id}'. Add it first with 'add-concept'.`);
    if (!EVENTS.includes(event)) fail(`Event must be one of ${EVENTS.join("|")}`);
    const difficulty = options.difficulty ?? (event === "correct" || event === "incorrect" ? "recall" : undefined);
    if (difficulty && !DIFFICULTIES.includes(difficulty)) fail(`--difficulty must be one of ${DIFFICULTIES.join("|")}`);

    progress = progress ?? newProgress();
    const state = progress.concepts[id] ?? { level: 0, history: [] };
    state.history = state.history ?? [];
    const { before, after } = applyEvent(state, event, difficulty);
    const entry = { at: new Date().toISOString(), event };
    if (difficulty) entry.difficulty = difficulty;
    if (options.ticket && options.ticket !== true) entry.ticket = options.ticket;
    if (options.note && options.note !== true) entry.note = options.note;
    state.history.push(entry);
    progress.concepts[id] = state;
    progress.log.push({ concept: id, ...entry });
    const unlocked = checkAchievements(curriculum, progress);
    saveProgress(progress);

    const change = before === after ? `stays ${levelLabel(after)}` : `${LEVEL_ICONS[before]} → ${levelLabel(after)}${after > before ? " ⬆️" : ""}`;
    console.log(`${id}: ${event}${difficulty ? ` (${difficulty})` : ""} — ${change}; review ${relativeDays(state.due)}`);
    announce(unlocked);
}

function cmdConcepts(curriculum, progress, options) {
    const search = typeof options.search === "string" ? options.search.toLowerCase() : null;
    for (const module of curriculum.modules) {
        if (typeof options.module === "string" && module.id !== options.module) continue;
        const concepts = module.concepts.filter((c) => {
            if (!search) return true;
            return [c.id, c.name, c.summary, ...(c.keywords ?? [])].join(" ").toLowerCase().includes(search);
        });
        if (!concepts.length) continue;
        console.log(`[${module.id}] ${moduleLabel(module)}`);
        for (const c of concepts) {
            const state = stateOf(progress, c.id);
            const dueMark = isDue(state) ? " ⏰" : "";
            console.log(`  ${LEVEL_ICONS[state.level ?? 0]} L${state.level ?? 0} T${c.tier} ${c.id} — ${c.name}${dueMark}${c.custom ? " [custom]" : ""}`);
        }
    }
}

function cmdShow(curriculum, progress, id) {
    const concept = curriculum.byId.get(id);
    if (!concept) fail(`Unknown concept '${id}'.`);
    const state = stateOf(progress, id);
    const prereqs = (concept.prereqs ?? []).map((p) => `${p} (L${levelOf(progress, p)})`);
    const unlocks = [...curriculum.byId.values()].filter((c) => (c.prereqs ?? []).includes(id)).map((c) => c.id);
    const lines = [
        `${concept.name} [${concept.id}] — module ${concept.module}, tier ${concept.tier}`,
        `Level: ${levelLabel(state.level ?? 0)}${state.due ? `, review ${relativeDays(state.due)}` : ""}${state.selfRatedConfident ? ", self-rated confident" : ""}`,
        `Summary: ${concept.summary}`,
        `Why: ${concept.why}`,
        `Prereqs: ${prereqs.join(", ") || "none"}`,
        `Unlocks: ${unlocks.join(", ") || "none"}`,
        `Anchors: ${(concept.anchors ?? []).map((a) => resolveAnchor(curriculum, a)).join(", ") || "none"}`,
    ];
    if (concept.readMore?.length) lines.push(`Read more: ${concept.readMore.map((a) => resolveAnchor(curriculum, a)).join(", ")}`);
    const history = (state.history ?? []).slice(-5).map((h) => `${h.at.slice(0, 10)} ${h.event}${h.difficulty ? `/${h.difficulty}` : ""}${h.ticket ? ` ${h.ticket}` : ""}${h.note ? ` — ${h.note}` : ""}`);
    if (history.length) lines.push(`Recent: ${history.join("; ")}`);
    console.log(lines.join("\n"));
}

function cmdAddConcept(curriculum, progress, options) {
    const required = ["id", "name", "summary"];
    for (const key of required) if (typeof options[key] !== "string") fail(`add-concept needs --${key}`);
    if (!/^[a-z0-9-]+$/.test(options.id)) fail("--id must be lowercase kebab-case");
    if (curriculum.byId.has(options.id)) fail(`Concept '${options.id}' already exists.`);
    const moduleId = typeof options.module === "string" ? options.module : "custom";
    if (moduleId !== "custom" && !curriculum.modules.some((m) => m.id === moduleId)) {
        fail(`Unknown module '${moduleId}'. Use one of: ${curriculum.modules.map((m) => m.id).join(", ")}, custom`);
    }
    const prereqs = list(options.prereqs);
    for (const p of prereqs) if (!curriculum.byId.has(p)) fail(`Unknown prereq '${p}'.`);
    const tier = Number(options.tier ?? 2);
    if (![1, 2, 3].includes(tier)) fail("--tier must be 1, 2 or 3");

    progress = progress ?? newProgress();
    progress.customConcepts.push({
        id: options.id,
        name: options.name,
        module: moduleId,
        tier,
        prereqs,
        summary: options.summary,
        why: typeof options.why === "string" ? options.why : "",
        keywords: list(options.keywords),
        anchors: list(options.anchors),
        addedAt: new Date().toISOString(),
    });
    const unlocked = checkAchievements(curriculum, progress);
    saveProgress(progress);
    console.log(`Added custom concept '${options.id}' to module '${moduleId}'. Consider proposing it for curriculum.json if it's widely useful.`);
    announce(unlocked);
}

function familiarity(term) {
    return TERM_FAMILIARITY.find(([min]) => term.uses >= min);
}

function cmdTerm(curriculum, progress, positional, options) {
    if (!positional.length) fail('Usage: term "<term>" ["<term>" ...] [--gloss "plain meaning"] [--concept id]');
    if (positional.length > 1 && (options.gloss || options.concept)) fail("--gloss and --concept need a single term.");
    if (typeof options.concept === "string" && !curriculum.byId.has(options.concept)) fail(`Unknown concept '${options.concept}'.`);
    progress = progress ?? newProgress();
    progress.terms = progress.terms ?? {};
    const now = new Date().toISOString();
    for (const name of positional) {
        const key = name.toLowerCase();
        const term = progress.terms[key] ?? { name, uses: 0, firstSeen: now };
        term.uses += 1;
        term.lastSeen = now;
        if (typeof options.gloss === "string") term.gloss = options.gloss;
        if (typeof options.concept === "string") term.concept = options.concept;
        progress.terms[key] = term;
        const [, icon, label] = familiarity(term);
        console.log(`${icon} ${term.name} — ${label} (${term.uses} stop${term.uses === 1 ? "" : "s"})${term.gloss ? `: ${term.gloss}` : ""}`);
    }
    const unlocked = checkAchievements(curriculum, progress);
    saveProgress(progress);
    announce(unlocked);
}

function cmdTerms(progress, options) {
    const search = typeof options.search === "string" ? options.search.toLowerCase() : null;
    const terms = Object.values(progress?.terms ?? {})
        .filter((t) => !search || `${t.name} ${t.gloss ?? ""}`.toLowerCase().includes(search))
        .sort((a, b) => a.uses - b.uses || a.name.localeCompare(b.name));
    if (!terms.length) {
        console.log("📖 No terms yet. Every term is new: introduce or gloss it.");
        return;
    }
    console.log(`📖 ${terms.length} terms met (🌱 new · 🌿 getting familiar · 🌳 familiar)`);
    for (const t of terms) {
        const [, icon] = familiarity(t);
        console.log(`  ${icon} ${t.name} (${t.uses})${t.gloss ? ` — ${t.gloss}` : ""}${t.concept ? ` [${t.concept}]` : ""}`);
    }
}

function cmdAchievements(curriculum, progress) {
    if (!progress) fail("Spring Boot Camp not started yet.");
    const unlocked = checkAchievements(curriculum, progress);
    if (unlocked.length) saveProgress(progress);
    const { earned } = badges(progress);
    console.log(`🏅 Achievements ${earned.length}/${ACHIEVEMENTS.length} (🥉 → 🥈 → 🥇)`);
    for (const t of TIERED_ACHIEVEMENTS) {
        const done = t.tiers.filter((_, i) => progress.achievements[tierKey(t.id, i)]).length;
        const medals = t.tiers.map((_, i) => (i < done ? MEDALS[i] : "⚪")).join("");
        if (done === t.tiers.length) {
            console.log(`  ${t.icon} ${medals} ${t.tiers[done - 1][1]} · all tiers done!`);
            continue;
        }
        const [threshold, name, desc] = t.tiers[done];
        const head = done ? `${t.icon} ${medals} ${t.tiers[done - 1][1]} · next ${MEDALS[done]} ${name}` : `🔒 ${medals} ${name}`;
        console.log(`  ${head}: ${desc} (${Math.min(t.count(progress, curriculum), threshold)}/${threshold})`);
    }
    const found = HIDDEN_ACHIEVEMENTS.filter((a) => progress.achievements[a.id]);
    console.log(`🤫 Secret achievements ${found.length}/${HIDDEN_ACHIEVEMENTS.length}`);
    for (const a of found) console.log(`  ${a.icon} ${a.name} — ${a.desc} (${progress.achievements[a.id].slice(0, 10)})`);
    const left = HIDDEN_ACHIEVEMENTS.length - found.length;
    if (left) console.log(`  ❓ ${left} still hidden.`);
}

function cmdMap(curriculum, progress, options) {
    const out = ["```mermaid", "flowchart LR"];
    LEVEL_NAMES.forEach((_, i) => out.push(`  classDef L${i} fill:${MAP_COLOURS[i]},stroke:#57534e,color:#1c1917`));
    const modules = [...curriculum.modules].sort((a, b) => order(a.id) - order(b.id));

    if (typeof options.module !== "string" && !options.all) {
        for (const module of modules) {
            const s = moduleStats(module, progress);
            const level = Math.round(s.fraction * 4);
            out.push(`  ${nodeId(module.id)}["${escapeMermaid(moduleLabel(module))}<br/>${LEVEL_ICONS[level]} ${pct(s.fraction)} · ${s.seen}/${s.total} seen"]:::L${level}`);
        }
        const pathModules = LEARNING_PATH.filter((id) => modules.some((m) => m.id === id));
        for (let i = 1; i < pathModules.length; i++) out.push(`  ${nodeId(pathModules[i - 1])} --> ${nodeId(pathModules[i])}`);
    } else {
        const selected = typeof options.module === "string" ? modules.filter((m) => m.id === options.module) : modules;
        if (!selected.length) fail(`Unknown module '${options.module}'.`);
        const shown = new Set(selected.flatMap((m) => m.concepts.map((c) => c.id)));
        for (const module of selected) {
            out.push(`  subgraph ${nodeId(module.id)}["${escapeMermaid(moduleLabel(module))}"]`);
            for (const c of module.concepts) {
                const level = levelOf(progress, c.id);
                const due = isDue(stateOf(progress, c.id)) ? " ⏰" : "";
                out.push(`    ${nodeId(c.id)}["${LEVEL_ICONS[level]} ${escapeMermaid(c.name)}${due}"]:::L${level}`);
            }
            out.push("  end");
        }
        for (const id of shown) {
            for (const p of curriculum.byId.get(id).prereqs ?? []) {
                if (shown.has(p)) out.push(`  ${nodeId(p)} --> ${nodeId(id)}`);
            }
        }
    }
    out.push("```");
    out.push(levelKey());
    console.log(out.join("\n"));
}

function defaultBase() {
    for (const ref of ["origin/main", "main"]) {
        try {
            execSync(`git rev-parse --verify --quiet ${ref}`, { stdio: "ignore" });
            return ref;
        } catch {
            // try the next candidate
        }
    }
    return "HEAD";
}

function cmdSuggest(curriculum, progress, options) {
    let text = typeof options.text === "string" ? options.text : "";
    if (options.git) text += `\n${relevantText(gitContext(typeof options.git === "string" ? options.git : defaultBase()))}`;
    if (typeof options.file === "string") text += `\n${relevantText(fs.readFileSync(options.file, "utf8"))}`;
    for (const file of list(options.files)) {
        if (!fs.existsSync(file)) fail(`File not found: ${file}`);
        text += `\n${file.replace(/\\/g, "/")}\n${fs.readFileSync(file, "utf8")}`;
    }
    if (options.stdin) text += `\n${relevantText(fs.readFileSync(0, "utf8"))}`;
    const limit = Number(options.limit ?? 5);
    const sourceGiven = options.git || options.file || options.files || options.stdin || typeof options.text === "string";
    if (sourceGiven && !text.trim()) console.log("(No ticket changes or text found - showing general next steps.)");
    const results = suggest(curriculum, progress, { text: text.trim(), concepts: list(options.concepts), limit });
    if (!results.length) {
        console.log("No matching concepts. Plain ticket wording rarely matches - pass the related code with --files a.kt,b.kt, or pick from 'concepts'.");
        return;
    }
    for (const r of results) {
        const state = stateOf(progress, r.concept.id);
        const why = [];
        if (isDue(state)) why.push("due for review");
        if (r.hits.length) why.push(`matched ${r.hits.slice(0, 4).map((h) => JSON.stringify(h)).join(", ")}`);
        if (r.startWith.length) why.push(`unseen prereqs - start with: ${r.startWith.join(", ")}`);
        console.log(`${r.concept.id} — L${state.level ?? 0} ${LEVEL_NAMES[state.level ?? 0]}, tier ${r.concept.tier}, score ${r.score.toFixed(1)}${why.length ? ` (${why.join("; ")})` : ""}`);
    }
}

function cmdHistory(progress, options) {
    const limit = Number(options.limit ?? 15);
    for (const e of (progress?.log ?? []).slice(-limit)) {
        console.log(`${e.at.slice(0, 16).replace("T", " ")} ${e.concept} ${e.event}${e.difficulty ? `/${e.difficulty}` : ""}${e.ticket ? ` ${e.ticket}` : ""}${e.note ? ` — ${e.note}` : ""}`);
    }
}

function cmdReset(options) {
    if (options.yes !== true) fail("This deletes all your bootcamp progress. Re-run with --yes to confirm.");
    if (fs.existsSync(PROGRESS_PATH)) fs.rmSync(PROGRESS_PATH);
    console.log(`Deleted ${PROGRESS_PATH}`);
}

const HELP = `Spring Boot Camp progress tracker
Progress file: ${PROGRESS_PATH}

  status                                   Overall and per-module progress, due reviews, next up
  profile [--spring|--kotlin|--java none|some|solid] [--mode quick|deep|quiz]
                                           Create/update your profile (shows it when no options)
  suggest [--git [base, default origin/main]] [--files a.kt,b.kt] [--file diff.txt] [--stdin] [--text "..."]
          [--concepts a,b] [--limit N]
                                           Rank concepts to teach for this ticket
  record <id> <explained|correct|incorrect|fuzzy|confident> [--difficulty recall|apply|explain]
         [--ticket PDJB-123] [--note "..."]  Record a teaching/quiz outcome
  show <id>                                Concept details, anchors and your history
  concepts [--module id] [--search text]   List concepts with your level
  map [--module id | --all]                Mermaid progress map (module overview by default)
  achievements                             Earned and locked achievements
  term "<term>" ["<term>" ...] [--gloss "plain meaning"] [--concept id]
                                           Record terminology used in a tour stop (once per stop)
  terms [--search text]                    Terminology met so far, least familiar first
  add-concept --id x --name "..." --summary "..." [--module id] [--tier 1|2|3] [--why "..."]
              [--prereqs a,b] [--keywords a,b] [--anchors a,b]
                                           Track a concept that isn't in the curriculum yet
  history [--limit N]                      Recent learning events
  reset --yes                              Delete your progress`;

function main() {
    const [command = "help", ...rest] = process.argv.slice(2);
    const { positional, options } = parseArgs(rest);
    const progress = loadProgress();
    const curriculum = loadCurriculum(progress);
    switch (command) {
        case "status":
            return cmdStatus(curriculum, progress);
        case "profile":
            return cmdProfile(progress, options);
        case "record":
            return cmdRecord(curriculum, progress, positional, options);
        case "concepts":
            return cmdConcepts(curriculum, progress, options);
        case "show":
            return cmdShow(curriculum, progress, positional[0]);
        case "add-concept":
            return cmdAddConcept(curriculum, progress, options);
        case "map":
            return cmdMap(curriculum, progress, options);
        case "achievements":
            return cmdAchievements(curriculum, progress);
        case "term":
            return cmdTerm(curriculum, progress, positional, options);
        case "terms":
            return cmdTerms(progress, options);
        case "suggest":
            return cmdSuggest(curriculum, progress, options);
        case "history":
            return cmdHistory(progress, options);
        case "reset":
            return cmdReset(options);
        default:
            console.log(HELP);
    }
}

main();
