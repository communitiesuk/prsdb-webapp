# Update gas safety journey map report

Generated 2026-11-03T12:31:06.289816100Z at commit `f960f1073`. Feature flags: failover-test-endpoints=false, subject-identifier-page=true, pdjb-939-property-registration-phase-two=true, pdjb-939-property-registration-phase-three=false, pdjb-1664-password-benchmark-endpoint=false, pdjb-238-gatling-post-endpoint=false, pdjb-1009-payments=true, pdjb-1210-multi-user-organisations=true, pdjb-1894-gas-certificate-14-month-expiry=false, pdjb-955-associated-parties=false, pdjb-1666-eic-issue-date=false, pdjb-973-asynchronous-email-sending=false.

| Pages | Variants | Edges | Gaps | Errors |
|-------|----------|-------|------|--------|
| 13 | 26 | 29 | 0 | 0 |

## Gaps

None.

## Errors

None.

## Routing code

Every arrow the journey's routing code defines between pages and decisions, for every outcome.

| Arrows | Walked | Not walked | Depend on earlier answers |
|--------|--------|------------|---------------------------|
| 17 | 14 | 3 | 0 |

### Arrows not walked

| From | Outcome | To |
|------|---------|----|
| has-gas-supply | PROVIDE_LATER | provide-gas-safety-certificate-later |
| provide-gas-safety-certificate-later | COMPLETE | check-gas-safety-answers |
| internal:HasAnyInCollectionStep@task:GasSafetyDetailsTask | SOME_MEMBERS | check-gas-safety-certificate-uploads |

## Decisions and the answers they read

| Decision | Steps whose answers it reads | Reads every answer |
|----------|------------------------------|--------------------|
| remove-gas-safety-certificate-upload | gas-safety-certificate-file-upload | no |
