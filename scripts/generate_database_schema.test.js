const assert = require('node:assert/strict');
const test = require('node:test');

const {
    assertMigrationsAreCurrent,
    assertStagedMigrationsHaveNoUnstagedChanges,
    formatPsqlError,
    parseArguments,
    parseGitMigrationPaths,
} = require('./generate_database_schema');

test('parseArguments defaults to comparing filesystem migrations', () => {
    const options = parseArguments([]);

    assert.equal(options.help, false);
    assert.equal(options.useStagedMigrations, false);
});

test('parseArguments rejects unknown arguments', () => {
    assert.throws(() => parseArguments(['--output', 'elsewhere.mmd']), /Unknown argument: --output/);
});

test('formatPsqlError includes database and schema update guidance', () => {
    assert.equal(
        formatPsqlError('service "postgres" is not running', 1),
        'service "postgres" is not running\n\n'
        + 'Start the local database with:\n'
        + '  docker compose --file docker-compose.local.yml up --detach postgres\n\n'
        + 'Update the schema by running the IntelliJ "local" configuration, or:\n'
        + '  ./gradlew flywayMigrate',
    );
});

test('formatPsqlError falls back to the exit status when stderr is empty', () => {
    assert.match(formatPsqlError('   ', 137), /^docker exited with status 137\n/);
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

test('assertStagedMigrationsHaveNoUnstagedChanges ignores unstaged changes to migrations outside the commit', () => {
    assert.doesNotThrow(() => assertStagedMigrationsHaveNoUnstagedChanges(
        ['V1_0_0__initial.sql'],
        ['V1_1_0__not_in_this_commit.sql'],
    ));
});

test('assertStagedMigrationsHaveNoUnstagedChanges rejects partially staged migrations', () => {
    assert.throws(
        () => assertStagedMigrationsHaveNoUnstagedChanges(
            ['V1_0_0__initial.sql', 'V1_1_0__add_users.sql'],
            ['V1_1_0__add_users.sql'],
        ),
        /Partially staged migrations:\n {2}V1_1_0__add_users\.sql/,
    );
});