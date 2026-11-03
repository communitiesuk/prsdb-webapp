# Update electrical safety journey map report

Generated 2026-11-03T12:29:05.004354100Z at commit `f960f1073`. Feature flags: failover-test-endpoints=false, subject-identifier-page=true, pdjb-939-property-registration-phase-two=true, pdjb-939-property-registration-phase-three=false, pdjb-1664-password-benchmark-endpoint=false, pdjb-238-gatling-post-endpoint=false, pdjb-1009-payments=true, pdjb-1210-multi-user-organisations=true, pdjb-1894-gas-certificate-14-month-expiry=false, pdjb-955-associated-parties=false, pdjb-1666-eic-issue-date=false, pdjb-973-asynchronous-email-sending=false.

| Pages | Variants | Edges | Gaps | Errors |
|-------|----------|-------|------|--------|
| 22 | 37 | 51 | 0 | 0 |

## Gaps

None.

## Errors

None.

## Routing code

Every arrow the journey's routing code defines between pages and decisions, for every outcome.

| Arrows | Walked | Not walked | Depend on earlier answers |
|--------|--------|------------|---------------------------|
| 25 | 20 | 5 | 0 |

### Arrows not walked

| From | Outcome | To |
|------|---------|----|
| has-electrical-safety | PROVIDE_THIS_LATER | provide-electrical-safety-certificate-later |
| provide-electrical-safety-certificate-later | COMPLETE | check-electrical-safety-answers |
| internal:HasAnyInCollectionStep@task:ElectricalSafetyDetailsTask | SOME_MEMBERS | check-eic-uploads |
| internal:HasAnyInCollectionStep@task:ElectricalSafetyDetailsTask#2 | NO_MEMBERS | eicr-file-upload |
| internal:HasAnyInCollectionStep@task:ElectricalSafetyDetailsTask#2 | SOME_MEMBERS | check-eicr-uploads |

## Decisions and the answers they read

| Decision | Steps whose answers it reads | Reads every answer |
|----------|------------------------------|--------------------|
| remove-eic-upload | eic-file-upload | no |
| remove-eicr-upload | eicr-file-upload | no |
