const assert = require('node:assert/strict');
const test = require('node:test');

const {
    assertMatchingMigrationManifest,
    assertMigrationsAreCurrent,
    createMigrationManifest,
    formatPsqlError,
    hasMatchingMigrationManifest,
    renderMermaid,
} = require('./generate_database_schema');
const { insertHook } = require('./install_database_schema_hook');

const migrationFiles = [
    { script: 'V1_1_0__add_users.sql', sha256: 'b'.repeat(64) },
    { script: 'V1_0_0__initial.sql', sha256: 'a'.repeat(64) },
];
const migrationManifest = createMigrationManifest(migrationFiles, 'public', 'c'.repeat(64));

const metadata = {
    tables: [
        {
            name: 'child',
            columns: [
                { name: 'parent_id', ordinal: 2, type: 'bigint', nullable: false, identity: false, generated: false, default: null },
                { name: 'id', ordinal: 1, type: 'bigint', nullable: false, identity: true, generated: false, default: null },
            ],
        },
        {
            name: 'parent',
            columns: [
                { name: 'id', ordinal: 1, type: 'bigint', nullable: false, identity: true, generated: false, default: null },
            ],
        },
    ],
    constraints: [
        { name: 'child_parent_fk', type: 'f', table: 'child', columns: ['parent_id'], referencedTable: 'parent', referencedColumns: ['id'] },
        { name: 'parent_pkey', type: 'p', table: 'parent', columns: ['id'], referencedTable: null, referencedColumns: [] },
        { name: 'child_pkey', type: 'p', table: 'child', columns: ['id'], referencedTable: null, referencedColumns: [] },
    ],
};

test('renderMermaid produces stable output regardless of metadata ordering', () => {
    const reorderedMetadata = {
        tables: [...metadata.tables].reverse().map(table => ({ ...table, columns: [...table.columns].reverse() })),
        constraints: [...metadata.constraints].reverse(),
    };

    assert.equal(
        renderMermaid(metadata, 'public', migrationManifest),
        renderMermaid(reorderedMetadata, 'public', migrationManifest),
    );
    assert.match(renderMermaid(metadata, 'public', migrationManifest), /parent \|\|--o\{ child : parent_id/);
});

test('migration manifest is stable and embedded as structured Mermaid comments', () => {
    const reorderedManifest = createMigrationManifest([...migrationFiles].reverse(), 'public', 'c'.repeat(64));
    const content = renderMermaid(metadata, 'public', migrationManifest);

    assert.deepEqual(migrationManifest, reorderedManifest);
    assert.match(content, /^%% database-schema-manifest: \{"formatVersion":1,"schema":"public","generatorSha256":"c{64}"\}$/m);
    assert.match(content, /^%% database-schema-migration: \{"script":"V1_0_0__initial.sql","sha256":"a{64}"\}$/m);
    assert.match(content, /^%% database-schema-migration: \{"script":"V1_1_0__add_users.sql","sha256":"b{64}"\}$/m);
    assert.equal(hasMatchingMigrationManifest(content, reorderedManifest), true);
});

test('migration manifest mismatch is detected without querying the database', () => {
    const content = renderMermaid(metadata, 'public', migrationManifest);
    const changedMigrationManifest = createMigrationManifest(
        [{ script: 'V1_0_0__initial.sql', sha256: 'd'.repeat(64) }],
        'public',
        'c'.repeat(64),
    );

    assert.equal(hasMatchingMigrationManifest(content, changedMigrationManifest), false);
    assert.equal(hasMatchingMigrationManifest('erDiagram\n', migrationManifest), false);
    assert.throws(
        () => assertMatchingMigrationManifest('erDiagram\n', migrationManifest),
        /migration manifest is out of date\. Commit blocked/,
    );
});

test('database schema hook is inserted before existing hooks and only once', () => {
    const existingHook = '#!/bin/sh\n######## KTLINT-GRADLE HOOK START ########\nexit 0\n';
    const installedHook = insertHook(existingHook);

    assert.ok(installedHook.indexOf('DATABASE-SCHEMA-HOOK START') < installedHook.indexOf('KTLINT-GRADLE HOOK START'));
    assert.equal(insertHook(installedHook), installedHook);
});

test('formatPsqlError includes database and schema initialization guidance', () => {
    const options = {
        composeFile: 'docker-compose.local.yml',
        service: 'postgres',
        useHostPsql: false,
    };

    assert.equal(
        formatPsqlError(options, 'service "postgres" is not running', 1),
        'service "postgres" is not running\n\n'
        + 'Start the local database with:\n'
        + '  docker compose --file docker-compose.local.yml up --detach postgres\n\n'
        + 'Then initialize its schema by running the IntelliJ "local" configuration, or:\n'
        + '  ./gradlew flywayMigrate',
    );
});

test('assertMigrationsAreCurrent accepts matching filesystem and successful database migrations', () => {
    assert.doesNotThrow(() => assertMigrationsAreCurrent(
        ['V1_0_0__initial.sql', 'V1_1_0__add_users.sql'],
        [
            { script: 'V1_1_0__add_users.sql', success: true },
            { script: 'V1_0_0__initial.sql', success: true },
        ],
    ));
});

test('assertMigrationsAreCurrent reports missing, failed, and unexpected database migrations', () => {
    assert.throws(
        () => assertMigrationsAreCurrent(
            ['V1_0_0__initial.sql', 'V1_1_0__add_users.sql'],
            [
                { script: 'V1_0_0__initial.sql', success: false },
                { script: 'V0_9_0__removed.sql', success: true },
            ],
        ),
        new Error(
            'The running database does not match the filesystem migrations. The Mermaid file was not changed.\n\n'
            + 'Missing or unsuccessful database migrations:\n'
            + '  V1_0_0__initial.sql\n'
            + '  V1_1_0__add_users.sql\n\n'
            + 'Database migrations not present in the filesystem:\n'
            + '  V0_9_0__removed.sql\n\n'
            + 'Update the schema by running the IntelliJ "local" configuration, or:\n'
            + '  ./gradlew flywayMigrate',
        ),
    );
});