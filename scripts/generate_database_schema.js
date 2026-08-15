#!/usr/bin/env node

const childProcess = require('node:child_process');
const fs = require('node:fs');
const path = require('node:path');

const repositoryRoot = path.resolve(__dirname, '..');
const migrationsDirectory = path.join(repositoryRoot, 'src/main/resources/db/migrations');

function parseArguments(argumentsList) {
    const options = {
        composeFile: 'docker-compose.local.yml',
        database: 'prsdblocal',
        output: 'docs/database-schema.mmd',
        schema: 'public',
        service: 'postgres',
        tblsConfig: '.tbls.yml',
        tblsProfile: 'tools',
        tblsService: 'tbls',
        useStagedMigrations: false,
        user: 'postgres',
        useHostPsql: false,
    };

    for (let index = 0; index < argumentsList.length; index += 1) {
        const argument = argumentsList[index];
        if (argument === '--host-psql') {
            options.useHostPsql = true;
        } else if (argument === '--staged-migrations') {
            options.useStagedMigrations = true;
        } else if (argument === '--help' || argument === '-h') {
            options.help = true;
        } else {
            const optionNames = {
                '--compose-file': 'composeFile',
                '--database': 'database',
                '--output': 'output',
                '--schema': 'schema',
                '--service': 'service',
                '--tbls-config': 'tblsConfig',
                '--tbls-profile': 'tblsProfile',
                '--tbls-service': 'tblsService',
                '--user': 'user',
            };
            const optionName = optionNames[argument];
            if (!optionName) {
                throw new Error(`Unknown argument: ${argument}`);
            }
            index += 1;
            if (index >= argumentsList.length) {
                throw new Error(`Missing value for ${argument}`);
            }
            options[optionName] = argumentsList[index];
        }
    }

    return options;
}

function sqlString(value) {
    return `'${value.replaceAll("'", "''")}'`;
}

function sqlIdentifier(value) {
    return `"${value.replaceAll('"', '""')}"`;
}

function buildFlywayHistoryExistsQuery(schema) {
    return `SELECT to_json(pg_catalog.to_regclass(${sqlString(`${schema}.flyway_schema_history`)}) IS NOT NULL)::text;`;
}

function buildFlywayHistoryQuery(schema) {
    return `
SELECT COALESCE(
    json_agg(
        json_build_object(
            'script', script,
            'success', success
        ) ORDER BY installed_rank
    ),
    '[]'::json
)::text
FROM ${sqlIdentifier(schema)}.${sqlIdentifier('flyway_schema_history')}
WHERE type = 'SQL';
`.trim();
}

function shellArgument(value) {
    if (/^[A-Za-z0-9_./:-]+$/.test(value)) {
        return value;
    }
    return `'${value.replaceAll("'", "'\\''")}'`;
}

function toPosixPath(value) {
    return value.split(path.sep).join('/');
}

function formatPsqlError(options, standardError, status) {
    const errorMessage = standardError.trim() || `docker exited with status ${status}`;
    if (options.useHostPsql) {
        return errorMessage;
    }

    const startCommand = [
        'docker',
        'compose',
        '--file',
        shellArgument(options.composeFile),
        'up',
        '--detach',
        shellArgument(options.service),
    ].join(' ');
    return `${errorMessage}\n\nStart the local database with:\n  ${startCommand}`
        + '\n\nThen update its schema by running the IntelliJ "local" configuration, or:'
        + '\n  ./gradlew flywayMigrate';
}

function listFilesystemMigrations(directory = migrationsDirectory) {
    return fs.readdirSync(directory, { withFileTypes: true })
        .filter(entry => entry.isFile() && /^V[^/]*\.sql$/.test(entry.name))
        .map(entry => entry.name)
        .sort(compareNames);
}

function parseGitMigrationPaths(output) {
    return output.split('\0')
        .filter(Boolean)
        .filter(filePath => path.dirname(filePath) === 'src/main/resources/db/migrations')
        .map(filePath => path.basename(filePath))
        .filter(fileName => /^V[^/]*\.sql$/.test(fileName))
        .sort(compareNames);
}

function listStagedMigrations() {
    const result = childProcess.spawnSync(
        'git',
        ['ls-files', '--cached', '-z', '--', 'src/main/resources/db/migrations'],
        { cwd: repositoryRoot, encoding: 'utf8' },
    );
    if (result.error || result.status !== 0) {
        const message = result.error?.message || result.stderr.trim();
        throw new Error(`Could not read staged migrations from Git: ${message}`);
    }
    return parseGitMigrationPaths(result.stdout);
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
        + 'Update the schema by running the IntelliJ "local" configuration, or:\n'
        + '  ./gradlew flywayMigrate',
    );
}

function runPsql(options, query) {
    const psqlArguments = [
        '--no-psqlrc',
        '--quiet',
        '--tuples-only',
        '--no-align',
        '--set',
        'ON_ERROR_STOP=1',
        '--username',
        options.user,
        '--dbname',
        options.database,
        '--command',
        query,
    ];

    const command = options.useHostPsql ? 'psql' : 'docker';
    const commandArguments = options.useHostPsql
        ? psqlArguments
        : ['compose', '--file', options.composeFile, 'exec', '-T', options.service, 'psql', ...psqlArguments];
    const result = childProcess.spawnSync(command, commandArguments, {
        cwd: repositoryRoot,
        encoding: 'utf8',
        env: process.env,
        maxBuffer: 10 * 1024 * 1024,
    });

    if (result.error) {
        throw new Error(`Could not run ${command}: ${result.error.message}`);
    }
    if (result.status !== 0) {
        throw new Error(formatPsqlError(options, result.stderr, result.status));
    }

    try {
        return JSON.parse(result.stdout.trim());
    } catch (error) {
        throw new Error(`psql returned invalid schema metadata: ${error.message}`);
    }
}

function runTbls(options, outputPath) {
    fs.mkdirSync(path.dirname(outputPath), { recursive: true });

    const outputRelativePath = toPosixPath(path.relative(repositoryRoot, outputPath));
    const configRelativePath = toPosixPath(path.relative(repositoryRoot, path.resolve(repositoryRoot, options.tblsConfig)));
    const commandArguments = [
        'compose',
        '--file',
        options.composeFile,
        '--profile',
        options.tblsProfile,
        'run',
        '--rm',
        '--no-deps',
        options.tblsService,
        'out',
        '--config',
        configRelativePath,
        '--format',
        'mermaid',
        '--out',
        outputRelativePath,
    ];
    const result = childProcess.spawnSync('docker', commandArguments, {
        cwd: repositoryRoot,
        encoding: 'utf8',
        env: process.env,
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
    if (left < right) return -1;
    if (left > right) return 1;
    return 0;
}

function printHelp() {
    console.log([
        'Usage: node scripts/generate_database_schema.js [options]',
        '',
        'Generates a Mermaid ER diagram from the local PostgreSQL database using tbls.',
        '',
        'Options:',
        '  --output <path>         Output path relative to the repository root',
        '                          (default: docs/database-schema.mmd)',
        '  --schema <name>         PostgreSQL schema to inspect (default: public)',
        '  --compose-file <path>   Docker Compose file (default: docker-compose.local.yml)',
        '  --service <name>        PostgreSQL Compose service (default: postgres)',
        '  --database <name>       Database name (default: prsdblocal)',
        '  --user <name>           Database user (default: postgres)',
        '  --tbls-config <path>    tbls config path relative to repository root',
        '                          (default: .tbls.yml)',
        '  --tbls-profile <name>   Compose profile used for tbls service',
        '                          (default: tools)',
        '  --tbls-service <name>   tbls Compose service name',
        '                          (default: tbls)',
        '  --host-psql             Use psql from PATH instead of Docker Compose',
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

    const outputPath = path.resolve(repositoryRoot, options.output);
    const expectedMigrations = options.useStagedMigrations
        ? listStagedMigrations()
        : listFilesystemMigrations();

    const flywayHistoryExists = runPsql(options, buildFlywayHistoryExistsQuery(options.schema));
    const databaseMigrations = flywayHistoryExists
        ? runPsql(options, buildFlywayHistoryQuery(options.schema))
        : [];
    assertMigrationsAreCurrent(expectedMigrations, databaseMigrations);
    runTbls(options, outputPath);
    console.log(`Wrote ${path.relative(repositoryRoot, outputPath)}`);
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
    buildFlywayHistoryExistsQuery,
    buildFlywayHistoryQuery,
    formatPsqlError,
    listFilesystemMigrations,
    listStagedMigrations,
    parseArguments,
    parseGitMigrationPaths,
    runTbls,
};