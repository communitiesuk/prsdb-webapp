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
  src/gatling/resources/               # Feeder files, logback config, etc.
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

Run a single simulation by fully qualified class name:

```bash
./gradlew :performance-tests:gatlingRun \
  --simulation uk.gov.communities.prsdb.webapp.performance.RegisterAsLandlordGetSimulation
```

Without `--all` or `--simulation`, the task prompts you to choose a simulation interactively. For CI, always
pass one of those options (and `--non-interactive`) so the build never blocks waiting for input.

Gatling fails the build if any request assertion fails, so these tasks can be wired into CI as-is.

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

**Base URL** — always build the protocol from `BaseUrlConfig.baseUrl` so every simulation is retargetable:

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
