# NFT performance recovery checkpoints

The performance workflow keeps recovery state in the private NFT seed bucket under
`performance-recovery/<run-id>/<attempt>/`. Recovery objects are retained for seven days;
they are not GitHub artifacts. Public workflow output is limited to the workflow summary,
compiled Gatling classes and the successful revision marker.

Gatling launches use `nft-gatling.mjs` to capture raw stdout/stderr in mode-0600
runner-temporary files. Public progress contains only fixed stage names and numeric
elapsed/exit values. An always-run step refreshes the orchestrator role and uploads
those raw logs to this same private recovery prefix; upload failure fails the job.
Raw diagnostics are never included in public workflow artifacts.

## Scan-result worker lifecycle gate

The existing restore chain records the exact original async task revision and desired
count, including zero, and refuses an inactive baseline revision. The worker is stopped
and its actual tasks must be `STOPPED` during database replacement. A nonempty or
unverified scan-result queue blocks replacement; the scripts never purge, receive or
delete queue messages themselves.

Before any correctness/upload/ramp stage, `start-async` clones the original worker task,
not the web task. It retains the worker roles and NFT destinations, uses
`web-server-deactivated,scan-result-worker,nft`, and pins the exact running candidate
app's image digest. It does not activate email consumption or a web server. The gate
checks the configured queue identity, ECS rollout, actual worker task and image digest.
These checks do **not** prove that Spring's listener or the real scanning path works:
an approved live upload canary remains mandatory.

Candidate ownership, including partial registration/switch failures, is stored in the
private `worker-state` checkpoint using the existing recovery selector. A deterministic
run-owned task family permits reconciliation after a lost response/checkpoint; ambiguous
identity blocks automatic recovery. Cleanup waits for queued/in-flight work, stops actual
candidate tasks, restores the baseline task revision at zero, and retires only the
run-owned candidate after NFT service/deployment references are gone. Unexpected service
references or draining tasks block reset. Success, failed correctness stages and abandonment
use this same cleanup/reset/recovery chain; retry failed recovery jobs rather than starting
another workload.

Before any maintenance schedule or writer is changed, the workflow discovers the latest
`ACTIVE` revision of the managed `prsdb-nft-vpc-restore-performance` family, validates its
restore container and immutable dump metadata, and checkpoints the exact revision and dump
identity. The optional `restore-task-definition` input is an exceptional explicit override;
normal runs do not depend on a GitHub restore-revision variable.

The normal compatibility path requires the workflow/reset SHA to match the deployed app
image and verifies that the app and async worker task definitions and running image digests
match. The restored database is migrated and the restarted app must pass QA before the
baseline worker can be restarted. If an explicit `expected-app-sha` override differs from
the workflow/reset SHA, the existing compatibility secret and variable remain an exceptional
manual approval path; they are not required for normal deployment-matched runs. Any missing
or mismatched compatibility evidence fails closed before destructive work, and failed app
migration or QA leaves the worker stopped. Never resume an old image against an unverified
schema.

The NFT-only orchestrator policy extension must be applied separately with approval.
No new service is provisioned. The existing NFT cluster/service names, exclusive test
window, absence of unowned/incoming work, queue/DLQ health and actual listener readiness
must be checked live. Approximate queue counts and local mocks are not AWS runtime proof.
No access-rule, workload schedule or other environment defaults are changed by this gate.

## Retry recovery through GitHub Actions

For a failure in one of the recovery jobs, rerun that failed recovery job from the
workflow run. GitHub increments the attempt number; the existing recovery CLI retrieves
checkpoints for the same run and selects the latest valid state. Do not retry the dataset
restore or performance workload in the same run; start a fresh workflow run for those.
From a terminal, the failed recovery jobs can be retried with:

```sh
gh run rerun <run-id> --failed
```

The recovery jobs assume the existing NFT performance workflow role, retrieve checkpoints
for that run, select the newest valid checkpoint using `nft-recovery-state.mjs`, then resume
the existing restore chain. A retry increments the attempt number; the selector considers
only that run and attempts up to the current attempt.

## Retrieve checkpoints with an approved NFT profile

For private inspection, use an approved NFT operator profile that already has read access
to the recovery prefix and its KMS-encrypted objects. Replace the run and attempt values
with the digits shown in GitHub Actions, use a private local directory, then run the same
recovery CLI:

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

The selected state is written only under `RUNNER_TEMP`; do not attach it to issues, pull
requests, workflow artifacts or public logs. The workflow role remains GitHub-OIDC-only;
direct profile access must use an already-approved operator profile and is not granted by
the workflow role policy.
