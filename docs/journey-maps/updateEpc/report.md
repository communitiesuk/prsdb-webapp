# Update the energy performance certificate journey map report

Generated 2026-10-23T09:35:57.904050200Z at commit `3a5832daa`. Feature flags: failover-test-endpoints=false, subject-identifier-page=true, pdjb-939-property-registration-phase-two=false, pdjb-1053-dashboard-nav-link=true, pdjb-1022-delegate-to-letting-agent=true, pdjb-1664-password-benchmark-endpoint=false, pdjb-238-gatling-post-endpoint=false, pdjb-1040-correspondence-address=true, pdjb-1009-payments=false, pdjb-926-landlord-dashboard-update=true, pdjb-1210-multi-user-organisations=false.

| Pages | Variants | Edges | Gaps | Errors |
|-------|----------|-------|------|--------|
| 23 | 42 | 55 | 0 | 0 |

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
