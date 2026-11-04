# Performance Tests

Gatling performance tests for the PRSDB webapp, written using Gatling's Kotlin DSL.

The performance code lives in the root project's `src/gatling` source set (added by the
[`io.gatling.gradle`](https://docs.gatling.io/reference/integrations/build-tools/gradle-plugin/) plugin). It can see
the webapp's `src/main` classes, so the journeys reuse the webapp's route constants, but it still drives the running
application over HTTP. Nothing in `src/gatling` ships in the production jar.

## Layout

```
src/gatling/kotlin/uk/gov/communities/prsdb/webapp/performance/
  BaseUrlConfig.kt                     # Resolves the target base URL
  RegisterAsLandlordGetSimulation.kt   # GET-only example
  FormSubmissionPostSimulation.kt      # GET -> extract CSRF -> POST example
  BasicJourneysSimulation.kt           # Serial one-user populations: phone/email updates -> registration
  *Journey.kt, OneLoginAuthentication.kt, Basic*.kt   # Reusable HTTP chains and run configuration
  *Tests.kt, BasicContractSimulation.kt, BasicHttpTestServer.kt   # Unit and Gatling assertion tests
src/test/resources/one-login-simulator/  # Synthetic One Login identity-verification claims, shared with the
                                         # simulator integration tests and added to the gatling resources
scripts/performance-tests/start-local.mjs  # Local official-simulator application bootstrap
```

Simulations and their supporting code live in `src/gatling/kotlin`.

## Choosing the target environment

`BaseUrlConfig` reads the `gatling.baseUrl` system property and falls back to `http://localhost:8080`, so the
simulations work against a locally running webapp with no extra configuration.

To target a deployed environment, pass the property on the command line:

```bash
./gradlew gatlingRun -Dgatling.baseUrl=https://<environment-host>
```

```powershell
.\gradlew.bat gatlingRun "-Dgatling.baseUrl=https://<environment-host>"
```

If you run the webapp locally on a non-default port (git worktrees are assigned a unique `SERVER_PORT` in their
`.env`), pass that port explicitly:

```bash
./gradlew gatlingRun -Dgatling.baseUrl=http://localhost:<port>
```

```powershell
.\gradlew.bat gatlingRun "-Dgatling.baseUrl=http://localhost:<port>"
```

## Running the simulations

Run every simulation:

```bash
./gradlew gatlingRun --all
```

```powershell
.\gradlew.bat gatlingRun --all
```

`--all` now includes the real basic suite: supply its properties and start the official simulator as described
below. The toy POST example also needs its feature flag enabled.

Run a single simulation by fully qualified class name:

```bash
./gradlew gatlingRun \
  --simulation uk.gov.communities.prsdb.webapp.performance.RegisterAsLandlordGetSimulation
```

```powershell
.\gradlew.bat gatlingRun `
  --simulation uk.gov.communities.prsdb.webapp.performance.RegisterAsLandlordGetSimulation
```

Without `--all` or `--simulation`, the task prompts you to choose a simulation interactively. For CI, always
pass one of those options (and `--non-interactive`) so the build never blocks waiting for input.

Request checks mark incorrect responses as failed requests. To fail the simulation and build when a request
check fails, also register a simulation assertion such as `global().failedRequests().count().is(0L)`
(`is` requires backticks in Kotlin). The toy examples currently demonstrate request checks only.

### Developing the basic suite locally

The basic-suite foundations have independent tests. The run configuration and `HttpDestination` have unit
tests, and a small set of tests execute Gatling against a disposable loopback HTTP server, with one virtual user.
None of them need the webapp, Docker, AWS credentials or NFT:

```bash
./gradlew performanceTest --console=plain
```

```powershell
.\gradlew.bat performanceTest --console=plain
```

Build and Test runs these in the separate "Performance tests" job. Like any build of the webapp, they need Node
and `npm ci` because compiling the root project builds the frontend assets.

The Gatling tests prove that incorrect bodies/content types, missing CSRF tokens and skipped requests fail the
runner; warm-up requests are excluded from measured request counts; repeated samples are aggregated; and mean
and maximum timing gates reject the corresponding deliberately delayed responses. They do **not** prove that the
journey chains still work against the application, and they do not measure application performance: only running
the simulation against the real app (below) does that.

`BasicRunConfig` separates the target from the measurement mode. Both targets use the same HTTP journey logic;
the existing toy simulations continue to use `BaseUrlConfig` unchanged.

| Property | Purpose |
|----------|---------|
| `gatling.target` | Explicit `local` or `nft` target |
| `gatling.baseUrl` | Application origin, including the local/worktree port |
| `gatling.simulatorUrl` | Official One Login simulator origin |
| `gatling.basic.landlordSubject` | Seeded synthetic landlord for the phone and email update slices |
| `gatling.basic.registrationSubject` | Fresh run-specific prefix for generated synthetic registration subjects; must differ from `landlordSubject` |
| `gatling.basic.iterations` | Number of measured journey passes; positive integer, default `10` |
| `gatling.basic.maxIterations` | Safety ceiling for measured passes; positive integer, default `100`; raise explicitly to run more |
| `gatling.basic.mode` | `baseline` records timings; `gated` applies explicit timing limits |
| `gatling.local.basic.meanResponseTimeMs` | Local gated per-request mean limit, positive integer milliseconds |
| `gatling.local.basic.maxResponseTimeMs` | Local gated per-request limit, positive integer milliseconds |
| `gatling.nft.basic.meanResponseTimeMs` | Separately agreed NFT gated per-request mean limit |
| `gatling.nft.basic.maxResponseTimeMs` | Separately agreed NFT gated per-request limit |

URLs must be HTTP/HTTPS origins, without credentials, paths, query strings or fragments. Baseline mode rejects
limits for its target rather than ignoring them. Gated mode requires both target-specific mean and maximum limits;
NFT never inherits local timing limits. The only enforced relationship is `maxResponseTimeMs >= meanResponseTimeMs`.
For example, an initial maximum budget might be three times the mean budget, but that ratio is guidance only and
is not validated.

The basic profile performs one functional warm-up pass followed by the configured number of measured passes,
with one active virtual user and no concurrent load. It always requires a 0% failed-request rate; warm-up requests
are functionally checked too. Each measured application request must execute exactly `gatling.basic.iterations`
times. Warm-up uses separate request names and is excluded from measured request counts and timings. Gated runs
apply both mean and maximum limits per named request. Baseline runs record timings without applying those limits.
This follows ADR-0035: basic tests exercise individual actions independently of wider concurrent load, but still
repeat requests to measure averages and maxima.

Local limits prove the gates work; they are not NFT acceptance limits. Future load, spike, or stress profiles can
reuse journey chains while defining their own concurrent workload.

Build and Test runs the unit and assertion tests (`performanceTest`) in their own job, but never the simulations.
The webapp test shards run only the root `test` task. Automated performance execution belongs in the dedicated PDJB-430 performance-testing action; the
commands here remain available for explicit local development runs.

### Real local basic journey

Requires Docker, Node 22+, JDK 21 and the usual gitignored `.env` configuration. Run commands from the
repository root. Commands are shown for macOS/Linux (bash) and Windows (PowerShell). This uses the standard
local database and Redis, **not** a deployed environment.

Start the usual dependencies:

```bash
docker compose -f docker-compose.local.yml up -d --wait
```

```powershell
docker compose -f docker-compose.local.yml up -d --wait
```

Start the pinned official One Login simulator in a separate terminal:

```bash
docker run --rm --name prsdb-performance-simulator \
  -p 127.0.0.1:13000:3000 -e INTERACTIVE_MODE=true \
  ghcr.io/govuk-one-login/simulator@sha256:0d5e62c1db1c400c4881be2270b3f08aeb55c72ca3d9eb9a6e5196becef6f5e5
```

```powershell
docker run --rm --name prsdb-performance-simulator `
  -p 127.0.0.1:13000:3000 -e INTERACTIVE_MODE=true `
  ghcr.io/govuk-one-login/simulator@sha256:0d5e62c1db1c400c4881be2270b3f08aeb55c72ca3d9eb9a6e5196becef6f5e5
```

**Destructive local reset:** stop the application first, and only run the following against a disposable local
database. It deletes the local schema; application startup then migrates and loads `data-local.sql` again.
The Gradle Flyway task reads the database port from `.env`.

```bash
FLYWAY_CLEAN_DISABLED=false ./gradlew flywayClean --console=plain
node --env-file=.env scripts/performance-tests/start-local.mjs
```

```powershell
$env:FLYWAY_CLEAN_DISABLED = "false"; .\gradlew.bat flywayClean --console=plain; Remove-Item Env:FLYWAY_CLEAN_DISABLED
node --env-file=.env scripts/performance-tests/start-local.mjs
```

Wait for the application startup message. The bootstrap creates disposable signing keys, configures the local
simulator and overrides the One Login endpoints. Although it reuses the `local,local-no-auth` profiles for local
services, the landlord signs in through the **official simulator**, not `/local/one-login`. Keys are removed
when the bootstrap exits. It does not reset the database automatically. Stop it and the simulator with Ctrl-C
when finished.

The default application port is 8080; `SERVER_PORT`, `POSTGRES_PORT` and `REDIS_PORT` follow `.env`.
`ONE_LOGIN_SIMULATOR_PORT` overrides 13000; change the Docker port mapping to match.
The simulator configuration API requires its IP origin (`127.0.0.1`), not `localhost`.
Council-provider discovery is kept local for startup, but council login is **not** implemented by this bootstrap.

In another terminal, run:

```bash
./gradlew gatlingRun --non-interactive \
  --simulation uk.gov.communities.prsdb.webapp.performance.BasicJourneysSimulation \
  -Dgatling.target=local -Dgatling.basic.mode=baseline \
  -Dgatling.baseUrl=http://localhost:8080 \
  -Dgatling.simulatorUrl=http://127.0.0.1:13000 \
  -Dgatling.basic.landlordSubject=urn:fdc:gov.uk:2022:UVWXY \
  -Dgatling.basic.registrationSubject=urn:fdc:gov.uk:2026:PERF-REGISTRATION-001
```

```powershell
.\gradlew.bat gatlingRun --non-interactive `
  --simulation uk.gov.communities.prsdb.webapp.performance.BasicJourneysSimulation `
  "-Dgatling.target=local" "-Dgatling.basic.mode=baseline" `
  "-Dgatling.baseUrl=http://localhost:8080" `
  "-Dgatling.simulatorUrl=http://127.0.0.1:13000" `
  "-Dgatling.basic.landlordSubject=urn:fdc:gov.uk:2022:UVWXY" `
  "-Dgatling.basic.registrationSubject=urn:fdc:gov.uk:2026:PERF-REGISTRATION-001"
```

Match the URLs to the ports used by the bootstrap. The Gradle build forwards `gatling.*` properties to the
simulation JVM and excludes test-only simulations from production runs. The registration subject property is a
run-specific prefix: the simulation appends a warm-up or iteration suffix to generate a new synthetic subject for
each registration. Each run creates `gatling.basic.iterations + 1` registrations and leaves them in the target.
Use a fresh prefix for every run, or restore/reseed the database before reusing one.

The basic suite signs in as seeded landlord Alexander Smith for each profile-update pass. Phone and email values
change on every warm-up and measured pass, so each profile submission updates the saved value rather than
resubmitting an unchanged value. Phone values use the synthetic London range beginning `02079460123`; email
addresses use `example.invalid`. Each pass checks the saved profile value. Run with the local profile and do not
enable `use-notify`; local email notifications are handled by the stub service. In NFT the `one-login-simulator` profile (active only during performance runs) also selects the stub service, so
no emails are sent to Notify and Notify's allow-list cannot fail the journeys.

NFT is behind CloudFront, whose cache policy has a 1 second minimum TTL that overrides `no-store`. A repeat GET
of the same URL by the same session within a second can return a stale page, so the saved-details reads append a
unique `perfCacheBuster` query parameter to bypass the cache.

Gatling's console output goes to a public GitHub Actions log, so `src/gatling/resources/logback-test.xml` limits it to
warnings and errors. Without it logback defaults to DEBUG, which prints every request and response, including session
cookies and OAuth codes. Diagnose failures from the uploaded Gatling report, or re-run locally with debug logging.

To enable debug logging locally, temporarily change `<root level="WARN">` to `<root level="DEBUG">` in
`src/gatling/resources/logback-test.xml`, then rerun your usual local `gatlingRun` command. Gradle copies the updated
resource into the Gatling runtime classpath. Change the level back to `WARN` afterwards and check the diff before
committing or pushing. Never enable debug logging in the public GitHub Actions workflow, and do not upload or share
unredacted debug output: it can contain session cookies and OAuth codes.

Each registration pass uses a separate sequential virtual user and a unique synthetic One Login subject, email,
and phone value. It performs identity verification with synthetic claims, accepts the privacy notice, and completes
the individual manual-address registration. Registration clears the app session to refresh the user's roles, so
each pass reauthenticates before checking the confirmation registration number and following the dashboard link.
Profile-update passes run before registration passes; all passes run serially, with one active virtual user at a
time and no app or simulator cookies shared between identities. Each redirect is a separate named request, with no
embedded-resource fetching or concurrent-load injection.

Reset/reseed between comparable baselines so the seeded phone/email values and registration subject return to their
initial state. Registration fixtures are synthetic and do not contain real identity data. For a local timing-gate
experiment, change the mode to `gated` and supply both `gatling.local.basic.meanResponseTimeMs` and
`gatling.local.basic.maxResponseTimeMs`. These are explicit local limits, not an agreed NFT SLA. Simulator
front-channel requests are checked for correctness but excluded from application timing gates; the application
callback includes its back-channel authentication work.

The basic slice covers seeded phone and email updates and individual registration through the manual-address
branch. It does not cover other registration branches, council or letting-agent journeys. See ADRs 0035-0040
for the basic/load distinction, API-level measurement, runner, scheduling, Gatling and fixture-restoration decisions.

### Generating the large NFT dataset (PDJB-1748)

`NftDataSeeder` adds records alongside the existing small fixture. Explicit IDs start above each table's existing
maximum, and sequences are aligned without rewinding an already-ahead sequence. Generation requires exclusive
database access: stop application and scheduled writers before running it. This is not an idempotent startup
script; start each generation from a freshly reset fixture, rather than rerunning the same random seed over a
previously generated dataset.

Bulk property addresses must be active, have a UPRN and belong to an English local council. The five reserved
`EG1 2AA` lookup addresses remain outside bulk property allocation so the existing registration journey keeps
working. `NFT_SEED_MINIMUM_FREE_ADDRESSES` (default `0`) reserves additional address capacity for subsequent
registration journeys; choose it from the planned test volume rather than assuming the total address count
represents usable capacity. Generation fails and rolls back if there are not enough addresses or if the
configured landlords produce fewer property registrations than requested.

The property settings are **additional** records, excluding the small fixture. Completed and incomplete registrations
have separate exact targets:

| Environment variable | Deployed default | Meaning |
| --- | ---: | --- |
| `NFT_SEED_SYSTEM_OPERATORS` | 150 | Additional system operators |
| `NFT_SEED_LOCAL_COUNCIL_USERS` | 3,000 | Additional local-council users |
| `NFT_SEED_PROPERTIES` | 2,450,000 | Completed property registrations |
| `NFT_SEED_INCOMPLETE_PROPERTIES` | 272,223 | Incomplete property-registration journeys |
| `NFT_SEED_LANDLORDS` | 0 | Derive landlord count from the existing synthetic portfolio distribution |
| `NFT_SEED_BATCH_SIZE` | 10,000 | Records processed per seeding batch |
| `NFT_SEED_RANDOM_SEED` | 239 | Random seed for reproducible generated data |
| `NFT_SEED_GENERATED_ADDRESSES` | 0 | Generate no extra addresses; use the existing address dataset |
| `NFT_SEED_MINIMUM_FREE_ADDRESSES` | 0 | Extra eligible addresses to leave unallocated |

A positive landlord count requests that many additional landlords instead; generation fails if their portfolios
cannot supply both property targets. Automatic mode includes landlords with no properties according to the existing
distribution and stops after the batch that meets both targets. It is a synthetic population, not a landlord demand
forecast. The final portfolio can be truncated to reach the exact property targets. Local defaults remain small:
30 landlords, 40 completed properties and 5 incomplete journeys.

#### Workload assumptions and arithmetic

The dump loader downloads a pinned S3 object version. The NFT `nft-rds-access` role therefore needs
`s3:GetObjectVersion` on `arn:aws:s3:::prsdb-seed-data-nft/seed-data.dump`, as well as its existing object-read
and KMS-decrypt permissions. **Infrastructure PR required:** codify this version-read permission in
`communitiesuk/prsdb-infra`; a manual live-test permission change is not a substitute for Terraform configuration.

The performance workflow resets the small fixture using migrations from its own commit, matching the deployed
application. Other reset callers retain their environment-branch default. Startup checks outside the runner's WAF
allow-list window use ECS rollout health; HTTP readiness checks remain inside the existing preparation window.

The loader uses `pg_restore --jobs=4` against the custom-format dump. Parallel workers can load independent tables
and build indexes concurrently, but do not split a single table's `COPY` across workers or remove a shared SSM
tunnel throughput limit. This is an experiment, not a measured fourfold speedup. The database instance and storage
configuration stay unchanged so timings can be compared.

Logs include UTC start/completion/failure timestamps and elapsed seconds for download, archive inspection,
restore, Flyway migrations and dataset verification (including `ANALYZE`). Each active phase emits an elapsed-time
heartbeat every minute; this is not a percentage-complete estimate. Raw PostgreSQL output and credentials are
not streamed to the Actions log.

An interrupted restore can leave the preserved address/council tables without their post-data keys and indexes.
The normal small-fixture reset preserves these tables, so it may require explicit repair of their constraints
and names from the verified dump schema before it can migrate successfully. Keep the app stopped until recovery
has completed; do not assume that cancelling the restore alone returns NFT to a usable state.

The fee-model forecast is based on **properties, not landlords**: 4.9 million eligible properties, with uptake of
50% in year one, 55% in year two, 65% in year three and 70% in year four. The reusable load-test baseline is the
year-one database volume; the tests exercise new registrations and other journeys against it.

```text
year 1 registrations = 4,900,000 * 0.50 = 2,450,000
year 2 registrations = 4,900,000 * 0.55 = 2,695,000
year 3 registrations = 4,900,000 * 0.65 = 3,185,000
year 4 registrations = 4,900,000 * 0.70 = 3,430,000

year-one completed properties = 2,450,000
incomplete journeys  = ceil(completed properties * 0.10 / 0.90)
                     = 272,223
total generated property registrations = 2,722,223

registration journeys/minute = annual registrations / working days / active hours per day / 60
                             = 2,450,000 / 200 / 8 / 60 = 25.520833
agreed rounded load target   = 26 registrations/minute
eight-hour registration capacity = 26 * 60 * 8 = 12,480
basic-suite registrations    = 10 measured passes + 1 warm-up = 11
minimum free-address reserve = 12,480 + 11 = 12,491
```

The 200 working days and 8 active hours/day are planning assumptions, not observed traffic. The 90% completed /
10% incomplete mix retains the existing synthetic seed assumption; it is not part of the fee-model forecast.
All counts can be overridden independently. `NFT_SEED_PROPERTIES` now counts **completed** registrations, unlike
its previous combined-total meaning; update any existing task/environment overrides accordingly.

The 26/minute figure is a **completed property-registration journey arrival rate**, not an HTTP request rate or
concurrent-user count. The load suite is intended to exercise registrations and other service journeys; the
registration rate is the quantified component of that workload, while the mix and rates for the other journeys are
separate inputs. An eight-hour modeled day requires 12,480 addresses for new registrations. The basic simulation
also creates 11 registrations (one functional warm-up and 10 measured passes), so the shared starting dump reserves
12,491 eligible free addresses in total. The five `EG1 2AA` Gatling lookup addresses are additional reserved
fixtures and are never part of this allocation. This is the agreed full-day registration capacity, not extra
safety margin or a spike-test concurrency target. The seeder's final `unallocated addresses remain` count is the
capacity check: it subtracts addresses allocated to both completed registrations and incomplete journeys.

For the local dump rebuild from the existing preserved address pool, which had one unallocated address, use
`NFT_SEED_GENERATED_ADDRESSES=12490` and `NFT_SEED_MINIMUM_FREE_ADDRESSES=12491`. The generated-address value is
additional to that retained pool; recalculate it if the source address pool or baseline counts change.

The basic suite updates phone and email on the fixed synthetic landlord subject
`urn:fdc:gov.uk:2022:mGHDySEVfCsvfvc6lVWf6Qt9Dv0ZxPQWKoEzcjnBlUo`. Before creating a performance dump, apply
`src/main/resources/data-nft-performance.sql`; it assigns this subject to an unlinked synthetic individual landlord
and adds five high-ID `EG1 2AA` lookup addresses excluded from bulk allocation. New-registration journeys generate
distinct subjects for each run and authenticate them through the existing One Login simulator; those identities are
not pre-seeded. A later login spike can reuse the dump, but its simultaneous-login profile and account capacity must
be agreed separately.

Generated certificate metadata does not provide actual certificate files in S3. Record the tested image revision,
random seed, reference date and actual generated counts when creating a dump. The existing ECS seeder task has its
own image and environment settings: override its old `NFT_SEED_LANDLORDS=2820000` and `NFT_SEED_PROPERTIES=4700000`
with `0` and `2450000`, and explicitly supply `NFT_SEED_INCOMPLETE_PROPERTIES=272223`. Use the tested image and a
deliberately selected reference date rather than assuming the task definition inherits these new defaults.

The large dump is intended to be created once and loaded before performance testing, with migrations applied to
match the deployed NFT revision. Basic, load and later spike suites should use the same restored starting database
in one run; do not reset between suites. The existing performance workflow currently runs only the basic suite and
has a 60-minute job timeout; this documentation does not add load/spike simulations, extend that timeout, or add a
schedule. A full eight-hour capacity is the agreed dataset sizing model, not a claim that the current workflow can
run for eight hours. Any future scheduling and suite orchestration must account for the selected test duration.

After the run, including a failed run, the existing cleanup resets NFT to the small day-to-day fixture and starts
the app. It does not modify or replace the large S3 dump. A subsequent run restores the large dump again before
testing. The loader verifies at least the configured completed and incomplete registration counts before the
performance workflow continues. Its defaults match the deployed seeder targets, and can be overridden with GitHub
Environment variables `NFT_SEED_PROPERTIES` and `NFT_SEED_INCOMPLETE_PROPERTIES`. The minimum eligible free-address
check is independently configurable with GitHub Environment variable `NFT_LOAD_MINIMUM_FREE_ADDRESSES` (default
`1`); the reusable full-day dump is sized separately with its 12,491-address reserve. These loader checks do not
change the dataset or the seeder's own configuration.

### NFT basic performance workflow (PDJB-430)

`.github/workflows/run-performance-tests.yml` is dispatched on the `nft` branch. It first checks whether the NFT
head differs from the last successful run; if unchanged, it skips all AWS and database operations unless `force`
is selected. The deployed app image must carry the workflow's NFT SHA. Before testing, the workflow stops the
one-task NFT app, restores the versioned (or ETag-conditional) `s3://prsdb-seed-data-nft/seed-data.dump`, validates
the archive, migrates it to the deployed NFT revision and verifies required data. It then starts the app and runs
the existing `BasicJourneysSimulation` in baseline mode after the One Login simulator is prepared. Additional
load/spike/performance suites should run against this same restored dataset before cleanup; do not reset between
suites in a combined run. The performance job uploads Gatling reports and always attempts to restore the original
app/simulator state and its own WAF/security-group changes.

After the prepare/test/restore job completes, every selected execution invokes the shared database reset, even
when dump loading, preparation, Gatling or environment restoration failed. This is the final cleanup: it cleans
and migrates the NFT schema, then returns NFT to the small `data-test.sql` fixture for day-to-day use. It does
not reload the large dump. The manual `Reset Database - NFT` workflow has the same small-fixture semantics and
shares the `nft-app-service-operation` concurrency lock with performance runs and webapp NFT deployments. After
the reset succeeds, the workflow ensures the one-task app is running and healthy.

The large baseline is created separately, normally once, by the existing NFT ECS seed-and-dump task. It may be
deliberately regenerated in the future, but normal performance runs only read it; neither loading nor the final
small-fixture reset writes or deletes the S3 object. A run that needs a replacement baseline must use the
separately approved seed-and-dump operation.

The manual `fail-smoke` input injects a failure after simulator setup but before Gatling. The separate
`fail-gatling` input uses an unseeded synthetic landlord so the journey fails after Gatling starts. Both inputs
force the workflow past the unchanged-head check. Gatling reports are uploaded even when the simulation fails.
The successful NFT head marker advances only when dump restore, preparation, all configured performance suites,
post-run small-fixture reset and app recovery succeed. Failed test or reset attempts do not advance it.

The scripts record the original app task ARN, simulator task/count, temporary ARN and owned WAF/SG
entries in logs (and in the handoff state file). They require a stable one-task app using `default,nft`
and a stopped simulator. Preparation:

1. Adds its runner IPv4 `/32` only to the designated runner WAF IP sets and simulator ALB security
   group, preserving pre-existing entries.
2. Starts `nft-one-login-simulator` at one task and checks ECS stability and simulator HTTP health.
3. Registers a temporary revision in **the existing `prsdb-webapp-nft` family**, preserving the exact
   deployed image/settings/roles/secret references and adding only the `one-login-simulator` profile.
   Updating the existing app service to that revision restarts it with simulator authentication.
4. Waits for the app's `/healthcheck`, verifies simulator discovery, and checks the app authorization
   endpoint redirects to the simulator with the correct client/callback. It never follows that
   redirect or calls the blocked simulator `/config` endpoint.

Restoration (run unconditionally, even after a prior-step failure):

5. Restores the original app task and verifies its health **before** scaling simulator back to zero.
   It removes only its owned access, then deregisters the temporary app revision.

Manual dispatch examples:

```bash
gh workflow run run-performance-tests.yml --ref nft -f force=true
# Fail before Gatling to prove environment restoration:
gh workflow run run-performance-tests.yml --ref nft -f force=true -f fail-smoke=true
# Fail a Gatling journey to prove report retention, environment restoration and database reset:
gh workflow run run-performance-tests.yml --ref nft -f fail-gatling=true
```

Inspect the exact resulting run and compare the Gatling artifact, original/restored app task ARN/profile/health,
simulator desired/running count zero, owned access removal, temporary revision retirement and database-reset
outcome. The `fail-smoke` case never starts mutating journeys.

**Interrupted-run recovery:** ordinary errors and signals trigger bounded best-effort cleanup, but
hard cancellation/runner loss can prevent it. Do not start another NFT deployment or readiness run
until an operator has restored/verified the environment. This slice deliberately adds no SSM journal
or automatic recovery service. Use the run's logs to obtain `ORIGINAL_APP_TASK`, `TEMPORARY_APP_TASK`,
runner CIDR, WAF names/IDs and SG marker (`PDJB-430:<run-id>-<attempt>`).

First inspect live service state:

```bash
aws ecs describe-services --profile prsdb-nft --region eu-west-2 --cluster nft-app \
  --services nft-app nft-one-login-simulator \
  --query 'services[].{Name:serviceName,Task:taskDefinition,Desired:desiredCount,Running:runningCount}'
```

```powershell
aws ecs describe-services --profile prsdb-nft --region eu-west-2 --cluster nft-app `
  --services nft-app nft-one-login-simulator `
  --query 'services[].{Name:serviceName,Task:taskDefinition,Desired:desiredCount,Running:runningCount}'
```

If the app still uses **this run's temporary ARN**, restore the original ARN recorded in its logs.
If it already uses the original ARN, do not unnecessarily redeploy. If it uses any other task ARN,
coordinate with that deployment's owner rather than overwriting it. With `ORIGINAL_APP_TASK` set
to the recorded original ARN:

```bash
aws ecs update-service --profile prsdb-nft --region eu-west-2 --cluster nft-app \
  --service nft-app --task-definition "$ORIGINAL_APP_TASK"
aws ecs wait services-stable --profile prsdb-nft --region eu-west-2 --cluster nft-app --services nft-app
curl --fail --max-time 15 https://nft.register-home-to-rent.test.communities.gov.uk/healthcheck
# Only once the original app is stable and healthy:
aws ecs update-service --profile prsdb-nft --region eu-west-2 --cluster nft-app \
  --service nft-one-login-simulator --desired-count 0
aws ecs wait services-stable --profile prsdb-nft --region eu-west-2 --cluster nft-app \
  --services nft-one-login-simulator
```

```powershell
aws ecs update-service --profile prsdb-nft --region eu-west-2 --cluster nft-app `
  --service nft-app --task-definition "$ORIGINAL_APP_TASK"
aws ecs wait services-stable --profile prsdb-nft --region eu-west-2 --cluster nft-app --services nft-app
curl.exe --fail --max-time 15 https://nft.register-home-to-rent.test.communities.gov.uk/healthcheck
# Only once the original app is stable and healthy:
aws ecs update-service --profile prsdb-nft --region eu-west-2 --cluster nft-app `
  --service nft-one-login-simulator --desired-count 0
aws ecs wait services-stable --profile prsdb-nft --region eu-west-2 --cluster nft-app `
  --services nft-one-login-simulator
```

In PowerShell, set the variable first with `$ORIGINAL_APP_TASK = "<recorded ARN>"`, and use `curl.exe`
because `curl` is an alias for `Invoke-WebRequest`.

After verifying no service/deployment references `TEMPORARY_APP_TASK`, deregister that exact ARN:

```bash
aws ecs deregister-task-definition --profile prsdb-nft --region eu-west-2 \
  --task-definition "$TEMPORARY_APP_TASK"
```

```powershell
aws ecs deregister-task-definition --profile prsdb-nft --region eu-west-2 `
  --task-definition "$TEMPORARY_APP_TASK"
```

For network recovery, use `wafv2 get-ip-set` then `update-ip-set` with the **fresh** lock token and
current address list minus only the logged run-owned CIDR. CloudFront WAF uses `us-east-1`/`CLOUDFRONT`;
regional WAF uses `eu-west-2`/`REGIONAL`. Do not restore an old complete address-list snapshot. Use
`ec2 describe-security-group-rules` to find the exact logged CIDR and run description, then revoke
only those rule IDs with `ec2 revoke-security-group-ingress`. Leave all pre-existing entries alone.

If registration succeeded but its response was lost, the script might not know the new ARN. Inspect
the webapp task family/latest revision and compare its original image/settings and simulator profile
to this run before retiring it. **An active temporary revision can become family-latest and be picked
by existing deployment tooling**; resolve retirement before another NFT deployment. Do not rotate
credentials or deploy a newer image as a recovery shortcut.


### Prerequisites for a local run

1. For the real basic suite, start the app using `scripts/performance-tests/start-local.mjs` as described above.
   The standard `local,local-no-auth` run configuration does not exercise One Login authentication.
2. Confirm the app is up and note the port from your `.env`.

`RegisterAsLandlordGetSimulation` targets an existing public page and needs nothing else.

`FormSubmissionPostSimulation` targets the POST endpoint added for performance testing, which is gated behind
the `pdjb-238-gatling-post-endpoint` feature flag. That flag is enabled only in the NFT environment, so to run
this simulation locally you must enable it first, by either:

- setting `enabled: true` for that flag in `src/main/resources/application-local.yml`, then restarting the app
  (remember to revert the change before committing); or
- toggling it at runtime via the feature flag overrides page at `/system-operator/feature-flags`.

If the flag is off, the endpoint returns 404 and the simulation reports both a failed status check and a
`No attribute named 'csrfToken' is defined` error, because the CSRF token could never be extracted.

> Note: edits to `src/main/resources` only reach a running app once Gradle has copied them into the build
> output. If a config change appears not to take effect, run `./gradlew processResources` (`.\gradlew.bat
> processResources` on Windows) and let the app restart.

## Reports

Gatling writes an HTML report per run to:

```
build/reports/gatling/<simulation>-<timestamp>/index.html
```

The console output prints the exact path at the end of each run. These reports are build output and are
**not** committed — the repository's `.gitignore` excludes `build/`. In CI, publish the report directory as a
workflow artifact so each run's results can be downloaded and analysed.

## Code patterns

The two simulations establish the conventions new tests should follow.

**Base URL** — the toy examples use `BaseUrlConfig.baseUrl`; the basic suite uses the explicit
`BasicRunConfig.baseUrl`. Neither hardcodes its application origin in a request:

```kotlin
private val httpProtocol =
    http
        .baseUrl(BaseUrlConfig.baseUrl)
        .acceptHeader("text/html")
```

**CSRF-protected POSTs** — the webapp requires a CSRF token on every form submission. GET the page first,
extract the hidden input, then reference it in the POST. Gatling automatically carries the session cookie
issued by the GET into the subsequent POST for the same virtual user, so the token stays valid:

```kotlin
.exec(
    http("Get form-submission page")
        .get("/performance-test/form-submission")
        .check(status().`is`(200))
        .check(css("input[name='_csrf']", "value").saveAs("csrfToken")),
).exec(
    http("Submit form-submission")
        .post("/performance-test/form-submission")
        .formParam("_csrf", "#{csrfToken}")
        .check(status().`is`(200)),
)
```

**Assertions** — check the status of every request. A simulation that does not assert anything will pass even
when the application is returning errors.

**Injection profile** — the example simulations use `atOnceUsers(3)`, which is deliberately small so they can
be run safely against a local machine. Real performance tests should use a profile matching the load being
modelled (for example `rampUsers(n).during(duration)`).
