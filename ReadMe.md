# Private Rental Sector Database - Web App

This is the web app code for the Private Rental Sector Database (PRSDB).

## Development

The prsdb-webapp is a Spring Boot application written in Kotlin. The following should allow you to get started
developing
functionality for prsdb-webapp.

### Dependencies

For the easiest local development experience, use Intellij, and have a docker daemon running on your machine. The repo
includes a `local` launch configuration in the `.run` folder which will use docker compose to setup the required local
dependencies before starting the application.

A running docker daemon is also required to run the integration tests, which make use
of [testcontainers](https://testcontainers.com/).

The application requires Java 17 - Gradle should automatically install this for you the first time you run the
application locally.

We are using Ktlint for linting, via the [ktlint-gradle plugin](https://github.com/jlleitschuh/ktlint-gradle) and the
[ktlint Intellij plugin](https://plugins.jetbrains.com/plugin/15057-ktlint) which can be installed from within Intellij.

To ensure that your code meets the linting and formatting rules (as well as checking that the tests pass), we recommend
installing the pre-commit hook by running the `addKtlintCheckGitPreCommitHook` task from Gradle tab in Intellij.

There are also some local secrets that will need to be set up if you need to test integrations with other services when
running the project locally. Ask the team lead where these can be found.

When running your build against the integration environment of Gov.UK One Login you will be prompted for credentials to
access the integration environment, ask your team lead where these can be found.

### Testing

The project uses a combination of unit tests and integration tests. The integration tests use a testcontainer to run a
postgres database. This takes extra time to spin up, and spins up a clean container per test, and so should only be used
for integration tests that need to interact with a real database - for other tests the relevant repository beans should
be mocked instead.

### Code structure

#### Backend

Controllers can be found in the `controllers` package, entities and repositories can be found in the `database`
package, configuration classes in the `config` package and so on.

App configuration can be found in the `application.yml` and `application-local.yml` files for deployed and local
configuration respectively. For deployed environments configuration that differs between environments should be managed
through referencing environment variables in `application.yml`.

Database migrations can be found in `src/main/resources/db.migrations`. See section below on database migrations for
more information on these.

When developing locally, third party APIs should be stubbed by pointing the requests back at http://localhost:8080
through your local configuration in `application-local.yml`, and adding an equivalent endpoint to the one you're calling
to the `local.api` package. Those controllers should be annotated by `@Profile("local")` to ensure that they are not
included in any deployed builds.

#### Frontend

The project uses the Thymeleaf templating engine, combined with the Gov.UK design system. The top-level templates can be
found in `src/main/resources/templates`, and reusable fragments can be found in the `fragments` subfolder.

Static assets should be added to the `src/main/resources/assets` folder. These will be copied into
the `src/main/resources/static/assets` folder at build time. Assets should not be added to the `static/assets` folder
directly as this is excluded from source control.

### Database migrations

The project uses Flyway to manage migrations. To add a migration, create a new SQL file
in `src/main/resources/db.migrations` with a name of the form `V<version number>__<migration name>.sql`. The version
number is in semvar format with underscores separating the major/minor/fix elements. This determines the order in which
Flyway runs the migrations.

Database migrations <will be/ are> run at deployment time for all non-local deployments. This is done using
the `flywayMigrate` Gradle task. When developing locally using the `local` profile the migrations will run at
application start up. If you are using the `local` launch profile in IntelliJ, this will also run the `flywayClean` task
before running the migrations. After the migrations have run Spring Boot will then run the SQL in `data-local.sql` to
populate the database with seed data. 