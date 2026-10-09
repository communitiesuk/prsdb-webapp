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

### Daily NFT performance-test orchestrator (PDJB-430 first slice)

`.github/workflows/run-performance-tests.yml` runs at **19:00 UTC daily** (chosen to avoid the 1–2am
maintenance windows) on a standard GitHub-hosted runner and can be dispatched manually from `main`. It
observes the `nft` branch. The workflow and scripts come from `main`; a separate `nft-source` checkout
supplies the NFT SHA. If NFT has not changed since the last successful workflow, it skips **before
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
3. Restrict the GitHub `nft` environment's deployment branches to `main`, without required reviewers,
   before granting the expanded role. The workflow also rejects dispatches from other refs.
4. Merge the workflow/scripts into `main` before dispatching. Do not run NFT infra applies, manual
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
gh workflow run run-performance-tests.yml --ref main -f force=true
# A separate controlled-failure run must fail while still restoring the environment:
gh workflow run run-performance-tests.yml --ref main -f force=true -f fail-smoke=true
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
