# Performance Tests

Gatling performance tests for the PRSDB webapp, written using Gatling's Kotlin DSL.

This is a standalone Gradle module. It deliberately has **no dependency on the root webapp project** — the
simulations drive the application over HTTP rather than reusing its classes.

## Layout

```
performance-tests/
  build.gradle.kts
  src/gatling/kotlin/uk/gov/communities/prsdb/webapp/performance/
    BaseUrlConfig.kt                   # Resolves the target base URL
    RegisterAsLandlordGetSimulation.kt # GET-only example
    FormSubmissionPostSimulation.kt    # GET -> extract CSRF -> POST example
    BasicJourneysSimulation.kt         # Serial one-user populations: phone/email updates -> registration
  src/main/resources/fixtures/         # Synthetic One Login identity-verification claims
  src/main/kotlin/                     # Reusable HTTP chains and run configuration
  src/test/kotlin/                     # Unit tests and Gatling assertion tests
  scripts/start-local.mjs              # Local official-simulator application bootstrap
```

Simulations live in `src/gatling/kotlin`, which is the source set the
[`io.gatling.gradle`](https://docs.gatling.io/reference/integrations/build-tools/gradle-plugin/) plugin adds.

## Choosing the target environment

`BaseUrlConfig` reads the `gatling.baseUrl` system property and falls back to `http://localhost:8080`, so the
simulations work against a locally running webapp with no extra configuration.

To target a deployed environment, pass the property on the command line:

```bash
./gradlew :performance-tests:gatlingRun -Dgatling.baseUrl=https://<environment-host>
```

If you run the webapp locally on a non-default port (git worktrees are assigned a unique `SERVER_PORT` in their
`.env`), pass that port explicitly:

```bash
./gradlew :performance-tests:gatlingRun -Dgatling.baseUrl=http://localhost:<port>
```

## Running the simulations

Run every simulation in the module:

```bash
./gradlew :performance-tests:gatlingRun --all
```

`--all` now includes the real basic suite: supply its properties and start the official simulator as described
below. The toy POST example also needs its feature flag enabled.

Run a single simulation by fully qualified class name:

```bash
./gradlew :performance-tests:gatlingRun \
  --simulation uk.gov.communities.prsdb.webapp.performance.RegisterAsLandlordGetSimulation
```

Without `--all` or `--simulation`, the task prompts you to choose a simulation interactively. For CI, always
pass one of those options (and `--non-interactive`) so the build never blocks waiting for input.

Request checks mark incorrect responses as failed requests. To fail the simulation and build when a request
check fails, also register a simulation assertion such as `global().failedRequests().count().is(0L)`
(`is` requires backticks in Kotlin). The toy examples currently demonstrate request checks only.

### Developing the basic suite locally

The basic-suite foundations have independent module tests. The run configuration and `HttpDestination` have unit
tests, and a small set of tests execute Gatling against a disposable loopback HTTP server, with one virtual user.
None of them need the webapp, Docker, AWS credentials or NFT:

```bash
./gradlew :performance-tests:test --console=plain
```

Build and Test runs these in the separate "Performance module tests" job.

The Gatling tests prove that incorrect bodies/content types, missing CSRF tokens and skipped requests fail the
runner, and that the maximum timing gate rejects deliberately delayed responses while a correct run within the
limit passes. They do **not** prove that the journey chains still work against the application, and they do
not measure performance: only running the simulation against the real app (below) does that.

`BasicRunConfig` separates the target from the measurement mode. Both targets use the same HTTP journey logic;
the existing toy simulations continue to use `BaseUrlConfig` unchanged.

| Property | Purpose |
|----------|---------|
| `gatling.target` | Explicit `local` or `nft` target |
| `gatling.baseUrl` | Application origin, including the local/worktree port |
| `gatling.simulatorUrl` | Official One Login simulator origin |
| `gatling.basic.landlordSubject` | Seeded synthetic landlord for the phone and email update slices |
| `gatling.basic.registrationSubject` | Fresh, unregistered synthetic subject for registration; must differ from `landlordSubject` |
| `gatling.basic.mode` | `baseline` records timings; `gated` applies explicit timing limits |
| `gatling.local.basic.maxResponseTimeMs` | Local gated per-request limit, positive integer milliseconds |
| `gatling.nft.basic.maxResponseTimeMs` | Separately agreed NFT gated per-request limit |

URLs must be HTTP/HTTPS origins, without credentials, paths, query strings or fragments. Baseline mode rejects
limits for its target rather than ignoring them. Gated mode requires the limit for its target; NFT never
inherits local timing limits.

`BasicPerformanceAssertions` always requires zero failed requests and exactly one execution of each named
application request. This also detects skipped requests and request-building failures. In gated mode it applies the
maximum limit to each explicitly named application request, so a fast page cannot hide a slow one in a global average.
There is deliberately no mean limit: the basic profile is a single user making each request once (see ADRs
0035-0040), so the mean would always equal the maximum. Mean or percentile limits belong in future load
simulations that make repeated requests.
Local limits prove the gates work; they are not NFT acceptance limits. Future load simulations can reuse journey
chains, but must define their own workload rather than changing the single-user basic profile.

Build and Test runs the module's unit and assertion tests (`:performance-tests:test`) in their own job, but never
the simulations. Its webapp test shards explicitly invoke `:test` to avoid selecting the performance module's
tests. Automated performance execution belongs in the dedicated PDJB-430 performance-testing action; the
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
node --env-file=.env performance-tests/scripts/start-local.mjs
```

```powershell
$env:FLYWAY_CLEAN_DISABLED = "false"; .\gradlew.bat flywayClean --console=plain; Remove-Item Env:FLYWAY_CLEAN_DISABLED
node --env-file=.env performance-tests/scripts/start-local.mjs
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
./gradlew :performance-tests:gatlingRun --non-interactive \
  --simulation uk.gov.communities.prsdb.webapp.performance.BasicJourneysSimulation \
  -Dgatling.target=local -Dgatling.basic.mode=baseline \
  -Dgatling.baseUrl=http://localhost:8080 \
  -Dgatling.simulatorUrl=http://127.0.0.1:13000 \
  -Dgatling.basic.landlordSubject=urn:fdc:gov.uk:2022:UVWXY \
```

```powershell
.\gradlew.bat :performance-tests:gatlingRun --non-interactive `
  --simulation uk.gov.communities.prsdb.webapp.performance.BasicJourneysSimulation `
  "-Dgatling.target=local" "-Dgatling.basic.mode=baseline" `
  "-Dgatling.baseUrl=http://localhost:8080" `
  "-Dgatling.simulatorUrl=http://127.0.0.1:13000" `
  "-Dgatling.basic.landlordSubject=urn:fdc:gov.uk:2022:UVWXY" `
  "-Dgatling.basic.registrationSubject=urn:fdc:gov.uk:2026:PERF-REGISTRATION-001"
```

Match the URLs to the ports used by the bootstrap. The Gradle module forwards `gatling.*` properties to the
simulation JVM and excludes test-only simulations from production runs. Use a new, unregistered synthetic
registration subject for each run, or restore the local database before reusing one; the registration chain stops
at its preflight if that subject already has a landlord registration.

The basic suite first signs in as seeded landlord Alexander Smith, opens the rendered phone-change link, preserves
the journey URL and CSRF token, submits the fictional London number `02079460123`, then checks that exact number
in the saved phone summary row. It then follows the rendered email-change link in that same session, preserves
the email journey URL and hidden fields, submits `performance-landlord@example.invalid`, and checks that address
in the saved email summary row. Run with the local profile and do not enable `use-notify`; local email
notifications are handled by the stub service. A separate sequential user signs in with the unregistered registration subject,
performs identity verification with synthetic claims, accepts the privacy notice, and completes the individual
manual-address registration. Registration clears the app session to refresh the user's roles, so the journey
reauthenticates with the same subject before checking the confirmation registration number and following the
dashboard link.
The two one-user populations run serially, so no app or simulator cookies cross between identities. Each
redirect is a separate named request; there is one active virtual user at a time, no embedded-resource fetching
and no load injection.

Reset/reseed between comparable baselines so the seeded phone/email values and registration subject return to their
initial state. The registration fixtures are synthetic and do not contain real identity data.
For a local timing-gate experiment, change the mode to `gated` and supply
`gatling.local.basic.maxResponseTimeMs`. This is an explicit local
limit, not an agreed NFT SLA. Simulator front-channel requests are checked for correctness but excluded from
application timing gates; the application callback includes its back-channel authentication work.

The local basic slice now covers seeded phone and email updates and individual registration through the manual-address
branch. It does not cover other registration branches, council or letting-agent journeys, or run the scheduled
NFT workflow. See ADRs 0035-0040 for the basic/load distinction, API-level measurement, runner, scheduling,
Gatling and fixture-restoration decisions.

### Daily NFT performance-test orchestrator (PDJB-430 first slice)

`.github/workflows/schedule-performance-tests.yml` runs on `main` at **19:00 UTC daily** (chosen to avoid
the 1–2am maintenance windows) and only dispatches `run-performance-tests.yml` on the `nft` branch
(`gh workflow run run-performance-tests.yml --ref nft`). The orchestrator workflow and its scripts run
entirely from `nft`, so `main` never needs access to the `nft` environment, and the observed NFT SHA is
simply the checked-out `HEAD`. It runs on a standard GitHub-hosted runner and can also be dispatched
manually from `nft`. If NFT has not changed since the last successful workflow, it skips **before
assuming AWS credentials**. The small `nft-head-sha` artifact records success (including unchanged
skips) for 30 days. Missing/expired history causes a fresh run; GitHub/API errors or malformed markers
fail explicitly. Failures do not advance the marker. The deployed app image must also carry the
observed NFT SHA, so a failed deployment cannot cause an older app to be marked ready.

The `performance-tests` job runs three steps on the same runner (so the IP it allowlists for WAF/SG
access stays valid throughout): **Prepare environment** switches the app to simulator authentication
and smoke-checks it; **Run performance tests** is currently a stub (see below); **Restore environment**
(`if: always()`) always runs, even if preparation or the test step failed, and returns the app/simulator
and any owned WAF/SG entries to their original state. Preparation and restoration are separate scripts
(`nft-prepare.mjs`/`nft-restore.mjs`, sharing helpers from `nft-shared.mjs`) that hand off a small JSON
state file between them, rather than one script performing an atomic switch-then-restore.

This first slice **does not run Gatling**, complete login, submit journey forms, restore data, or send
notifications. It proves the existing simulator can start, the existing app can switch to simulator
authentication, and both can return to their original state. Daily journey execution remains a follow-up:
prove ADR-0040 fixture restoration, session handling and safe Notify recipients first. A successful run
is not a performance result and does not complete PDJB-430's eventual performance-execution requirement.
The "Run performance tests" step is a placeholder until the Gatling simulations (PDJB-552, see PR #2028)
are wired in here to run between preparation and restoration.

Before enabling the workflow:

1.  Verify the **simulator service's exact referenced task**, not simply the latest family revision, uses the approved digest
   `sha256:5257554c6f6a50c471ad231bc8f13da4a866b4a2e320a6d1f2f74e5d0ad52755` and current shared simulator
   settings. Do not rotate or overwrite the existing client ID/public/private credentials. The runner
   checks app/simulator snapshots agree without reading the private key.
2. Apply the separately reviewed extension to `nft-performance-test-network-access`. It needs app
   service describe/update, webapp-family task registration, existing app-role PassRole, and simulator
   ECR image reads, in addition to existing simulator/network access. AWS task-definition describe and
   deregister do not support resource-level restrictions: those actions are region-restricted to
   `eu-west-2`, and the scripts retire only their own returned temporary ARN. No Terraform-admin, DB,
   SSM, secret-value or `RunTask` permissions are used.
3. Keep the GitHub `nft` environment's deployment branches restricted to `nft` (as for normal NFT deploys),
   without required reviewers, before granting the expanded role. The workflow also rejects dispatches
   from other refs.
4. Merge the workflow/scripts into `main` (so the scheduler and workflow are registered) and release them to `nft` before dispatching.
   The scheduler is live as soon as it reaches `main`, and its dispatch fails until `nft` has the workflow, so
   run `gh workflow disable schedule-performance-tests.yml` straight after merging to `main`, and
   `gh workflow enable schedule-performance-tests.yml` once the changes are released to `nft`. Do not run NFT infra applies, manual
   ECS changes, or network changes while it is running. The shared non-cancelling concurrency group
   serializes this action with the **webapp** NFT deploy workflow, not the separate infra repository.

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

Manual proof commands:

```bash
gh workflow run run-performance-tests.yml --ref nft -f force=true
# A separate controlled-failure run must fail while still restoring the environment:
gh workflow run run-performance-tests.yml --ref nft -f force=true -f fail-smoke=true
```

Inspect the exact resulting run and compare original/restored app task ARN/profile/health, simulator
desired/running count zero, owned access removal and temporary revision retirement. The `fail-smoke`
input injects failure after the app starts with simulator auth; it never enables mutating journeys.

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

After verifying no service/deployment references `TEMPORARY_APP_TASK`, deregister that exact ARN:

```bash
aws ecs deregister-task-definition --profile prsdb-nft --region eu-west-2 \
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

1. For the real basic suite, start the app using `performance-tests/scripts/start-local.mjs` as described above.
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
> output. If a config change appears not to take effect, run `./gradlew processResources` and let the app
> restart.

## Reports

Gatling writes an HTML report per run to:

```
performance-tests/build/reports/gatling/<simulation>-<timestamp>/index.html
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
