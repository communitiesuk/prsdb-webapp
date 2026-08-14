#!/usr/bin/env node

const childProcess = require('node:child_process');
const fs = require('node:fs');
const path = require('node:path');

const repositoryRoot = path.resolve(__dirname, '..');
const startMarker = '######## DATABASE-SCHEMA-HOOK START ########';
const hookContent = `${startMarker}

echo "Checking database schema diagram migration manifest"

node scripts/generate_database_schema.js --check-manifest --staged
DATABASE_SCHEMA_EXIT_CODE=$?

if [ $DATABASE_SCHEMA_EXIT_CODE -ne 0 ]; then
    exit $DATABASE_SCHEMA_EXIT_CODE
fi

echo "Completed database schema diagram migration manifest check"

####### DATABASE-SCHEMA-HOOK END #######`;

function insertHook(existingContent) {
    if (existingContent.includes(startMarker)) {
        return existingContent;
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