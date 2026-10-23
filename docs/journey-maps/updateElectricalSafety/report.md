# Update electrical safety journey map report

Generated 2026-10-23T09:34:19.894367100Z at commit `3a5832daa`. Feature flags: failover-test-endpoints=false, subject-identifier-page=true, pdjb-939-property-registration-phase-two=false, pdjb-1053-dashboard-nav-link=true, pdjb-1022-delegate-to-letting-agent=true, pdjb-1664-password-benchmark-endpoint=false, pdjb-238-gatling-post-endpoint=false, pdjb-1040-correspondence-address=true, pdjb-1009-payments=false, pdjb-926-landlord-dashboard-update=true, pdjb-1210-multi-user-organisations=false.

| Pages | Variants | Edges | Gaps | Errors |
|-------|----------|-------|------|--------|
| 15 | 27 | 30 | 0 | 0 |

## Gaps

None.

## Errors

None.

## Routing code

Every arrow the journey's routing code defines between pages and decisions, for every outcome.

| Arrows | Walked | Not walked | Depend on earlier answers |
|--------|--------|------------|---------------------------|
| 16 | 13 | 3 | 0 |

### Arrows not walked

| From | Outcome | To |
|------|---------|----|
| has-electrical-safety | PROVIDE_THIS_LATER | provide-electrical-safety-certificate-later |
| provide-electrical-safety-certificate-later | COMPLETE | check-electrical-safety-answers |
| internal:HasAnyInCollectionStep@task:ElectricalSafetyDetailsTask | SOME_MEMBERS | check-electrical-safety-certificate-uploads |

## Decisions and the answers they read

| Decision | Steps whose answers it reads | Reads every answer |
|----------|------------------------------|--------------------|
| remove-electrical-safety-certificate-upload | electrical-safety-certificate-file-upload | no |
