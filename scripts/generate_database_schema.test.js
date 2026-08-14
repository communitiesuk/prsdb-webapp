const assert = require('node:assert/strict');
const test = require('node:test');

const {
    assertMigrationsAreCurrent,
    formatPsqlError,
    parseArguments,
    parseGitMigrationPaths,
} = require('./generate_database_schema');
const { insertHook } = require('./install_database_schema_hook');

test('parseArguments defaults include tbls options', () => {
    const options = parseArguments([]);

    assert.equal(options.tblsConfig, '.tbls.yml');
    assert.equal(options.tblsProfile, 'tools');
    assert.equal(options.tblsService, 'tbls');
});

test('parseArguments accepts tbls option overrides', () => {
    const options = parseArguments([
        '--tbls-config',
        'config/custom.tbls.yml',
        '--tbls-profile',
        'ci-tools',
        '--tbls-service',
        'diagrammer',
    ]);

    assert.equal(options.tblsConfig, 'config/custom.tbls.yml');
    assert.equal(options.tblsProfile, 'ci-tools');
    assert.equal(options.tblsService, 'diagrammer');
});

test('database schema hook is inserted before existing hooks and only once', () => {
    const existingHook = '#!/bin/sh\n######## KTLINT-GRADLE HOOK START ########\nexit 0\n';
    const installedHook = insertHook(existingHook);

    assert.ok(installedHook.indexOf('DATABASE-SCHEMA-HOOK START') < installedHook.indexOf('KTLINT-GRADLE HOOK START'));
    assert.match(installedHook, /git diff --cached --quiet --diff-filter=ACMRD -- src\/main\/resources\/db\/migrations/);
    assert.match(installedHook, /node scripts\/generate_database_schema\.js --staged-migrations/);
    assert.equal(insertHook(installedHook), installedHook);
});

test('database schema hook replaces an older marked version', () => {
    const existingHook = '#!/bin/sh\n######## DATABASE-SCHEMA-HOOK START ########\nold command\n'
        + '####### DATABASE-SCHEMA-HOOK END #######\n';
    const installedHook = insertHook(existingHook);

    assert.doesNotMatch(installedHook, /old command/);
    assert.match(installedHook, /git diff --cached --quiet/);
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

test('staged migration mode reads migration names from Git index paths', () => {
    assert.equal(parseArguments(['--staged-migrations']).useStagedMigrations, true);
    assert.deepEqual(
        parseGitMigrationPaths(
            'src/main/resources/db/migrations/V1_2_0__second.sql\0'
            + 'src/main/resources/db/migrations/V1_1_0__first.sql\0'
            + 'src/main/resources/db/migrations/notes.txt\0'
            + 'other/V1_0_0__ignored.sql\0',
        ),
        ['V1_1_0__first.sql', 'V1_2_0__second.sql'],
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
            'The running database does not match the expected migrations. The Mermaid file was not changed.\n\n'
            + 'Missing or unsuccessful database migrations:\n'
            + '  V1_0_0__initial.sql\n'
            + '  V1_1_0__add_users.sql\n\n'
            + 'Database migrations not present in the expected migration set:\n'
            + '  V0_9_0__removed.sql\n\n'
            + 'Update the schema by running the IntelliJ "local" configuration, or:\n'
            + '  ./gradlew flywayMigrate',
        ),
    );
});