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
  BasicJourneysSimulation.kt           # Serial one-user populations: profile updates -> registration
  SavedRegistrationSimulation.kt       # Disjoint owners: resume/save one registration, delete another
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

| Property                                 | Purpose                                                                                                     |
|------------------------------------------|-------------------------------------------------------------------------------------------------------------|
| `gatling.target`                         | Explicit `local` or `nft` target                                                                            |
| `gatling.baseUrl`                        | Application origin, including the local/worktree port                                                       |
| `gatling.simulatorUrl`                   | Official One Login simulator origin                                                                         |
| `gatling.basic.landlordSubject`          | Seeded synthetic landlord for the profile update slices                                                      |
| `gatling.basic.registrationSubject`      | Fresh run-specific prefix for generated synthetic registration subjects; must differ from `landlordSubject` |
| `gatling.basic.iterations`               | Number of measured journey passes; positive integer, default `10`                                           |
| `gatling.basic.maxIterations`            | Safety ceiling for measured passes; positive integer, default `100`; raise explicitly to run more           |
| `gatling.basic.mode`                     | `baseline` records timings; `gated` applies explicit timing limits                                          |
| `gatling.local.basic.meanResponseTimeMs` | Local gated per-request mean limit, positive integer milliseconds                                           |
| `gatling.local.basic.maxResponseTimeMs`  | Local gated per-request limit, positive integer milliseconds                                                |
| `gatling.nft.basic.meanResponseTimeMs`   | Separately agreed NFT gated per-request mean limit                                                          |
| `gatling.nft.basic.maxResponseTimeMs`    | Separately agreed NFT gated per-request limit                                                               |

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
The webapp test shards run only the root `test` task. Automated performance execution belongs in the dedicated PDJB-430 performance-testing
action; the
commands here remain available for explicit local development runs.

### Owned saved-registration journeys

`SavedRegistrationSimulation` is a separate, destructive correctness slice for the restored synthetic dataset.
Each virtual user authenticates as a distinct existing owner, resumes a saved registration, retains its selected
address, submits property type and two bedrooms, and checks the saved value. It then deletes a **different**
saved registration and checks that the target disappeared while the resumed registration remains.
HTTP contracts reject missing CSRF, a mismatched resume ID, unsaved progress, retained delete targets and loss of
the unrelated record. No fixture is recycled within a run.

Export fixtures immediately before running. For local work, first set the usual `PGHOST`, `PGPORT`, `PGDATABASE`,
`PGUSER` and `PGPASSWORD` variables for the **disposable restored large database**, not the developer database:

```bash
PGOPTIONS='-c default_transaction_read_only=on' \
  psql --no-psqlrc --set ON_ERROR_STOP=1 --tuples-only --no-align \
  --file src/main/resources/data-nft-saved-registration-fixtures.sql > /absolute/path/saved-fixtures.json
./gradlew gatlingRun --non-interactive \
  --simulation uk.gov.communities.prsdb.webapp.performance.SavedRegistrationSimulation \
  -Dgatling.target=local -Dgatling.basic.mode=baseline \
  -Dgatling.baseUrl=http://localhost:18080 \
  -Dgatling.simulatorUrl=http://127.0.0.1:19090 \
  -Dgatling.basic.landlordSubject=urn:fdc:gov.uk:2022:mGHDySEVfCsvfvc6lVWf6Qt9Dv0ZxPQWKoEzcjnBlUo \
  -Dgatling.saved.fixturePath=/absolute/path/saved-fixtures.json
```

The shared SQL exports at most 101 owners, each with its two earliest owned saved registrations, covering
warm-up plus at most 100 measured passes. Both the exported subjects and state IDs must be disjoint.
The simulation fails before HTTP mutation if capacity, required fields or uniqueness are invalid. It uses
each exported subject for authentication; `gatling.basic.landlordSubject` remains required by the shared config.
The ordinary small local fixture does not provide this pool.

In NFT, the `fixtures` mode of the bounded SSM database action invokes
`node scripts/performance-tests/nft-load-dump.mjs --export-saved-fixtures <output-path>`.
It verifies the AWS account and tunnel configuration, executes read-only SQL, checks capacity/uniqueness and
writes a mode-0600 file in the runner's temporary directory. The performance workflow runs the basic suite and
then this saved-registration suite against the same restored database, with no reset between them.
These changes are locally verified, not yet proven by another GitHub/NFT execution.

The combined large-local check passed 913/913 basic-suite requests followed by 209/209 saved-registration
requests (26s and 10s respectively), each with warm-up plus ten measured passes. A separate database query
confirmed all 11 resumed states retained their owned address and saved bedrooms, all 11 delete targets were
removed, and allocation/schema checks remained valid. This is not a concurrency or NFT timing result.
The pristine archive is unchanged; restore it again for a comparable starting point.

### Owned completed-property and organisation updates

Two further destructive correctness simulations use separate read-only fixture exports:

| Simulation | Implemented variants | Verified large-local result |
|---|---|---|
| `CompletedPropertySimulation` | Ownership, bedrooms, furnishings, households/tenants, rent frequency/amount, water/custom/excluded bills, account/different correspondence email, selected/manual correspondence address, vacant/occupied and deferred tenancy completion; selective, mandatory HMO, additional HMO and removed licensing | 1,771/1,771 requests, 1m 54s |
| `OrganisationProfileSimulation` | Organisation name, email, phone, selected/manual address and main contact, authenticated as existing organisation administrators | 440/440 requests, 11s |

Both results include warm-up plus ten measured passes, with a different owner/organisation per pass.
Independent database queries verified all 11 allocated property updates and all 11 organisation profile
updates, retaining the expected ownership/admin membership and registration number. The property audit also
confirmed the final manual contact address, occupied status, completed tenancy and cleared custom bills.
These are sequential
correctness timings, not concurrent-load results or NFT acceptance measurements.

Export `data-nft-completed-property-fixtures.sql` or `data-nft-organisation-fixtures.sql` using the read-only
`psql` command above against the disposable restored database. Both exports are bounded to 101 distinct
actors. Property fixtures exclude shared ownership, delegation and owners with saved registrations.
Organisation fixtures require actual admin memberships and never allocate the same organisation twice.
The parsers reject underfilled, shared or invalid fixtures before authenticating.

Use the common local simulation options above, replacing the simulation and fixture argument with:

```text
--simulation uk.gov.communities.prsdb.webapp.performance.CompletedPropertySimulation
-Dgatling.property.fixturePath=/absolute/path/property-fixtures.json

--simulation uk.gov.communities.prsdb.webapp.performance.OrganisationProfileSimulation
-Dgatling.organisation.fixturePath=/absolute/path/organisation-fixtures.json
```

**Certificate storage is a separate fixture dependency.** The seed archive contains certificate metadata,
not the referenced file bodies. Some property detail pages fail locally if those files are missing.
The property export includes the actual referenced `files` (seed-owned object keys and extensions). Prepare
their synthetic bodies before starting the isolated app:

```bash
mkdir -p /absolute/session/path/isolated-app
node scripts/performance-tests/local-seed-files.mjs \
  /absolute/path/property-fixtures.json /absolute/session/path/isolated-app
```

The storage root must already exist, outside the repository and its ancestors. Preparation is bounded to
101 distinct owners and 20 file references per property, accepts only seed-owned PNG/PDF keys, rejects
symlinked upload paths and different existing objects, and is repeatable without overwriting files.
It creates real synthetic PNG/PDF bodies under `.local-uploads/safe/`; it does not change the database.
Run the isolated app with this directory as its working directory and an absolute jar path. Its local
One Login controller also needs a `src` link pointing to this checkout's `src` directory.
The recorded check prepared 122 PNGs; an authenticated HTTP download returned the expected content type
and 490 matching synthetic bytes. Developer uploads were not changed.
This only materialises existing seeded certificates. New upload/compliance journeys separately use real
synthetic PDFs and the unchanged production scanning task. NFT object storage provisioning/download
validation remains outstanding; the archive alone is insufficient for end-to-end NFT execution.

### Council and operator local dependencies

Council chains use the internal-access OIDC provider; operator chains use One Login with real
`system_operator` memberships. Neither uses a broad role override.
The official simulator needs a **separate interactive
instance** for the RS256 internal-access client; do not overwrite the ES256 One Login configuration.
Start it on a distinct loopback port using the same simulator image as the local landlord instance:

```bash
docker run -d --name <owned-council-simulator> -e INTERACTIVE_MODE=true \
  -p 127.0.0.1:19091:3000 ghcr.io/govuk-one-login/simulator:<local-version>
COUNCIL_SPRING_CONFIG_PATH=/absolute/session/path/council-spring.json \
  node scripts/performance-tests/council-local-simulator.mjs
```

The bootstrap reads the existing One Login simulator configuration at `127.0.0.1:19090`, configures
only the separate council simulator, checks its interactive form and issuer, and writes a new
mode-0600 Spring override file. Set `ONE_LOGIN_SIMULATOR_URL`, `COUNCIL_SIMULATOR_URL` and
`COUNCIL_APP_URL` explicitly when using other loopback ports. Merge the generated overrides into
the isolated application's configuration and restart it. Pass
`-Dgatling.council.simulatorUrl=http://127.0.0.1:19091`; `gatling.simulatorUrl` still identifies One Login.
The simulations fail closed on unexpected external destinations.

Fresh ten-pass actual-app runs subsequently passed 409/409 council requests in 22s and 300/300
operator requests in 10s, each plus warm-up. Independent read-only audits confirmed all eleven target
removals, invitation cleanup and preserved actor memberships. Missing-invitation pagination was
reproduced and fixed with a contract regression; partial-run invitations and removals were recovered
through scoped HTTP journeys before allocating fresh measured fixtures.
The shared seed audit reported zero allocation/schema violations after the initial pilots.
These are sequential local timings, not concurrent-load results or GitHub/NFT validation.

The workflow now exports these pools through the guarded `property-fixtures` and `organisation-fixtures`
SSM action modes and runs both simulations after saved registrations, without resetting the database.
The guarded Node CLI options are `--export-property-fixtures` and `--export-organisation-fixtures`.
Council and operator exports also have guarded action modes
`council-fixtures` and `operator-fixtures`. Their equivalent CLI is:

```bash
node scripts/performance-tests/nft-load-dump.mjs --export-journey-fixtures council /absolute/path/council.json
```

Replace `council` with `operator` as appropriate. These exports use the
same account/tunnel guards and read-only database setting, reject shared actors/removal targets and
unsafe file references, and write owner-only files. Council detail pages require the referenced
certificate objects, not just database metadata. Council invitation registration, organisation-structure
variants, operator passcodes/metrics and payment-retry experiments are outside this normal-load scope.

Council and operator wiring has not yet been validated in GitHub/NFT. Local evidence above does not
establish their remote dependency readiness or a combined fresh-restore run.

### NFT workflow and mixed-ramp coverage

The mixed ramp currently has chains labelled for all 11 listed journey families, but **does not yet
implement all agreed representative paths or their prerequisites**. The table below describes the
current implementation, not completed PDJB-553 acceptance. Do not use `gatlingRun --all`
against NFT: that includes local-only payment journeys, contract simulations and examples. Property
registration uses the app-side performance-test payment mock; the simulation must not call the real
GOV.UK Pay service. The ramp is skipped unless the NFT GitHub environment variable
`NFT_PERFORMANCE_TEST_PAYMENT_MOCK_READY` is `true`; set it only after the app-side test profile is
configured to route payment API calls to the local mock. The workflow does not deploy or enable that
profile.

The agreed calibration profile has a five-minute linear ramp and fifteen-minute hold, including 26
actual property registrations per minute. The 1 arrival per minute for each of the other ten families
and the two-second think pauses after authentication and between completed-property stages remain
provisional. None of these rates is an approved capacity target or performance budget. The schedule yields 455 property
registrations and 17 arrivals per other family (625 starts total); arrivals are not completed-request
throughput. Mean and maximum timings are reported for calibration only.

| Journey family | Sequential NFT stage | Mixed ramp | Not covered by the representative chain |
|---|---|---|---|
| Individual landlord registration | Yes | 1/min | Other registration variants |
| Property registration | No | 26/min | One vacant, manually addressed, exempt variant; payment retries/pending states and registration-led certificate updates are excluded |
| Saved property registration resume/save/delete | Yes | 1/min | Uses owned incomplete properties rather than new registrations |
| Completed-property details and licensing/ownership/occupancy updates | Yes | 1/min | Uses distinct, exclusive owners and properties |
| Certificates and compliance | Yes | 1/min | Gas/electrical uploads on separate completed properties; registration-led uploads and other certificate variants are excluded |
| Organisation profile | Yes | 1/min | Initial registration, team invitations/access changes and structure variants |
| System operator | Yes | 1/min | Dashboard, administrator access changes and invite/cancel; passcodes and successful metrics results are excluded |
| Local council | No | 1/min | Search/details, access changes, invite/cancel; invitation acceptance and council registration are excluded |
| Joint landlord | No | 1/min | Owner-side invite/resend/cancel; accept/reject and new-user decisions are excluded |
| Letting agent | No | 1/min | Owner-side delegation/removal; agent login, password and certificate/update flows are excluded |
| Property offboarding | No | 1/min | Property deregistration only; leave/switch ownership and account deregistration are excluded |

The completed-property update and compliance families use disjoint slices of the exported fixtures.
Sequential update fixtures use indices 0–10, sequential certificates use 11–21, and ramped update and
compliance fixtures start at 22. Operator removals use targets disjoint from the correctness stage.
Fixture parsers fail before load starts if an export is too small or contains shared actors/targets.

The ramp retains zero-failed-request assertions and additionally requires every scheduled operator
to reach the checked outcomes and every completed-property actor to persist both certificates.
Independent workflow stages continue after failures; the aggregate still fails, followed by normal
database reset, app restart and QA verification. These newly wired paths require a fresh approved
NFT run before claiming remote mixed-load verification.

Certificate downloads accept `application/pdf` or S3's `application/octet-stream`, but must match the
uploaded PDF's exact SHA-256. Signed queries remain unchanged and external downloads carry no app
credentials. Authentication entry/refresh requests and council/operator mutable-page checks use unique
cache keys, retaining same-origin checks, explicit callbacks and unexpected-response failures.
Local contracts reproduce the shared-authorization and stale-role cache defects. They do not prove
the cause of every live redirect; the isolated simulator HTTP 403 from the last ramp remains unresolved.

For the remaining families, prepare and audit fixtures through the real HTTP journeys before measuring
them, reserve separate sequential/ramp allocations, and export tokens only after their prerequisite
stage succeeds. Keep notifications stubbed and scheduled writers paused, validate identity providers
and controlled dependencies before traffic, and retain aggregate failure plus QA recovery. Council
infrastructure and payment dependencies must not be bypassed to make a run appear complete.

### Remaining normal-load acceptance work

Select the agreed occupied-property happy path rather than the current vacant variant. Complete only
the selected organisation/team, existing-user joint acceptance and agent access/update paths, preparing
invitations through HTTP before exporting real tokens. Offboarding uses exclusive sole-owner property
deregistration fixtures, without manufacturing joint ownership. Check allocation disjointness
across families, not only within individual exports.

Verify council client/callback/signing configuration, controlled payment readiness and certificate
objects before any approved remote run. Move provisional think pauses to natural decision transitions,
excluding authentication. Keep the existing council search timeout as an observation requiring fresh
evidence, not a confirmed load-related regression.

Run the selected correctness checks and repeated, separately approved 5+15-minute calibration runs
with QA recovery. Agree mean/max/error acceptance limits from that evidence and workload-owner review
before claiming ADR 0035 acceptance. Exhaustive variant matrices and spike testing are not prerequisites
for this ticket. Earlier local matrix results are not validation of the narrowed workload.

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

The bootstrap adds `data-nft-performance.sql` after the normal local fixtures. Its five reserved addresses use
distinct `PRSDB Performance Square` labels: older dumps can contain QA addresses with the original `PRSDB Square`
labels, which would otherwise make selected-address submission ambiguous. The fixture upgrades only its own
reserved rows with matching UPRNs, leaving imported and QA addresses unchanged. A separately restored large
database with SQL initialisation disabled must have this fixture applied explicitly before running the suite.

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

The basic suite signs in as the seeded, unverified landlord for each profile-update pass. It updates name, date of birth, phone, email and address
(selected lookup followed by manual entry). Phone, email and manual-address values change on every warm-up and
measured pass; the address variants alternate so neither resubmits an unchanged address. Phone values use the
synthetic London range beginning `02079460123`; email addresses use `example.invalid`. Each submission checks the
saved profile value. Address HTTP contract tests also require the session, CSRF token, extra hidden fields and
journey query string, and reject unchanged saved values even when response statuses are correct.
Run with the local profile and do not
enable `use-notify`; local email notifications are handled by the stub service. In NFT the `one-login-simulator` profile (active only during
performance runs) also selects the stub service, so
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

The current slice covers seeded name, date-of-birth, phone, email and selected/manual address updates, and
individual registration through the manual-address branch. Identity updates require an unverified landlord.
It also follows authenticated links through the landlord dashboard, property records, an owned property's
details/compliance panels, compliance/outstanding actions and incomplete-property listing. Property headings
must match the selected record, and unexpected redirects fail rather than following back to login.
It does not cover other registration branches, council or letting-agent journeys. See ADRs 0035-0040
for the basic/load distinction, API-level measurement, runner, scheduling, Gatling and fixture-restoration decisions.

### Generating the large NFT dataset (PDJB-1748)

`NftDataSeeder` adds records alongside the existing small fixture. Explicit IDs start above each table's existing
maximum, and sequences are aligned without rewinding an already-ahead sequence. Generation requires exclusive
database access and a freshly reset fixture; it is not an idempotent startup script. The seeding runner must
enforce those preconditions rather than rerun the same random seed over a previously generated dataset.

Bulk property addresses must be active, have a UPRN and belong to an English local council. The five reserved
`EG1 2AA` lookup addresses remain outside bulk property allocation so the existing registration journey keeps
working. `NFT_SEED_MINIMUM_FREE_ADDRESSES` (default `0`) reserves additional address capacity for subsequent
registration journeys; choose it from the planned test volume rather than assuming the total address count
represents usable capacity. Generation fails and rolls back if there are not enough addresses or if the
configured landlords produce fewer property registrations than requested.

The property settings are **additional** records, excluding the small fixture. Completed and incomplete registrations
have separate exact targets:

| Environment variable              | Deployed default | Meaning                                                                  |
|-----------------------------------|-----------------:|--------------------------------------------------------------------------|
| `NFT_SEED_SYSTEM_OPERATORS`       |              150 | Additional system operators                                              |
| `NFT_SEED_LOCAL_COUNCIL_USERS`    |            3,000 | Additional local-council users                                           |
| `NFT_SEED_PROPERTIES`             |        2,450,000 | Completed property registrations                                         |
| `NFT_SEED_INCOMPLETE_PROPERTIES`  |          272,223 | Incomplete property-registration journeys                                |
| `NFT_SEED_LANDLORDS`              |                0 | Derive landlord count from the existing synthetic portfolio distribution |
| `NFT_SEED_BATCH_SIZE`             |           10,000 | Records processed per seeding batch                                      |
| `NFT_SEED_RANDOM_SEED`            |              239 | Random seed for reproducible generated data                              |
| `NFT_SEED_GENERATED_ADDRESSES`    |                0 | Generate no extra addresses; use the existing address dataset            |
| `NFT_SEED_MINIMUM_FREE_ADDRESSES` |                0 | Extra eligible addresses to leave unallocated                            |

A positive landlord count requests that many additional landlords instead; generation fails if their portfolios
cannot supply both property targets. Automatic mode includes landlords with no properties according to the existing
distribution and stops after the batch that meets both targets. It is a synthetic population, not a landlord demand
forecast. The final portfolio can be truncated to reach the exact property targets. Local defaults remain small:
30 landlords, 40 completed properties and 5 incomplete journeys.

#### Workload assumptions and arithmetic

The in-VPC worker downloads a pinned S3 object version. Its dedicated task role needs `s3:GetObjectVersion`
on `arn:aws:s3:::prsdb-seed-data-nft/seed-data.dump`, restricted to the approved version, and KMS-decrypt
permission. The Actions orchestrator needs scoped ECS launch/monitor/stop, PassRole and worker-log access.
The current proof of concept uses isolated temporary AWS resources. Permanent worker provisioning and permissions
in `communitiesuk/prsdb-infra` follow only after the GitHub-driven process is proven; they are not prerequisites
for this experiment.

The performance workflow resets the small fixture using migrations from its own commit, matching the deployed
application. Other reset callers retain their environment-branch default. Startup checks outside the runner's WAF
allow-list window verify completed ECS rollout and a healthy ALB target matching the running task; HTTP readiness
checks remain inside the existing preparation window.

The worker uses `pg_restore --jobs=4` against the custom-format dump directly from private ECS networking to RDS.
SSM carries migration and verification traffic only, never the bulk dump. Parallel workers can load independent
tables and build indexes concurrently but do not split a single table's `COPY` across workers. The database instance
and storage configuration stay unchanged so timings can be compared.

Logs include UTC start/completion/failure timestamps and elapsed seconds for download, archive inspection,
restore, Flyway migrations and dataset verification (including `ANALYZE`). Each active phase emits an elapsed-time
heartbeat every minute; this is not a percentage-complete estimate. Raw PostgreSQL output and credentials are
not streamed to the Actions log.

An interrupted restore can leave the preserved address/council tables without their post-data keys and indexes.
The normal small-fixture reset preserves these tables, so cancellation alone does not establish recovery.
Runner recovery must verify schema integrity before restarting the app; an unresolved failure blocks completion.

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
capacity check for newly generated data: it subtracts addresses allocated to both completed registrations and
incomplete journeys. Both types now draw from one forward-only, bounded address buffer, so JDBC batch flushes
cannot release incomplete addresses for reuse. Before appending data, a transaction-local snapshot excludes
addresses already selected by saved registrations. Generated street labels include a synthetic sequence suffix
to avoid ambiguous address selections when random street/postcode combinations repeat.
The seeder DAO uses transaction-local `force_custom_plan` so cached collision-check plans cannot retain the
tiny pre-seed table estimate as uncommitted batches grow. PostgreSQL restores the inherited planning mode on
both commit and rollback; ordinary application sessions and database-wide settings are unchanged. A focused
1.5M-row, 50,000-parameter benchmark reproduced a cached generic-plan timeout at five seconds versus approximately
0.2 seconds with custom planning. This isolates that query bottleneck, not the total full-rebuild duration.

**Existing dump limitation:** the earlier proof validated restore/recovery and the original basic journeys, not
incomplete-address integrity. A subsequent local audit found repeated incomplete selections, overlap with active
completed properties, and duplicate synthetic address labels. The corrected seeder does not repair an existing
dump. Rebuild and independently validate the saved registrations before using that dump for resume/delete
journeys or making a full-day capacity claim; row counts alone are insufficient.

The corrected isolated local rebuild uses a fresh migrated QA fixture, `NFT_SEED_GENERATED_ADDRESSES=3000000`
and `NFT_SEED_MINIMUM_FREE_ADDRESSES=12537`, with the full completed/incomplete targets above. It leaves
277,777 independently verified free addresses. The original archive is retained unchanged; the replacement
has been exported and restored into a separate local database, but has not been uploaded to S3.
Do not reuse the earlier generated-address calculation based on the defective archive's allocations.
The final local candidate, `prsdb-performance-seed-corrected-qa.dump`, also includes complete street/town
components for the synthetic QA address fallbacks, so record headings retain the full address.
Its SHA256 is `f55efa64e33b168969fdb887996a2ef38db3340ff5f2826f92d420d8a82b432e`.
A fresh four-worker restore passed both allocation/schema and all-26-QA-record audits. The expanded local
baseline passed 913/913 requests over ten measured passes plus warm-up, in 27 seconds. This is a sequential
correctness check, not an NFT timing, concurrency, average-use or spike result; broader journey coverage remains unfinished.

The current suite updates name, date of birth, phone, email and selected/manual address on the fixed synthetic landlord subject
`urn:fdc:gov.uk:2022:mGHDySEVfCsvfvc6lVWf6Qt9Dv0ZxPQWKoEzcjnBlUo`. Dump preparation belongs to the seeding
runner: its fixture contract requires `src/main/resources/data-nft-performance.sql` before bulk allocation for
the five reserved high-ID `EG1 2AA` lookup addresses, and `src/main/resources/data-nft-performance-landlord.sql`
after bulk seeding to assign this subject to an unlinked, unverified synthetic individual landlord if it is absent. The landlord script is
dump preparation only, not startup data initialisation; the address fixture also supports databases with no
landlords. The earlier restore proof of concept used the existing dump, without regenerating it.
New-registration journeys generate
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
in one run; do not reset between suites. The performance workflow currently runs the basic and saved-registration suites, with separate
bounded restore, compilation, test and recovery jobs. It does not yet include load/spike simulations.
A full eight-hour capacity is the agreed dataset sizing model, not a claim that the current workflow can
run for eight hours. Any future scheduling and suite orchestration must account for the selected test duration.

After the run, including a failed run, the existing cleanup resets NFT to the small day-to-day fixture and starts
the app. It does not modify or replace the large S3 dump. A subsequent run restores the large dump again before
testing. The loader verifies at least the configured completed and incomplete registration counts before the
performance workflow continues. The shared read-only `data-nft-seed-verify.sql` audit also rejects duplicate or
ambiguous incomplete selections, overlap with completed ownership, missing cached address metadata, mismatched
subjects, invalid schema objects and missing search indexes. Free capacity excludes both completed and incomplete
allocations; the old defective dump now fails validation rather than being silently repaired during restore.
The defaults match the deployed seeder targets, and can be overridden with GitHub
Environment variables `NFT_SEED_PROPERTIES` and `NFT_SEED_INCOMPLETE_PROPERTIES`. The minimum eligible free-address
check is independently configurable with GitHub Environment variable `NFT_LOAD_MINIMUM_FREE_ADDRESSES` (default
`1`); the reusable full-day dump is sized separately with its 12,491-address reserve. These loader checks do not
change the dataset or the seeder's own configuration.

### NFT basic performance workflow (PDJB-430)

`.github/workflows/run-performance-tests.yml` is dispatched on the `nft` branch. It first checks whether the NFT
head differs from the last successful run; if unchanged, it skips all AWS and database operations unless `force`
is selected. The `restore-task-definition` input identifies an exact worker revision, never `latest`; scheduled runs
use the NFT Environment variable `NFT_RESTORE_TASK_DEFINITION`. Neither has a default worker.
The original temporary proof-of-concept worker has been retired. Do not dispatch another run until an approved
replacement worker and its scoped permissions are provisioned and its exact active ARN is configured.
The deployed app image must carry the workflow's NFT SHA, checked before shutdown. Before testing, the
workflow waits for actual app tasks to reach `STOPPED`: service `runningCount=0` is insufficient while ALB draining
keeps containers and database connections alive. The dedicated worker validates the pinned dump version, size,
checksum and archive, checks for database clients, then restores `s3://prsdb-seed-data-nft/seed-data.dump` directly
to RDS. Actions migrates it to the deployed NFT revision and verifies required data through SSM. It starts the app and runs
`BasicJourneysSimulation` followed by `SavedRegistrationSimulation` in baseline mode after the One Login simulator is prepared. Additional
load/spike/performance suites should run against this same restored dataset before cleanup; do not reset between
suites in a combined run. Gatling compilation runs in parallel with restore and transfers the compiled classes/resources
to the test runner, which skips recompilation. Restore has a 45-minute budget, compilation 20 minutes, and
simulator preparation plus Gatling 35 minutes. Preparation and HTTP tests deliberately share one runner because
WAF and simulator access belong to that runner's IPv4 address. Each AWS job obtains fresh credentials.
The restore job uploads its own recovery state and a pre-restore checkpoint; the performance job uploads
Gatling reports and its separate preparation state. A separate recovery job
restores the original app/simulator state and its own WAF/security-group changes.

After the performance job completes, recovery stops the owned restore task, restores simulator configuration/access
and waits for the actual app tasks to stop. Reset is blocked if this cannot be verified or the latest active app
revision differs from the recorded baseline. Otherwise the shared database reset runs even when loading,
preparation or Gatling failed. This is the final cleanup: it cleans and migrates the NFT schema, then returns
NFT to the small `data-test.sql` fixture for day-to-day use. It does
not reload the large dump. The `Reset Database - NFT` workflow has the same small-fixture semantics and
shares the `nft-app-service-operation` concurrency lock with performance runs and webapp NFT deployments. After
the reset succeeds, the workflow ensures the one-task app is running and healthy, then checks the QA fixtures
read-only through SSM before recording success.

Health does not prove complete QA fixture initialization. The standard reset preserves address/council tables.
`data-test.sql` now supplies missing address dependencies as synthetic, NULL-UPRN fallbacks without replacing
preserved addresses. Its normal address sequence advances past fallback IDs without rewinding an imported
sequence or jumping into the reserved high-ID range. The runner checks all 26 exact property IDs and their
expected addresses, registration numbers and landlord links, both organisation landlords, successful migration
history, and absence of leftover performance properties/incomplete registrations. A partial reset (including
the previously observed nine-property reset) fails the workflow and cannot advance the success marker.
These changes are locally verified; another approved GitHub/NFT run must establish deployed end-to-end evidence.

The large baseline is created separately, normally once, by the existing NFT ECS seed-and-dump task. It may be
deliberately regenerated in the future, but normal performance runs only read it; neither loading nor the final
small-fixture reset writes or deletes the S3 object. A run that needs a replacement baseline must use the
separately approved seed-and-dump operation.

The `fail-smoke` input injects a failure after simulator setup but before Gatling. The separate
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

Execution and recovery belong to the GitHub runners. Their logs and artifacts retain the original app revision,
owned worker ARN, simulator state, access changes, restore stages, Gatling report and reset outcome.
The `fail-smoke` case never starts mutating journeys; `fail-gatling` exercises failure after Gatling starts.

**Interrupted-run limitation:** ordinary failures trigger bounded cleanup, but force-cancellation or runner
loss can prevent recovery jobs or artifact upload. Missing state, unknown tasks and unverified restoration
block automatic reset. Fully automatic recovery from those conditions is not yet proven by this experiment;
removing manual commands does not remove that limitation.

### Prerequisites for a local run

1. For the real basic suite, start the app using `scripts/performance-tests/start-local.mjs` as described above.
   The standard `local,local-no-auth` run configuration does not exercise One Login authentication.
2. Confirm the app is up and note the port from your `.env`.

`RegisterAsLandlordGetSimulation` targets an existing public page and needs nothing else.

`FormSubmissionPostSimulation` targets the POST endpoint added for performance testing, which is gated behind
the `pdjb-238-gatling-post-endpoint` feature flag. That flag is enabled only in the NFT environment, so to run
this simulation locally you must enable it first, by either:

- setting `enabled: true` for that flag in `src/main/resources/application-local.yml`, then restarting the app (remember to revert the
  change before committing); or
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

The console output prints the exact path at the end of each run. These reports are build output and are **not** committed — the repository's
`.gitignore` excludes `build/`. In CI, publish the report directory as a
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
val journey = exec(
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
