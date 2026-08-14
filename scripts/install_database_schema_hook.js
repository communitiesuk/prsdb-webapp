#!/usr/bin/env node

const childProcess = require('node:child_process');
const fs = require('node:fs');
const path = require('node:path');

const repositoryRoot = path.resolve(__dirname, '..');
const startMarker = '######## DATABASE-SCHEMA-HOOK START ########';
const endMarker = '####### DATABASE-SCHEMA-HOOK END #######';
const hookContent = `${startMarker}

if ! git diff --cached --quiet --diff-filter=ACMRD -- src/main/resources/db/migrations; then
    echo "Staged database migrations changed; regenerating the database schema diagram"
    node scripts/generate_database_schema.js --staged-migrations || exit $?
    git add docs/database-schema.mmd
fi

${endMarker}`;

function insertHook(existingContent) {
    if (existingContent.includes(startMarker)) {
        const startIndex = existingContent.indexOf(startMarker);
        const endIndex = existingContent.indexOf(endMarker, startIndex);
        if (endIndex === -1) {
            throw new Error(`Existing database schema hook is missing its end marker: ${endMarker}`);
        }
        return existingContent.slice(0, startIndex)
            + hookContent
            + existingContent.slice(endIndex + endMarker.length);
    }
    const shebangMatch = existingContent.match(/^#![^\n]*(?:\n|$)/);
    if (shebangMatch) {
        return `${shebangMatch[0]}\n${hookContent}\n\n${existingContent.slice(shebangMatch[0].length)}`;
    }
    return `#!/bin/sh\n\n${hookContent}\n\n${existingContent}`;
}

function main() {
    const hookPathResult = childProcess.spawnSync('git', ['rev-parse', '--git-path', 'hooks/pre-commit'], {
        cwd: repositoryRoot,
        encoding: 'utf8',
    });
    if (hookPathResult.error || hookPathResult.status !== 0) {
        const message = hookPathResult.error?.message || hookPathResult.stderr.trim();
        throw new Error(`Could not locate the Git pre-commit hook: ${message}`);
    }

    const configuredPath = hookPathResult.stdout.trim();
    const hookPath = path.resolve(repositoryRoot, configuredPath);
    const existingContent = fs.existsSync(hookPath) ? fs.readFileSync(hookPath, 'utf8') : '';
    const updatedContent = insertHook(existingContent);

    if (updatedContent === existingContent) {
        console.log('Database schema pre-commit hook is already installed');
        return;
    }

    fs.mkdirSync(path.dirname(hookPath), { recursive: true });
    fs.writeFileSync(hookPath, updatedContent, { encoding: 'utf8', mode: 0o755 });
    fs.chmodSync(hookPath, 0o755);
    console.log(`Installed database schema pre-commit hook at ${configuredPath}`);
}

if (require.main === module) {
    try {
        main();
    } catch (error) {
        console.error(`Hook installation failed: ${error.message}`);
        process.exitCode = 1;
    }
}

module.exports = { insertHook };