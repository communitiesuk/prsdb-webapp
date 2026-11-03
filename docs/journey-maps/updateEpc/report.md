# Update the energy performance certificate journey map report

Generated 2026-11-03T12:30:36.873919400Z at commit `f960f1073`. Feature flags: failover-test-endpoints=false, subject-identifier-page=true, pdjb-939-property-registration-phase-two=true, pdjb-939-property-registration-phase-three=false, pdjb-1664-password-benchmark-endpoint=false, pdjb-238-gatling-post-endpoint=false, pdjb-1009-payments=true, pdjb-1210-multi-user-organisations=true, pdjb-1894-gas-certificate-14-month-expiry=false, pdjb-955-associated-parties=false, pdjb-1666-eic-issue-date=false, pdjb-973-asynchronous-email-sending=false.

| Pages | Variants | Edges | Gaps | Errors |
|-------|----------|-------|------|--------|
| 22 | 42 | 54 | 0 | 0 |

## Gaps

None.

## Errors

None.

## Variants not captured

These pages had more variants than the generator keeps for each page.

| Node | Variants not captured |
|------|-----------------------|
| /landlord/property-details/1 | 1 |

## Routing code

Every arrow the journey's routing code defines between pages and decisions, for every outcome.

| Arrows | Walked | Not walked | Depend on earlier answers |
|--------|--------|------------|---------------------------|
| 33 | 31 | 2 | 0 |

### Arrows not walked

| From | Outcome | To |
|------|---------|----|
| has-epc | PROVIDE_LATER | provide-epc-later |
| provide-epc-later | COMPLETE | check-epc-answers |

## Decisions and the answers they read

| Decision | Steps whose answers it reads | Reads every answer |
|----------|------------------------------|--------------------|
| internal:EpcLookupByUprnStep@task:EpcDetailsTask | start | no |
| internal:EpcAgeCheckStep@task:EpcDetailsTask | check-epc-details, confirm-epc-details, epc-superseded | no |
| internal:EpcEnergyRatingCheckStep@task:EpcDetailsTask | check-epc-details, confirm-epc-details, epc-superseded | no |
