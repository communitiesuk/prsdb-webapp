# Private Rental Sector Database - Web App

This is the web app code for the Private Rental Sector Database (PRSD).

## Development

The prsd-webapp is a Spring Boot application written in Kotlin. The following should allow you to get started developing
functionality for prsd-webapp.

### Dependencies

For the easiest local development experience, use Intellij, and have a docker daemon running on your machine. The repo
includes a `local` launch configuration which will use docker compose to setup the required local dependencies before
starting the application.

A running docker daemon is also required to run the integration tests, which make use
of [testcontainers](https://testcontainers.com/).

The application requires Java 17 - Gradle will automatically install this for you the first time you run the
application locally.

We are using Ktlint for linting, via the [ktlint-gradle plugin](https://github.com/jlleitschuh/ktlint-gradle) and the
[ktlint Intellij plugin](https://plugins.jetbrains.com/plugin/15057-ktlint) which can be installed from within Intellij.

To ensure that your code meets the linting and formatting rules (as well as checking that the tests pass), we recommend
installing the pre-commit hook by running the `addKtlintCheckGitPreCommitHook` task from Gradle tab in Intellij.

### Testing

The project uses a combination of unit tests and integration tests. The integration tests use a testcontainer to run a
postgres database. This is relatively time-consuming to spin up, and so should only be used for integration tests that
need to interact with a real database - for other tests the relevant repository beans should be mocked instead.

### Code structure

#### Backend

Controllers can be found in the `controllers` package, entities and repositories can be found in the `database`
package.

When developing locally, third party APIs should be stubbed by pointing the requests back at http://localhost:8080, and
adding an equivalent endpoint to the one you're calling to the `local.api` package. Those controllers should be
annotated by `@Profile("local")` to ensure that they are not included in any non-local builds.

#### Frontend

The project uses the Thymeleaf templating engine, combined with the Gov.UK design system. The top-level templates can be
found in `src/main/resources/templates`, and reusable fragments can be found in the `fragments` subfolder.