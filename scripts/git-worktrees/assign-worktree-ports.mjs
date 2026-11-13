#!/usr/bin/env node
// Assigns unique local ports in a worktree's .env file so it can run alongside other worktrees.
//
// Usage:
//   node assign-worktree-ports.mjs <worktree-path>
//
// Used by new-worktree, copy-config-files and copilot-app-worktree.mjs so the port logic lives in one place.

import { spawnSync } from "node:child_process";
import { existsSync, readFileSync, writeFileSync } from "node:fs";
import path from "node:path";
import { fileURLToPath } from "node:url";

const DEFAULT_PORTS = { SERVER_PORT: 8080, POSTGRES_PORT: 5433, REDIS_PORT: 6379, ELASTICMQ_PORT: 9324 };
const PORT_LINE = /^(SERVER_PORT|POSTGRES_PORT|REDIS_PORT|ELASTICMQ_PORT)=\D*(\d+)/;

export function normalisePath(p) {
    const resolved = path.resolve(p);
    return process.platform === "win32" ? resolved.toLowerCase() : resolved;
}

function readPorts(envContent) {
    const ports = {};
    for (const line of envContent.split(/\r?\n/)) {
        const match = line.match(PORT_LINE);
        if (match) ports[match[1]] = Number(match[2]);
    }
    return ports;
}

// Ports assigned in the .env files of every other worktree of this repository (including the main checkout).
function findUsedPorts(worktreePath) {
    const result = spawnSync("git", ["worktree", "list", "--porcelain"], {
        cwd: worktreePath,
        encoding: "utf8",
        stdio: ["ignore", "pipe", "ignore"],
    });
    const worktreePaths = (result.status === 0 ? result.stdout : "")
        .split("\n")
        .filter((line) => line.startsWith("worktree "))
        .map((line) => line.slice("worktree ".length).trim());

    const usedPorts = new Set();
    for (const otherPath of worktreePaths) {
        if (normalisePath(otherPath) === normalisePath(worktreePath)) continue;
        const envFile = path.join(otherPath, ".env");
        if (!existsSync(envFile)) continue;
        Object.values(readPorts(readFileSync(envFile, "utf8"))).forEach((port) => usedPorts.add(port));
    }
    return usedPorts;
}

function setEnvVar(content, key, value) {
    const lines = content.split("\n");
    const index = lines.findIndex((line) => line.startsWith(`${key}=`));
    if (index === -1) return `${content.trimEnd()}\n${key}="${value}"\n`;
    lines[index] = `${key}="${value}"${lines[index].endsWith("\r") ? "\r" : ""}`;
    return lines.join("\n");
}

export function assignPorts(worktreePath) {
    const envFile = path.join(worktreePath, ".env");
    if (!existsSync(envFile)) {
        console.warn("No .env found in the worktree, so ports were not assigned.");
        return;
    }

    let content = readFileSync(envFile, "utf8");
    const usedPorts = findUsedPorts(worktreePath);

    // Keep the existing ports if they're already unique, so re-running doesn't change them. A .env copied from
    // another worktree has that worktree's ports, so those are reassigned.
    const currentPorts = readPorts(content);
    const alreadyAssigned = Object.entries(DEFAULT_PORTS).every(
        ([key, defaultPort]) => currentPorts[key] !== undefined && currentPorts[key] !== defaultPort && !usedPorts.has(currentPorts[key]),
    );
    if (alreadyAssigned) {
        console.log(`Ports already assigned (SERVER_PORT=${currentPorts.SERVER_PORT}), leaving .env unchanged.`);
        return;
    }

    // Use the smallest offset (>= 1) for which none of the offset ports are already assigned in another worktree's
    // .env file, so offsets freed by removed worktrees are reused rather than creeping ever higher.
    let offset = 1;
    while (Object.values(DEFAULT_PORTS).some((port) => usedPorts.has(port + offset))) offset++;
    const ports = Object.fromEntries(Object.entries(DEFAULT_PORTS).map(([key, port]) => [key, port + offset]));

    console.log(`Assigning unique ports for parallel execution (offset: ${offset})...`);
    content = content
        .replace(/^(RDS_URL="jdbc:postgresql:\/\/localhost:)\d+(\/prsdblocal")/m, `$1${ports.POSTGRES_PORT}$2`)
        .replace(/^(LANDLORD_BASE_URL="http:\/\/localhost:)\d+(\/landlord")/m, `$1${ports.SERVER_PORT}$2`)
        .replace(/^(LOCAL_AUTHORITY_BASE_URL="http:\/\/localhost:)\d+(\/local-council")/m, `$1${ports.SERVER_PORT}$2`)
        .replace(/^ELASTICACHE_PORT="\d+"/m, `ELASTICACHE_PORT="${ports.REDIS_PORT}"`);
    for (const [key, port] of Object.entries(ports)) {
        content = setEnvVar(content, key, port);
        console.log(`  ${key}=${port}`);
    }
    writeFileSync(envFile, content);
}

if (process.argv[1] && normalisePath(process.argv[1]) === normalisePath(fileURLToPath(import.meta.url))) {
    const worktreePath = process.argv[2];
    if (!worktreePath) {
        console.error("Usage: node assign-worktree-ports.mjs <worktree-path>");
        process.exit(1);
    }
    assignPorts(path.resolve(worktreePath));
}
