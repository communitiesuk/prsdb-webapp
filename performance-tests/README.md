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
    BasicJourneysSimulation.kt         # One user: simulator login -> phone update
  src/gatling/resources/               # Feeder files, logback config, etc.
  src/main/kotlin/                     # Reusable HTTP chains and run configuration
  src/test/kotlin/                     # Deterministic Gatling contract tests
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

The basic-suite foundations have independent module tests. These execute Gatling against a disposable loopback
HTTP server, with one virtual user, and do not need the webapp, Docker, AWS credentials or NFT:

```bash
./gradlew :performance-tests:test --console=plain
```

The contracts prove cookie continuity, CSRF extraction, form encoding and preservation of `journeyId`.
They also prove that incorrect bodies/content types, missing CSRF tokens, incomplete journeys and unsaved updates
fail the runner, and that maximum/mean timing gates reject deliberately delayed responses. Authentication
contracts preserve authorization queries, hidden fields, JSON textarea defaults and cookies, reject off-origin
destinations, and prevent mutation after failed login. These fixtures do **not** measure application performance;
use the real local run below for that.

`BasicRunConfig` separates the target from the measurement mode. Both targets use the same HTTP journey logic;
the existing toy simulations continue to use `BaseUrlConfig` unchanged.

| Property | Purpose |
|----------|---------|
| `gatling.target` | Explicit `local` or `nft` target |
| `gatling.baseUrl` | Application origin, including the local/worktree port |
| `gatling.simulatorUrl` | Official One Login simulator origin |
| `gatling.basic.landlordSubject` | Synthetic landlord identity from the target's fixtures |
| `gatling.basic.mode` | `baseline` records timings; `gated` applies explicit timing limits |
| `gatling.local.basic.maxResponseTimeMs` / `meanResponseTimeMs` | Local gated limits, positive integer milliseconds |
| `gatling.nft.basic.maxResponseTimeMs` / `meanResponseTimeMs` | Separately agreed NFT gated limits |

URLs must be HTTP/HTTPS origins, without credentials, paths, query strings or fragments. Baseline mode rejects
limits for its target rather than ignoring them. Gated mode requires both limits, with mean no greater than
maximum; NFT never inherits local timing limits.

`BasicPerformanceAssertions` always requires zero failed requests and exactly one execution of each named
application request. This also detects skipped requests and request-building failures. In gated mode it applies maximum and mean
limits to each explicitly named application request, so a fast page cannot hide a slow one in a global average.
Local limits prove the gates work; they are not NFT acceptance limits. Future load simulations can reuse journey
chains, but must define their own workload rather than changing the single-user basic profile.

Build and Test does not run performance-module contracts or simulations. Its webapp test shards explicitly
invoke `:test` to avoid selecting the performance module's tests. Automated performance execution belongs in
the dedicated PDJB-430 performance-testing action, which is still to be implemented; the commands here remain
available for explicit local development runs.

### Real local basic journey (macOS/Linux)

Requires Docker, Node 22+, OpenSSL, JDK 21 and the usual gitignored `.env` configuration. Run commands from the
repository root. This uses the standard local database and Redis, **not** a deployed environment.

Start the usual dependencies:

```bash
docker compose -f docker-compose.local.yml up -d --wait
```

Start the pinned official One Login simulator in a separate terminal:

```bash
docker run --rm --name prsdb-performance-simulator \
  -p 127.0.0.1:13000:3000 -e INTERACTIVE_MODE=true \
  ghcr.io/govuk-one-login/simulator@sha256:0d5e62c1db1c400c4881be2270b3f08aeb55c72ca3d9eb9a6e5196becef6f5e5
```

**Destructive local reset:** stop the application first, and only run the following against a disposable local
database. It deletes the local schema; application startup then migrates and loads `data-local.sql` again.
The Gradle Flyway task reads the database port from `.env`.

```bash
FLYWAY_CLEAN_DISABLED=false ./gradlew flywayClean --console=plain
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
  -Dgatling.basic.landlordSubject=urn:fdc:gov.uk:2022:UVWXY
```

Match the URLs to the ports used by the bootstrap. The Gradle module forwards `gatling.*` properties to the
simulation JVM and excludes test-only simulations from production runs.

The slice signs in as seeded landlord Alexander Smith, opens the rendered phone-change link, preserves the
journey URL and CSRF token, submits the fictional London number `02079460123`, then checks that exact number
in the saved phone summary row. Each redirect is a separate named request. There is exactly **one sequential
virtual user**, no embedded-resource fetching and no load injection.

Reset/reseed between comparable baselines so an existing update/session does not change the starting fixture.
For a local timing-gate experiment, change the mode to `gated` and supply both
`gatling.local.basic.maxResponseTimeMs` and `gatling.local.basic.meanResponseTimeMs`. These are explicit local
limits, not agreed NFT SLAs. Simulator front-channel requests are checked for correctness but excluded from
application timing gates; the application callback includes its back-channel authentication work.

This first slice does not yet cover registration, identity verification, council or letting-agent journeys, or
run the scheduled NFT workflow. See ADRs 0035-0040 for the basic/load distinction, API-level measurement,
runner, scheduling, Gatling and fixture-restoration decisions.

### Prerequisites for a local run

1. Start the webapp locally using the standard `local` run configuration (it starts the Docker Compose
   dependencies, runs the Flyway migrations, and activates the `local,local-no-auth` profiles). See the
   project's main README for details.
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
