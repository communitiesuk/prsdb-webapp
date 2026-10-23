# Update gas safety journey map report

Generated 2026-10-23T09:36:28.568507300Z at commit `3a5832daa`. Feature flags: failover-test-endpoints=false, subject-identifier-page=true, pdjb-939-property-registration-phase-two=false, pdjb-1053-dashboard-nav-link=true, pdjb-1022-delegate-to-letting-agent=true, pdjb-1664-password-benchmark-endpoint=false, pdjb-238-gatling-post-endpoint=false, pdjb-1040-correspondence-address=true, pdjb-1009-payments=false, pdjb-926-landlord-dashboard-update=true, pdjb-1210-multi-user-organisations=false.

| Pages | Variants | Edges | Gaps | Errors |
|-------|----------|-------|------|--------|
| 14 | 26 | 30 | 0 | 0 |

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
