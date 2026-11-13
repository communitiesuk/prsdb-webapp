#!/usr/bin/env node
// Sets up (or tears down) a worktree created by the GitHub Copilot app.
// Run by the app via .github/github-app.yml, which provides the COPILOT_* environment variables.
//
//   session.create (or manual run): copy any .worktreeinclude files the app hasn't already copied,
//                                    assign unique local ports in .env, and run npm install.
//   session.archive:                stop the worktree's local Docker containers so their ports are freed.
//
// Does nothing for in-place sessions (where the workspace is the main checkout).

import { spawnSync } from "node:child_process";
import { copyFileSync, existsSync, mkdirSync, readFileSync } from "node:fs";
import path from "node:path";
import { assignPorts, normalisePath } from "./assign-worktree-ports.mjs";

const workspacePath = process.env.COPILOT_WORKSPACE_PATH;
const rootPath = process.env.COPILOT_ROOT_PATH;
const trigger = process.env.COPILOT_SCRIPT_TRIGGER;

function run(command, args, cwd) {
    return spawnSync(command, args, { cwd, stdio: "inherit", shell: process.platform === "win32" });
}

function git(args, cwd) {
    const result = spawnSync("git", args, { cwd, encoding: "utf8", stdio: ["ignore", "pipe", "ignore"] });
    return result.status === 0 ? result.stdout : "";
}

function copyWorktreeIncludeFiles() {
    const includeFile = path.join(rootPath, ".worktreeinclude");
    if (!existsSync(includeFile)) return;

    const pathspecs = readFileSync(includeFile, "utf8")
        .split(/\r?\n/)
        .map((line) => line.trim())
        .filter((line) => line && !line.startsWith("#"))
        .map((line) => (line.startsWith("!") ? `:(exclude)${line.slice(1)}` : line));
    if (pathspecs.length === 0) return;

    const files = git(["ls-files", "--others", "--ignored", "--exclude-standard", "--", ...pathspecs], rootPath)
        .split("\n")
        .filter(Boolean);

    for (const file of files) {
        const dest = path.join(workspacePath, file);
        if (existsSync(dest)) continue;
        mkdirSync(path.dirname(dest), { recursive: true });
        copyFileSync(path.join(rootPath, file), dest);
        console.log(`  Copied ${file}`);
    }
}

function setUp() {
    console.log("Copying .worktreeinclude files missing from the worktree...");
    copyWorktreeIncludeFiles();
    assignPorts(workspacePath);

    console.log("Installing npm dependencies...");
    const result = run("npm", ["install"], workspacePath);
    if (result.status !== 0) process.exit(result.status ?? 1);
}

function tearDown() {
    console.log("Stopping the worktree's local Docker containers...");
    const result = run("docker", ["compose", "-f", "docker-compose.local.yml", "down"], workspacePath);
    if (result.status !== 0) console.warn("Could not stop Docker containers (is Docker running?).");
}

if (!workspacePath || !rootPath) {
    console.error("COPILOT_WORKSPACE_PATH and COPILOT_ROOT_PATH must be set. This script is run by the GitHub Copilot app.");
    process.exit(1);
}
if (normalisePath(workspacePath) === normalisePath(rootPath)) {
    console.log("Session is using the main checkout, so there is no worktree to set up.");
    process.exit(0);
}

if (trigger === "session.archive") {
    tearDown();
} else {
    setUp();
}
