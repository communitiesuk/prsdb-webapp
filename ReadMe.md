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

You can run the unit tests by running the `verification\test` task from the Gradle tab in Intellij.

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

The project uses the Thymeleaf templating engine, combined with
the [Gov.UK design system](https://design-system.service.gov.uk/). The top-level templates can be found in
`src/main/resources/templates`, while fragments are stored in the `fragments` subfolder. When a new reusable component
is required, use relevant html from the design system to create a fragment.

Static assets should be added to the `src/main/resources/assets` folder. These will be copied into
the `src/main/resources/static/assets` folder at build time. Assets should not be added to the `static/assets` folder
directly as this is excluded from source control.

Custom css can now be added using [sass](https://sass-lang.com/) which is compiled to css by rollup when the project is
run.
New styles can be added to new or existing files in `src/main/resources/css` - if you make a new file, make sure it is
added
to `custom.scss` (this is what will get compiled). This lets directly use the govuk colours / spacing mixins.
So far we just included minimal govuk scss as this is all we need - see
[here](https://frontend.design-system.service.gov.uk/import-css/#import-specific-parts-using-sass) for adding more if
required.

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

### Updating Local Authority Data

The project uses migrations to populate the `local_authority` table with data from
`src/main/resources/data/local_authorities/local_authorities.csv`. If the CSV file is updated, create a
copy of
it and call it `local_authorities_V<version number>.csv`, where `version number` is one more than the latest version in
`src/main/resources/db/migrations/data/local_authorities`. Then run the utility script to generate the sql for a new
migration by:

- `cd`ing into the `/scripts` folder
- running `node run generate_la_migration.js`
- using the output in `/scripts/output/draft_upsert_local_authorities.sql` to create your new migration

### Mock One Login Oauth2

For development, we've mocked elements of the governments one login system (that the web app will be using in
deployment).
When you start the app using the `local` run configuration, this will be available, when you attempt to login. It will
automatically log you in as a user that has every role - and therefore can access all pages.

If you are adding new roles please add the user with the `userId` set in `MockOneLoginHelper` to that new role/table.

If you need to be able to login as a user that has specific roles then you can change the `userId` in
`MockOneLoginHelper` to the id from the `one_login_user` table of a user that has the permissions you want.

#### Disabling the mock One Login Oauth2

If you need to disable the mock to run the app with One login's integration system, edit the run configuration so that
it uses the `local-auth` profile instead of the `local-no-auth` profile.

### One Login accounts

When you run the app with the one login mock disabled and try to view pages, you will be prompted to sign in or create a
One Login account.

To view most pages, your account will need to have been added to the relevant database (e.g. LandlordUser,
LocalAuthorityUser) for you to be able to see the page. It checks the database on login (you can step through
`getRolesforSubjectId` in `UserRolesService` to test it), so you will need to log in again to see the change in
permissions (if logging out is not yet implemented, try running in an incognito tab so you are prompted to log in
again).

For local dev, you can add your account by modifying the `data-local.sql` file. Insert an entry into the
`one_login_user` database with a subject_identifier matching your real one login id (see below).
Then you can add entries to any other user database that you need access to (e.g. landlord, local_authority_user
with is_manager set to true to see local authority admin pages).

#### Finding your One Login id

One way to find your id is to check the `subjectId` in `getRolesForSubjectId` in the `UserRolesService` while you are
logging in.

* Run the app in debug mode, add a break point in `getRolesForSubjectId`
* If you are already logged in it won't hit the breakpoint. Load the app in an incognito tab so that you are prompted
  to log in again.
* When you hit the debug point, your one login id should be available in `subjectId`
  (it should look like `urn:fdc:gov.uk:2022:string-of-characters`)

If anyone knows a better way to do this please add it here!

### Connecting to AWS

When the service runs in AWS it has the profile of the ECS service it is running on.
This allows it to connect to e.g. S3, the database and other AWS services.
To connect to the deployed database while running locally you need to set up a port forwarding session using SSM due to
networking rules.
To connect to S3 you need to provide your local service with a profile with which to connect.
You can do that using `aws-vault`, as follows.
To set up `aws-vault` follow the instructions in the `prsdb-infra` repository.

#### Setting up `aws-vault` as a profile server

Run
```shell
aws-vault exec <profile> --server
```

This starts a session with aws-vault acting as a credential server.
You can add `-- bash` or `-- powershell` to enter the server using your shell of choice.

Then run
```shell
env | grep AWS_CONTAINER
```

This will return two lines giving you the `AWS_CONTAINER_CREDENTIALS_FULL_URI` and the
`AWS_CONTAINER_AUTHORIZATION_TOKEN` for your server.
Copy both of these lines into your `.env` file and add the line
```
AWS_REGION=eu-west-2
```

Then run the service as usual, it will pick up the profile provided by the `aws-vault exec` command.

When you have finished running the service, run `exit` in the server terminal to close the server.

#### Connecting to AWS S3 locally

By default, when the service is run locally, it uses the `LocalFileUploader` instead of the `AwsS3FileUploader`.
You can manually switch by manipulating the profiles and attributes on those classes.
Currently, there isn't a profile which connects to AWS with an otherwise local build.

## Releasing to Test

At least once a sprint we aim to release changes into the Test environment. This process happens automatically when
changes are merged to the `test` branch. Merges into `test` should be made as normal (not squash) merges to ensure a
common git history between `main` and `test`.

The normal process is simply to raise a PR merging `main` into `test`. In most cases this will be all that is required
as all features on integration will have been QA'd, demoed, and be ready for review.

In the rare case that there are changes on `main` that we do not want to release to `test`:

- Identify the last commit on `main` before the code that you do not want to release was added
- Create a new branch off of that commit, e.g. `release/main-to-test-11` for the 11th release to `test`
- Identify any later commits that you _do_ want to release to `test` and cherry-pick them onto the new branch
- Merge the new branch into `test`
- Merge `test` back into `main` **using a normal merge - not a squash commit** - you will need to ask an admin on the
  repo to temporarily allow normal merges into `main` to do this

#### Hotfixes

It should be very rare that a hotfix will need to be made directly to `test` (vs. being made on `main` and then
releasing to `test` in the normal way). However, if this is needed:

- Create a new branch from `test` e.g. `hotfix/prsd-<ticket number>-<description>`
- Make the changes on the hotfix branch
- Merge the hotfix branch into `test`
- Merge `test` back into `main` **using a normal merge - not a squash commit** - you will need to ask an admin on the
  repo to temporarily allow normal merges into `main` to do this
