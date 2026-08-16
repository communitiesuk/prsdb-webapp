#!/usr/bin/env node

const childProcess = require('node:child_process');
const fs = require('node:fs');
const path = require('node:path');

const repositoryRoot = path.resolve(__dirname, '..');
const migrationsPath = 'src/main/resources/db/migrations';
const migrationsDirectory = path.join(repositoryRoot, migrationsPath);

// Only versioned migrations; the project does not use repeatable (R__) migrations.
const migrationFileNamePattern = /^V.*\.sql$/;

const schemaUpdateGuidance = 'Update the schema by running the IntelliJ "local" configuration, or:\n'
    + '  ./gradlew flywayMigrate';

// These must stay in sync with docker-compose.local.yml and .tbls.yml.
const composeFile = 'docker-compose.local.yml';
const postgresService = 'postgres';
const databaseName = 'prsdblocal';
const databaseUser = 'postgres';
const databaseSchema = 'public';
const tblsService = 'tbls';
const tblsProfile = 'tools';
const tblsConfigPath = '.tbls.yml';
const schemaDiagramPath = 'docs/database-schema.mmd';

const flywayHistoryExistsQuery =
    `SELECT to_json(pg_catalog.to_regclass('${databaseSchema}.flyway_schema_history') IS NOT NULL)::text;`;

const flywayHistoryQuery = `
SELECT COALESCE(
    json_agg(
        json_build_object(
            'script', script,
            'success', success
        ) ORDER BY installed_rank
    ),
    '[]'::json
)::text
FROM ${databaseSchema}.flyway_schema_history
WHERE type = 'SQL';
`.trim();

function parseArguments(argumentsList) {
    const options = {
        help: false,
        useStagedMigrations: false,
    };

    for (const argument of argumentsList) {
        if (argument === '--staged-migrations') {
            options.useStagedMigrations = true;
        } else if (argument === '--help' || argument === '-h') {
            options.help = true;
        } else {
            throw new Error(`Unknown argument: ${argument}`);
        }
    }

    return options;
}

function formatPsqlError(standardError, status) {
    const errorMessage = standardError.trim() || `docker exited with status ${status}`;
    const startCommand = `docker compose --file ${composeFile} up --detach ${postgresService}`;
    return `${errorMessage}\n\nStart the local database with:\n  ${startCommand}\n\n${schemaUpdateGuidance}`;
}

function listFilesystemMigrations(directory = migrationsDirectory) {
    return fs.readdirSync(directory, { withFileTypes: true })
        .filter(entry => entry.isFile() && migrationFileNamePattern.test(entry.name))
        .map(entry => entry.name)
        .sort(compareNames);
}

function parseGitMigrationPaths(output) {
    return output.split('\0')
        .filter(Boolean)
        .filter(filePath => path.dirname(filePath) === migrationsPath)
        .map(filePath => path.basename(filePath))
        .filter(fileName => migrationFileNamePattern.test(fileName))
        .sort(compareNames);
}

function runGitMigrationQuery(gitArguments, description) {
    const result = childProcess.spawnSync(
        'git',
        [...gitArguments, '-z', '--', migrationsPath],
        { cwd: repositoryRoot, encoding: 'utf8' },
    );
    if (result.error || result.status !== 0) {
        const message = result.error?.message || result.stderr.trim();
        throw new Error(`Could not read ${description} from Git: ${message}`);
    }
    return parseGitMigrationPaths(result.stdout);
}

function listStagedMigrations() {
    return runGitMigrationQuery(['ls-files', '--cached'], 'staged migrations');
}

// Migrations changed in the working tree but not staged, so not part of this commit.
function listUnstagedMigrations() {
    return runGitMigrationQuery(['diff', '--name-only'], 'unstaged migrations');
}

function assertStagedMigrationsHaveNoUnstagedChanges(stagedMigrations, unstagedMigrations) {
    const stagedMigrationSet = new Set(stagedMigrations);
    const partiallyStagedMigrations = unstagedMigrations.filter(migration => stagedMigrationSet.has(migration));

    if (partiallyStagedMigrations.length === 0) {
        return;
    }

    throw new Error(
        'Migrations in this commit also have unstaged changes. The Mermaid file was not changed.\n\n'
        + `Partially staged migrations:\n${partiallyStagedMigrations.map(name => `  ${name}`).join('\n')}\n\n`
        + 'The local database was migrated from the working tree, so the diagram would not necessarily\n'
        + 'match the migrations being committed. Stage or stash the remaining changes, then commit again.',
    );
}

function assertMigrationsAreCurrent(filesystemMigrations, databaseMigrations) {
    const successfulDatabaseMigrations = new Set(
        databaseMigrations.filter(migration => migration.success).map(migration => migration.script),
    );
    const allDatabaseMigrations = new Set(databaseMigrations.map(migration => migration.script));
    const filesystemMigrationSet = new Set(filesystemMigrations);
    const missingMigrations = filesystemMigrations
        .filter(migration => !successfulDatabaseMigrations.has(migration))
        .sort(compareNames);
    const unexpectedMigrations = [...allDatabaseMigrations]
        .filter(migration => !filesystemMigrationSet.has(migration))
        .sort(compareNames);

    if (missingMigrations.length === 0 && unexpectedMigrations.length === 0) {
        return;
    }

    const details = [];
    if (missingMigrations.length > 0) {
        details.push(`Missing or unsuccessful database migrations:\n${missingMigrations.map(name => `  ${name}`).join('\n')}`);
    }
    if (unexpectedMigrations.length > 0) {
        details.push(`Database migrations not present in the expected migration set:\n${unexpectedMigrations.map(name => `  ${name}`).join('\n')}`);
    }
    throw new Error(
        'The running database does not match the expected migrations. The Mermaid file was not changed.\n\n'
        + `${details.join('\n\n')}\n\n`
        + schemaUpdateGuidance,
    );
}

function runPsql(query) {
    const commandArguments = [
        'compose',
        '--file',
        composeFile,
        'exec',
        '-T',
        postgresService,
        'psql',
        '--no-psqlrc',
        '--quiet',
        '--tuples-only',
        '--no-align',
        '--set',
        'ON_ERROR_STOP=1',
        '--username',
        databaseUser,
        '--dbname',
        databaseName,
        '--command',
        query,
    ];

    const result = childProcess.spawnSync('docker', commandArguments, {
        cwd: repositoryRoot,
        encoding: 'utf8',
    });

    if (result.error) {
        throw new Error(`Could not run docker: ${result.error.message}`);
    }
    if (result.status !== 0) {
        throw new Error(formatPsqlError(result.stderr, result.status));
    }

    try {
        return JSON.parse(result.stdout.trim());
    } catch (error) {
        throw new Error(`psql returned unreadable Flyway history: ${error.message}`);
    }
}

function runTbls() {
    fs.mkdirSync(path.dirname(path.resolve(repositoryRoot, schemaDiagramPath)), { recursive: true });

    const commandArguments = [
        'compose',
        '--file',
        composeFile,
        '--profile',
        tblsProfile,
        'run',
        '--rm',
        '--no-deps',
        tblsService,
        'out',
        '--config',
        tblsConfigPath,
        '--format',
        'mermaid',
        '--out',
        schemaDiagramPath,
    ];
    const result = childProcess.spawnSync('docker', commandArguments, {
        cwd: repositoryRoot,
        encoding: 'utf8',
    });

    if (result.error) {
        throw new Error(`Could not run docker: ${result.error.message}`);
    }
    if (result.status !== 0) {
        const errorOutput = [result.stdout, result.stderr].filter(Boolean).join('\n').trim();
        const message = errorOutput || `docker exited with status ${result.status}`;
        throw new Error(`tbls schema generation failed: ${message}`);
    }
}

function compareNames(left, right) {
    return left.localeCompare(right, undefined, { numeric: true });
}

function printHelp() {
    console.log([
        'Usage: node scripts/generate_database_schema.js [options]',
        '',
        `Generates ${schemaDiagramPath} from the local PostgreSQL database using tbls.`,
        'Connection and output settings come from docker-compose.local.yml and .tbls.yml.',
        '',
        'Options:',
        '  --staged-migrations     Compare Flyway with migrations in the Git index',
        '  -h, --help              Show this help',
    ].join('\n'));
}

function main() {
    const options = parseArguments(process.argv.slice(2));
    if (options.help) {
        printHelp();
        return;
    }

    let expectedMigrations;
    if (options.useStagedMigrations) {
        expectedMigrations = listStagedMigrations();
        assertStagedMigrationsHaveNoUnstagedChanges(expectedMigrations, listUnstagedMigrations());
    } else {
        expectedMigrations = listFilesystemMigrations();
    }

    const flywayHistoryExists = runPsql(flywayHistoryExistsQuery);
    const databaseMigrations = flywayHistoryExists ? runPsql(flywayHistoryQuery) : [];
    assertMigrationsAreCurrent(expectedMigrations, databaseMigrations);
    runTbls();
    console.log(`Wrote ${schemaDiagramPath}`);
}

if (require.main === module) {
    try {
        main();
    } catch (error) {
        console.error(`Schema generation failed: ${error.message}`);
        process.exitCode = 1;
    }
}

module.exports = {
    assertMigrationsAreCurrent,
    assertStagedMigrationsHaveNoUnstagedChanges,
    formatPsqlError,
    listFilesystemMigrations,
    listStagedMigrations,
    parseArguments,
    parseGitMigrationPaths,
};