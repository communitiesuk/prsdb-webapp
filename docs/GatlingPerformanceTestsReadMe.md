# Performance tests

The NFT workflow runs basic landlord journeys, saved-registration resume/deletion, completed-property
and organisation updates, gas/electrical certificates, organisation registration/team operations,
joint invitations/acceptance, agent access/property updates, operator operations and property
deregistration. Ten families run concurrently; preparation and sequential checks also exercise the
restored journeys. Landlord registration is a proxy, not paid property-registration capacity.

## Merge and release order

The changes are split for focused review: seeder/data generation (#2259), Gatling journeys/assertions
(#2260), fixtures/workflow/recovery (#2261), and infrastructure (communitiesuk/prsdb-infra#395).
Merge #2259, then #2260, then #2261; update/retarget each child onto main and rerun checks after its
parent merges. Infra can merge independently, but the approved NFT task-definition-state plan,
apply/configuration of the managed worker revision and webapp release are required before execution.
Merging alone does not deploy or establish load acceptance.

## Prerequisites

Use the seeder and supported Gatling changes together with this orchestration. The NFT environment needs
the seeded database dump, its pinned S3 version and checksum, an exact active VPC restore task revision,
the restore worker's S3/KMS access and the workflow runner's ECS, SSM and restore permissions.
The runner also needs `ecs:DescribeServices` and `ecs:UpdateService` on the NFT async-worker
service, plus the scoped task-list and task-describe permissions used to verify its shutdown.
By default, the workflow discovers the highest-numbered active managed restore task revision.
The optional `restore-task-definition` input selects an exact revision instead. The workflow does not
use the `NFT_RESTORE_TASK_DEFINITION` environment variable. The restore worker task definition must
contain the pinned dump inputs and verification settings.
Certificate metadata in a database dump does not contain file bodies: the matching synthetic objects
must be available in the app's storage for property and certificate checks.

For local development use Docker, Node 22+, the JDK required by the build and the normal `.env`.
Start the local dependencies and the official One Login simulator:

```bash
docker compose -f docker-compose.local.yml up -d --wait
docker run --rm --name prsdb-performance-simulator \
  -p 127.0.0.1:13000:3000 -e INTERACTIVE_MODE=true \
  ghcr.io/govuk-one-login/simulator@sha256:0d5e62c1db1c400c4881be2270b3f08aeb55c72ca3d9eb9a6e5196becef6f5e5
```

In another terminal start the local app and simulator bootstrap:

```bash
node --env-file=.env scripts/performance-tests/start-local.mjs
```

The bootstrap uses `SERVER_PORT` and `ONE_LOGIN_SIMULATOR_PORT` if set, and creates disposable signing
keys. It does not reset the database. Only on a disposable local database, stop the app and use
`FLYWAY_CLEAN_DISABLED=false ./gradlew flywayClean --console=plain` before starting it if a fresh schema
is needed. The bootstrap loads the local performance address fixture.

For local paid-registration variants, start the bootstrap with
`node --env-file=.env scripts/performance-tests/start-local.mjs --performance-dependencies` instead.
This adds the `performance-test` profile and uses loopback synthetic payment and EPC services; it
does not need staging EPC credentials. The ordinary bootstrap keeps its existing profiles.

The paid fixture SQL skips the first 3,000 landlords, so use a disposable locally seeded database
with enough eligible landlords; the small default local seed is insufficient. With `PGHOST`,
`PGPORT`, `PGDATABASE`, `PGUSER` and authentication already configured for that database, export
the fixtures read-only and create the synthetic PDF outside the checkout:

```bash
mkdir -p "$HOME/prsdb-performance-local"
PGOPTIONS="-c default_transaction_read_only=on" psql -X -qAt \
  -f src/main/resources/data-nft-paid-registration-fixtures.sql \
  -o "$HOME/prsdb-performance-local/paid-fixtures.json"
node scripts/performance-tests/local-registration-certificate.mjs "$HOME/prsdb-performance-local/certificate.pdf"
```

Run one warm-up and one measured vacant registration with distinct exported subjects:

```bash
./gradlew gatlingRun --non-interactive \
  --simulation uk.gov.communities.prsdb.webapp.performance.RegistrationSimulation \
  -Dgatling.target=local -Dgatling.basic.mode=baseline -Dgatling.basic.iterations=1 \
  -Dgatling.baseUrl=http://localhost:8080 \
  -Dgatling.simulatorUrl=http://127.0.0.1:13000 \
  -Dgatling.basic.landlordSubject=urn:fdc:gov.uk:2022:UVWXY \
  -Dgatling.registration.variants=VACANT_MANUAL_EXEMPT \
  -Dgatling.registration.fixturePath="$HOME/prsdb-performance-local/paid-fixtures.json" \
  -Dgatling.registration.certificatePath="$HOME/prsdb-performance-local/certificate.pdf"
```

Each selected variant needs one distinct fixture per warm-up and measured iteration. Re-export
from an appropriate fresh disposable seed before repeating a run. Upload variants require the PDF;
it is synthetic and not valid for real compliance use.

Run the basic journeys with a fresh registration subject prefix for each run:

```bash
./gradlew gatlingRun --non-interactive \
  --simulation uk.gov.communities.prsdb.webapp.performance.BasicJourneysSimulation \
  -Dgatling.target=local -Dgatling.basic.mode=baseline \
  -Dgatling.baseUrl=http://localhost:8080 \
  -Dgatling.simulatorUrl=http://127.0.0.1:13000 \
  -Dgatling.basic.landlordSubject=urn:fdc:gov.uk:2022:UVWXY \
  -Dgatling.basic.registrationSubject=urn:fdc:gov.uk:2026:PERF-REGISTRATION-001
```

For locally restored seeded property fixtures, export the matching SQL through a read-only database
connection, then prepare synthetic file bodies in an existing isolated storage root outside the
repository and its ancestors:

```bash
node scripts/performance-tests/local-seed-files.mjs property-fixtures.json /absolute/path/isolated-storage-root
node scripts/performance-tests/local-registration-certificate.mjs synthetic-certificate.pdf
```

Use the isolated directory as the app working directory, with an absolute jar path and a `src` link
to the checkout. The fixture helper refuses unsafe paths, symlinks, conflicting objects and non-seed
keys. The generated certificate is synthetic and not valid for any real compliance purpose.

## NFT workflow and recovery

When a new baseline is needed (for example, no suitable baseline for an initial run, changed fixtures
or certificate handling, or schema changes incompatible with the existing baseline), deploy compatible
app/seeder/dump and NFT infrastructure prerequisites, then run
[**Generate NFT seed dump**](https://github.com/communitiesuk/prsdb-infra/actions/workflows/generate-nft-seed.yml)
from infra `main`. Verify and select the matching manifest-bound restore baseline before running
performance tests; the Action does not switch it automatically. A suitable existing baseline can be
reused with compatible migrations, so not every first run or schema change requires regeneration.
Preserve the old usable baseline until the replacement is verified.

Dispatch **Run Performance Tests** from `nft`. It checks for changes, compiles Gatling, stops actual
writers, restores the pinned dump in the VPC, migrates and verifies it, starts the app and prepares
the simulator. It exports bounded, distinct saved-registration owners, sole-property owners and
organisation administrators through a read-only tunnel. It prepares invitations through the application
and re-exports matching tokens before their consumers run. Basic/property/profile/certificate checks
precede the ramp; additional restored sequential checks follow it. The ramp uses exclusive actors,
properties and organisations, two-second pauses, a five-minute ramp and a fifteen-minute hold.
It requires zero request failures and exact journey completions. Insufficient or overlapping fixture
pools fail closed; randomized seed eligibility is not assumed to guarantee capacity.

The workflow records ownership and recovery metadata for the exact run, workers, services
and simulator state. It records the async service's original task definition and desired count before
stopping it, verifies its recorded tasks have stopped, and keeps it stopped through the QA reset.
Cleanup independently attempts worker and readiness recovery, stops actual
writers before the QA reset and fails closed if required recovery state is missing. It then resets QA,
restarts the app and verifies the required QA properties and links. A separate recovery job restores
the recorded async revision and desired count only after the reset, app recovery and QA verification
succeed. If they fail or ownership state is unavailable, the async worker remains stopped for manual
reconciliation to avoid writing to a potentially incomplete database. Preserve private recovery
checkpoints and investigate a failed or missing-state cleanup before retrying; do not fabricate state. The manual
**Reset Database - NFT** workflow can instead verify the existing QA baseline with `verify-only`.
No unattended restoration of modified environment settings is claimed.

Recovery checkpoints are stored in the private NFT seed bucket under
`performance-recovery/<run-id>/<attempt>/` for seven days, not in GitHub artifacts. Gatling stdout,
stderr and the report archive are also uploaded privately. Public output contains only bounded
progress and workflow summaries; the workflow summary provides the authorised operator's command to
download the private report. Do not attach checkpoints, raw logs or reports to public issues, pull
requests or artifacts. Raw Gatling logs are captured in mode-0600 runner-temporary files; private
upload failure fails the job.

### Scan-result worker gate

The workflow records the original async task revision and desired count, including zero, and requires
an active baseline revision. It stops actual worker tasks during database replacement. A nonempty or
unverified scan-result queue blocks replacement; the scripts do not purge, receive or delete messages.

Before correctness and load stages, `start-async` clones the original worker task, retaining its roles
and NFT destinations, enables only the scan-result worker profile and pins the running candidate app's
image digest. It does not activate the web server or email consumer. The gate checks queue identity,
ECS rollout and the actual worker task and image. These checks do not prove the real listener or
scanning path works: an approved live upload canary remains necessary.

Candidate ownership and partial failures are retained in the private worker checkpoint. Cleanup drains
and stops candidate tasks, restores the baseline revision at zero, and retires only an unreferenced
run-owned candidate. Ambiguous ownership, unexpected service references or draining tasks block reset.
The normal compatibility path requires the workflow/reset SHA to match the deployed app image and
checks the app and worker images. An explicit differing `expected-app-sha` requires the compatibility
task-definition variable (or legacy secret) and app-SHA variable; missing or mismatched evidence
fails closed. Never restart an old image against an unverified schema.

Before schedules or writers change, the workflow discovers or validates and checkpoints the exact active
managed restore task revision and immutable dump identity. Its environment must declare `NFT_DUMP_VERSION`,
`NFT_DUMP_BYTES` and `NFT_DUMP_SHA256` independently of `NFT_CERTIFICATE_KEY`,
`NFT_CERTIFICATE_VERSION`, `NFT_CERTIFICATE_DUMP_VERSION`, `NFT_CERTIFICATE_DUMP_BYTES` and
`NFT_CERTIFICATE_DUMP_SHA256`. The three certificate-bound dump fields must exactly match the restore
dump fields. The certificate key must be `seeding/certificate_template.png` or
`seeding/certificate-references/<positive-run-id>-<positive-attempt>.png`. The workflow checks the
exact certificate version in `prsdb-uploaded-files-nft` before schedule changes and again before
launch; unavailable or mismatched identities stop the run before schedules or writers are changed.
This preflight also gates the first live run when the selected revision uses the legacy shared
certificate key: the exact pinned version must still exist and its published dump binding must match.
There is no fallback to another certificate version. If the check fails, an authorised operator must
publish a matching seed manifest and update the managed restore revision before retrying. Local tests
exercise the fail-closed check but cannot verify the currently deployed AWS task definition or object.

The seed generator copies the versioned template to a run-owned key and publishes a private,
versioned manifest binding that copy to the exact dump upload. Deploy an application version that
accepts the per-run key before deploying or running the new seed task. For a new baseline, an
authorised operator must retrieve the exact manifest version recorded in that seed task's private
dump log, use its dump and certificate fields together to update the managed restore task and its
scoped policy, and deploy those changes separately. Do not select the latest dump or manifest by key.
The existing baseline uses a historically verified legacy certificate identity; local tests do not
establish that version's current availability.

The 700-day retention period starts when a certificate version becomes noncurrent, not when its dump
is restored. If the pinned version has expired or is inaccessible, publish a regenerated seed with
matching metadata; do not substitute the latest object. The
NFT-only orchestrator policy extension requires separate approval and application; local mocks do not
prove live queue, listener or access readiness.
An approved operator must check the exclusive test window, absence of incoming or unowned work, and
queue/DLQ health before a live run.

### Retry or inspect recovery

Rerun a failed recovery job in the same workflow run, or use `gh run rerun <run-id> --failed`.
The recovery selector retrieves that run's checkpoints and chooses the latest valid attempt. Do not
retry the workload or dataset restore in the same run: start a fresh run for those.

For private inspection, use an already-approved NFT operator profile with access to the recovery
prefix and its KMS key. Substitute the run and attempt values from GitHub Actions:

```sh
export AWS_PROFILE='<approved NFT profile>'
export AWS_REGION=eu-west-2
export GITHUB_RUN_ID=123456
export GITHUB_RUN_ATTEMPT=2
export RUNNER_TEMP="$HOME/.cache/prsdb-nft-recovery-$GITHUB_RUN_ID-$GITHUB_RUN_ATTEMPT"
mkdir -p -m 700 "$RUNNER_TEMP"
node scripts/performance-tests/nft-recovery-state.mjs download
node scripts/performance-tests/nft-recovery-state.mjs select
```

The selected state remains under `RUNNER_TEMP`. The workflow role is GitHub-OIDC-only; it does not
grant direct operator-profile access.

Large datasets can require additional app startup time; ensure the environment's startup readiness
configuration is appropriate before an approved run. Run
`./gradlew performanceTest --console=plain` for the Gatling contracts.

## Limitations and evidence

The concurrent mix retains the landlord-registration proxy at 26/minute and saved, completed-property
and organisation-profile updates at 4/minute each. Six additional families run at provisional 1/minute:
gas/electrical compliance, organisation registration/team, joint acceptance, agent access/update,
operator and deregistration. Organisation registration/team share one arrival. This is 44 arrivals/minute
at hold and 767 starts overall; the added rates are assumptions, not measured production traffic.
The 26/minute planning forecast concerns paid property registrations, which the proxy does not prove.

Previously successful **local real-application Gatling runs** recorded:

| Journey | Successful requests |
|---|---:|
| Organisation registration / team invitation / acceptance / access change | 572 / 187 / 253 / 99 |
| Joint invitation operations / acceptance | 264 / 156 |
| Agent preparation / access and updates | 360 / 264 |
| Operator operations | 300 |
| Property deregistration | 156 |

These are historical sequential results, not expanded concurrent or NFT acceptance. Fresh fixture/HTTP
contracts validate restored code; a subsequent approved NFT run must validate the combined workload.
Joint rejection and existing-password agent variants are not claimed: preserved simulations support
acceptance and unclaimed invitations. Council execution, paid property registration, the failing public
EPC readiness path and EPC-dependent scenarios remain excluded. Gas/electrical checks remain included.

NFT run 43063425738 passed its then-supported correctness checks and QA recovery, but two of 31,257
ramp requests failed at simulator OAuth authorization because WAF `SQLi_QUERYARGUMENTS` matched nonce/state.
It did not run the restored extended families. Earlier expanded NFT attempts stopped in preparation;
skipped journeys are not demonstrated failures. Public EPC readiness failure does not establish an
internal loopback EPC failure; the payment roundtrip was skipped rather than proven failing.
No agreed latency SLO, successful combined NFT acceptance or paid-registration capacity is claimed.
