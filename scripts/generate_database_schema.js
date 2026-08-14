#!/usr/bin/env node

const childProcess = require('node:child_process');
const crypto = require('node:crypto');
const fs = require('node:fs');
const path = require('node:path');

const repositoryRoot = path.resolve(__dirname, '..');
const migrationsDirectory = path.join(repositoryRoot, 'src/main/resources/db/migrations');

function parseArguments(argumentsList) {
    const options = {
        checkManifest: false,
        composeFile: 'docker-compose.local.yml',
        database: 'prsdblocal',
        output: 'docs/database-schema.mmd',
        schema: 'public',
        service: 'postgres',
        user: 'postgres',
        useHostPsql: false,
        useStagedFiles: false,
    };

    for (let index = 0; index < argumentsList.length; index += 1) {
        const argument = argumentsList[index];
        if (argument === '--host-psql') {
            options.useHostPsql = true;
        } else if (argument === '--check-manifest') {
            options.checkManifest = true;
        } else if (argument === '--staged') {
            options.useStagedFiles = true;
        } else if (argument === '--help' || argument === '-h') {
            options.help = true;
        } else {
            const optionNames = {
                '--compose-file': 'composeFile',
                '--database': 'database',
                '--output': 'output',
                '--schema': 'schema',
                '--service': 'service',
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

    if (options.useStagedFiles && !options.checkManifest) {
        throw new Error('--staged can only be used with --check-manifest');
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

function buildMetadataQuery(schema) {
    const schemaLiteral = sqlString(schema);
    return `
WITH selected_tables AS (
    SELECT table_class.oid, table_class.relname AS name
    FROM pg_catalog.pg_class table_class
    JOIN pg_catalog.pg_namespace namespace ON namespace.oid = table_class.relnamespace
    WHERE namespace.nspname = ${schemaLiteral}
      AND table_class.relkind IN ('r', 'p')
            AND table_class.relname <> 'flyway_schema_history'
), schema_tables AS (
    SELECT COALESCE(
        json_agg(
            json_build_object(
                'name', selected_table.name,
                'columns', (
                    SELECT COALESCE(
                        json_agg(
                            json_build_object(
                                'name', attribute.attname,
                                'ordinal', attribute.attnum,
                                'type', pg_catalog.format_type(attribute.atttypid, attribute.atttypmod),
                                'nullable', NOT attribute.attnotnull,
                                'identity', attribute.attidentity <> '',
                                'generated', attribute.attgenerated <> '',
                                'default', pg_catalog.pg_get_expr(attribute_default.adbin, attribute_default.adrelid)
                            ) ORDER BY attribute.attnum
                        ),
                        '[]'::json
                    )
                    FROM pg_catalog.pg_attribute attribute
                    LEFT JOIN pg_catalog.pg_attrdef attribute_default
                      ON attribute_default.adrelid = attribute.attrelid
                     AND attribute_default.adnum = attribute.attnum
                    WHERE attribute.attrelid = selected_table.oid
                      AND attribute.attnum > 0
                      AND NOT attribute.attisdropped
                )
            ) ORDER BY selected_table.name
        ),
        '[]'::json
    ) AS value
    FROM selected_tables selected_table
), schema_constraints AS (
    SELECT COALESCE(
        json_agg(
            json_build_object(
                'name', constraint_record.conname,
                'type', constraint_record.contype,
                'table', selected_table.name,
                'columns', (
                    SELECT json_agg(attribute.attname ORDER BY key_column.ordinality)
                    FROM unnest(constraint_record.conkey) WITH ORDINALITY key_column(attribute_number, ordinality)
                    JOIN pg_catalog.pg_attribute attribute
                      ON attribute.attrelid = constraint_record.conrelid
                     AND attribute.attnum = key_column.attribute_number
                ),
                'referencedTable', referenced_table.relname,
                'referencedColumns', CASE
                    WHEN constraint_record.contype = 'f' THEN (
                        SELECT json_agg(attribute.attname ORDER BY key_column.ordinality)
                        FROM unnest(constraint_record.confkey) WITH ORDINALITY key_column(attribute_number, ordinality)
                        JOIN pg_catalog.pg_attribute attribute
                          ON attribute.attrelid = constraint_record.confrelid
                         AND attribute.attnum = key_column.attribute_number
                    )
                    ELSE '[]'::json
                END
            ) ORDER BY selected_table.name, constraint_record.conname
        ),
        '[]'::json
    ) AS value
    FROM pg_catalog.pg_constraint constraint_record
    JOIN selected_tables selected_table ON selected_table.oid = constraint_record.conrelid
    LEFT JOIN pg_catalog.pg_class referenced_table ON referenced_table.oid = constraint_record.confrelid
    WHERE constraint_record.contype IN ('p', 'u', 'f')
)
SELECT json_build_object(
    'tables', schema_tables.value,
    'constraints', schema_constraints.value
)::text
FROM schema_tables, schema_constraints;
`.trim();
}

function shellArgument(value) {
    if (/^[A-Za-z0-9_./:-]+$/.test(value)) {
        return value;
    }
    return `'${value.replaceAll("'", "'\\''")}'`;
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
        + '\n\nThen initialize its schema by running the IntelliJ "local" configuration, or:'
        + '\n  ./gradlew flywayMigrate';
}

function listFilesystemMigrations(directory = migrationsDirectory) {
    return fs.readdirSync(directory, { withFileTypes: true })
        .filter(entry => entry.isFile() && /^V[^/]*\.sql$/.test(entry.name))
        .map(entry => entry.name)
        .sort(compareNames);
}

function sha256File(filePath) {
    return crypto.createHash('sha256').update(fs.readFileSync(filePath)).digest('hex');
}

function sha256Content(content) {
    return crypto.createHash('sha256').update(content).digest('hex');
}

function createMigrationFiles(directory = migrationsDirectory) {
    return listFilesystemMigrations(directory).map(script => ({
        script,
        sha256: sha256File(path.join(directory, script)),
    }));
}

function runGit(argumentsList, encoding = 'utf8') {
    const result = childProcess.spawnSync('git', argumentsList, {
        cwd: repositoryRoot,
        encoding,
        maxBuffer: 10 * 1024 * 1024,
    });
    if (result.error) {
        throw new Error(`Could not run git: ${result.error.message}`);
    }
    if (result.status !== 0) {
        throw new Error(result.stderr.toString().trim() || `git exited with status ${result.status}`);
    }
    return result.stdout;
}

function readStagedFile(relativePath) {
    return runGit(['show', `:${relativePath}`], null);
}

function createStagedMigrationFiles() {
    const migrationPaths = runGit([
        'ls-files',
        '--cached',
        '--',
        'src/main/resources/db/migrations/V*.sql',
    ]).trim().split('\n').filter(Boolean).sort(compareNames);
    return migrationPaths.map(migrationPath => ({
        script: path.posix.basename(migrationPath),
        sha256: sha256Content(readStagedFile(migrationPath)),
    }));
}

function createMigrationManifest(migrations, schema, generatorSha256) {
    return {
        formatVersion: 1,
        schema,
        generatorSha256,
        migrations: [...migrations]
            .map(migration => ({ script: migration.script, sha256: migration.sha256 }))
            .sort((left, right) => compareNames(left.script, right.script)),
    };
}

function migrationManifestComments(manifest) {
    const header = {
        formatVersion: manifest.formatVersion,
        schema: manifest.schema,
        generatorSha256: manifest.generatorSha256,
    };
    return [
        `%% database-schema-manifest: ${JSON.stringify(header)}`,
        ...manifest.migrations.map(migration =>
            `%% database-schema-migration: ${JSON.stringify(migration)}`),
    ];
}

function hasMatchingMigrationManifest(content, expectedManifest) {
    const manifestPrefix = '%% database-schema-manifest: ';
    const migrationPrefix = '%% database-schema-migration: ';
    const lines = content.split('\n');

    try {
        const headerLines = lines.filter(line => line.startsWith(manifestPrefix));
        if (headerLines.length !== 1) {
            return false;
        }
        const header = JSON.parse(headerLines[0].slice(manifestPrefix.length));
        const migrations = lines
            .filter(line => line.startsWith(migrationPrefix))
            .map(line => JSON.parse(line.slice(migrationPrefix.length)));
        const actualManifest = createMigrationManifest(
            migrations,
            header.schema,
            header.generatorSha256,
        );
        actualManifest.formatVersion = header.formatVersion;
        return JSON.stringify(actualManifest) === JSON.stringify(expectedManifest);
    } catch {
        return false;
    }
}

function assertMatchingMigrationManifest(content, expectedManifest) {
    if (!hasMatchingMigrationManifest(content, expectedManifest)) {
        throw new Error(
            'The database schema diagram migration manifest is out of date. Commit blocked.\n\n'
            + 'Start the local application, regenerate the diagram, and stage it:\n'
            + '  node scripts/generate_database_schema.js\n'
            + '  git add docs/database-schema.mmd',
        );
    }
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
        details.push(`Database migrations not present in the filesystem:\n${unexpectedMigrations.map(name => `  ${name}`).join('\n')}`);
    }
    throw new Error(
        'The running database does not match the filesystem migrations. The Mermaid file was not changed.\n\n'
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

function compareNames(left, right) {
    if (left < right) return -1;
    if (left > right) return 1;
    return 0;
}

function normalizeType(formattedType) {
    const typeDetails = [];
    let type = formattedType.toLowerCase();

    type = type.replace(/^character varying(?:\((\d+)\))?$/, (_, maximumLength) => {
        if (maximumLength) {
            typeDetails.push(`max ${maximumLength}`);
        }
        return 'varchar';
    });
    type = type.replace(/^numeric\((\d+),(\d+)\)$/, (_, precision, scale) => {
        typeDetails.push(`precision ${precision} scale ${scale}`);
        return 'decimal';
    });
    type = type
        .replace('timestamp with time zone', 'timestamptz')
        .replace('timestamp without time zone', 'timestamp')
        .replace('double precision', 'double_precision')
        .replace('[]', '_array')
        .replaceAll(' ', '_');

    return { type: type.replace(/[^a-z0-9_-]/g, '_'), details: typeDetails };
}

function normalizeDefault(defaultValue) {
    if (defaultValue === null) {
        return null;
    }
    return defaultValue.replace(/\s+/g, ' ').trim();
}

function escapeDescription(value) {
    return value.replaceAll('\\', '\\\\').replaceAll('"', '\\"');
}

function constraintColumns(constraints, tableName, type) {
    return constraints
        .filter(constraint => constraint.table === tableName && constraint.type === type)
        .flatMap(constraint => constraint.columns);
}

function hasExactUniqueConstraint(constraints, tableName, columns) {
    const sortedColumns = [...columns].sort(compareNames);
    return constraints.some(constraint => {
        if (constraint.table !== tableName || !['p', 'u'].includes(constraint.type)) {
            return false;
        }
        return [...constraint.columns].sort(compareNames).join('\0') === sortedColumns.join('\0');
    });
}

function validateIdentifier(identifier, description) {
    if (!/^[A-Za-z_][A-Za-z0-9_-]*$/.test(identifier)) {
        throw new Error(`Cannot render ${description} "${identifier}" as a Mermaid identifier`);
    }
}

function renderMermaid(metadata, schema, migrationManifest) {
    const tables = [...metadata.tables]
        .map(table => ({
            ...table,
            columns: [...table.columns].sort((left, right) => left.ordinal - right.ordinal || compareNames(left.name, right.name)),
        }))
        .sort((left, right) => compareNames(left.name, right.name));
    const constraints = [...metadata.constraints].sort((left, right) =>
        compareNames(`${left.table}\0${left.name}`, `${right.table}\0${right.name}`));
    const tableByName = new Map(tables.map(table => [table.name, table]));
    const lines = [
        '%% Generated by scripts/generate_database_schema.js. Do not edit manually.',
        `%% PostgreSQL schema: ${schema}`,
        '%% Views, indexes, functions, triggers, procedures, and extensions are omitted.',
        ...migrationManifestComments(migrationManifest),
        'erDiagram',
    ];

    for (const table of tables) {
        validateIdentifier(table.name, 'table');
        const primaryKeyColumns = new Set(constraintColumns(constraints, table.name, 'p'));
        const foreignKeyColumns = new Set(constraintColumns(constraints, table.name, 'f'));
        const uniqueConstraints = constraints.filter(constraint => constraint.table === table.name && constraint.type === 'u');
        const uniqueColumns = new Set(uniqueConstraints.flatMap(constraint => constraint.columns));

        lines.push(`    ${table.name} {`);
        for (const column of table.columns) {
            validateIdentifier(column.name, 'column');
            const normalizedType = normalizeType(column.type);
            const keys = [];
            if (primaryKeyColumns.has(column.name)) keys.push('PK');
            if (foreignKeyColumns.has(column.name)) keys.push('FK');
            if (uniqueColumns.has(column.name)) keys.push('UK');

            const description = [column.nullable ? 'NULL' : 'NOT NULL'];
            if (column.identity) description.push('identity');
            if (column.generated) description.push('generated');
            description.push(...normalizedType.details);
            const defaultValue = normalizeDefault(column.default);
            if (defaultValue !== null && !column.generated) description.push(`default ${defaultValue}`);
            if (uniqueConstraints.some(constraint => constraint.columns.length > 1 && constraint.columns.includes(column.name))) {
                description.push('composite unique');
            }

            const keySuffix = keys.length > 0 ? ` ${keys.join(',')}` : '';
            lines.push(`        ${normalizedType.type} ${column.name}${keySuffix} "${escapeDescription(description.join(', '))}"`);
        }
        lines.push('    }', '');
    }

    const relationships = constraints
        .filter(constraint => constraint.type === 'f')
        .map(constraint => {
            const sourceTable = tableByName.get(constraint.table);
            const foreignKeyColumns = constraint.columns.map(columnName =>
                sourceTable.columns.find(column => column.name === columnName));
            if (foreignKeyColumns.some(column => !column)) {
                throw new Error(`Foreign key ${constraint.name} refers to an unknown column`);
            }
            const sourceIsOptional = foreignKeyColumns.some(column => column.nullable);
            const sourceIsUnique = hasExactUniqueConstraint(constraints, constraint.table, constraint.columns);
            return {
                label: constraint.columns.join('_'),
                source: constraint.table,
                target: constraint.referencedTable,
                targetCardinality: sourceIsOptional ? 'o|' : '||',
                sourceCardinality: sourceIsUnique ? 'o|' : 'o{',
            };
        })
        .sort((left, right) => compareNames(
            `${left.target}\0${left.source}\0${left.label}`,
            `${right.target}\0${right.source}\0${right.label}`,
        ));

    for (const relationship of relationships) {
        validateIdentifier(relationship.target, 'referenced table');
        lines.push(
            `    ${relationship.target} ${relationship.targetCardinality}--${relationship.sourceCardinality} `
            + `${relationship.source} : ${relationship.label}`,
        );
    }

    return `${lines.join('\n')}\n`;
}

function writeOutputFile(outputPath, content) {
    fs.mkdirSync(path.dirname(outputPath), { recursive: true });
    fs.writeFileSync(outputPath, content, 'utf8');
}

function printHelp() {
    console.log(`Usage: node scripts/generate_database_schema.js [options]

Generates a deterministic Mermaid ER diagram from the local PostgreSQL database.

Options:
  --output <path>         Output path relative to the repository root
                          (default: docs/database-schema.mmd)
  --schema <name>         PostgreSQL schema to inspect (default: public)
  --compose-file <path>   Docker Compose file (default: docker-compose.local.yml)
  --service <name>        PostgreSQL Compose service (default: postgres)
  --database <name>       Database name (default: prsdblocal)
  --user <name>           Database user (default: postgres)
  --host-psql             Use psql from PATH instead of Docker Compose
    --check-manifest        Check migration hashes without contacting PostgreSQL
    --staged                With --check-manifest, check files staged for commit
  -h, --help              Show this help
`);
}

function main() {
    const options = parseArguments(process.argv.slice(2));
    if (options.help) {
        printHelp();
        return;
    }

    const outputPath = path.resolve(repositoryRoot, options.output);
    const outputRelativePath = path.relative(repositoryRoot, outputPath).replaceAll(path.sep, '/');
    const generatorRelativePath = path.relative(repositoryRoot, __filename).replaceAll(path.sep, '/');
    const migrationFiles = options.useStagedFiles ? createStagedMigrationFiles() : createMigrationFiles();
    const generatorSha256 = options.useStagedFiles
        ? sha256Content(readStagedFile(generatorRelativePath))
        : sha256File(__filename);
    const migrationManifest = createMigrationManifest(
        migrationFiles,
        options.schema,
        generatorSha256,
    );
    if (options.checkManifest) {
        const outputContent = options.useStagedFiles
            ? readStagedFile(outputRelativePath).toString('utf8')
            : fs.readFileSync(outputPath, 'utf8');
        assertMatchingMigrationManifest(outputContent, migrationManifest);
        console.log(`${outputRelativePath} migration manifest is current`);
        return;
    }
    if (fs.existsSync(outputPath) && hasMatchingMigrationManifest(fs.readFileSync(outputPath, 'utf8'), migrationManifest)) {
        console.log(`No change detected in migration manifest. ${path.relative(repositoryRoot, outputPath)} is current, and has not been modified.`);
        return;
    }

    const flywayHistoryExists = runPsql(options, buildFlywayHistoryExistsQuery(options.schema));
    const databaseMigrations = flywayHistoryExists
        ? runPsql(options, buildFlywayHistoryQuery(options.schema))
        : [];
    assertMigrationsAreCurrent(migrationFiles.map(migration => migration.script), databaseMigrations);
    const metadata = runPsql(options, buildMetadataQuery(options.schema));
    writeOutputFile(outputPath, renderMermaid(metadata, options.schema, migrationManifest));
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
    assertMatchingMigrationManifest,
    assertMigrationsAreCurrent,
    buildFlywayHistoryExistsQuery,
    buildFlywayHistoryQuery,
    buildMetadataQuery,
    createMigrationFiles,
    createMigrationManifest,
    createStagedMigrationFiles,
    formatPsqlError,
    hasMatchingMigrationManifest,
    listFilesystemMigrations,
    parseArguments,
    renderMermaid,
    writeOutputFile,
};