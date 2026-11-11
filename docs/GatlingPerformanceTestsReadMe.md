# Performance tests

The supported NFT workflow runs basic landlord journeys, saved-registration resume and deletion,
completed-property updates, organisation profile updates and certificate correctness checks before a
four-family concurrent ramp. The landlord registration journey is a proxy; it does not measure paid
property-registration capacity.

## Prerequisites

Use the seeder and supported Gatling changes together with this orchestration. The NFT environment needs
the seeded database dump, its pinned S3 version and checksum, an exact active VPC restore task revision,
the restore worker's S3/KMS access and the workflow runner's ECS, SSM and restore permissions.
The runner also needs `ecs:DescribeServices` and `ecs:UpdateService` on the NFT async-worker
service, plus the scoped task-list and task-describe permissions used to verify its shutdown.
Configure `NFT_RESTORE_TASK_DEFINITION` as an NFT environment variable or pass the exact revision to the
workflow. The restore worker task definition must contain the pinned dump inputs and verification settings.
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

Dispatch **Run Performance Tests** from `nft`. It checks for changes, compiles Gatling, stops actual
writers, restores the pinned dump in the VPC, migrates and verifies it, starts the app and prepares
the simulator. It exports bounded, distinct saved-registration owners, sole-property owners and
organisation administrators through a read-only tunnel. Correctness stages precede the ramp; the ramp
uses exclusive actors, two-second pauses, a five-minute ramp and a fifteen-minute hold. It requires
zero request failures and exact journey completions.

The workflow records non-secret ownership and recovery metadata for the exact run, workers, services
and simulator state. It records the async service's original task definition and desired count before
stopping it, verifies its recorded tasks have stopped, and keeps it stopped through the QA reset.
Cleanup independently attempts worker and readiness recovery, stops actual
writers before the QA reset and fails closed if required recovery state is missing. It then resets QA,
restarts the app and verifies the required QA properties and links. A separate recovery job restores
the recorded async desired count after a successful reset even when app recovery or QA verification
fails. If reset fails or ownership state is unavailable, the async worker remains stopped for manual
reconciliation to avoid writing to a potentially incomplete database. Preserve recovery artifacts and
investigate a failed or missing-state cleanup before retrying; do not fabricate state. The manual
**Reset Database - NFT** workflow can instead verify the existing QA baseline with `verify-only`.
No unattended restoration of modified environment settings is claimed.

Large datasets can require additional app startup time; ensure the environment's startup readiness
configuration is appropriate before an approved run. Run
`./gradlew performanceTest -x buildFrontendAssets --console=plain` for the supported Gatling contracts.

## Limitations and evidence

Run 43063425738 passed correctness, but the ramp had two failed simulator OAuth authorizations out of
31,257 requests: WAF `SQLi_QUERYARGUMENTS` falsely matched nonce/state values. All QA recovery jobs
passed. This is not a successful load acceptance result, an agreed latency SLO, or evidence of paid
property-registration capacity. Council, agent, joint, operator, organisation-team and offboarding
journeys are not part of this supported baseline.
