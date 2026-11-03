# Update tenancy details journey map report

Generated 2026-11-03T12:32:21.792005300Z at commit `f960f1073`. Feature flags: failover-test-endpoints=false, subject-identifier-page=true, pdjb-939-property-registration-phase-two=true, pdjb-939-property-registration-phase-three=false, pdjb-1664-password-benchmark-endpoint=false, pdjb-238-gatling-post-endpoint=false, pdjb-1009-payments=true, pdjb-1210-multi-user-organisations=true, pdjb-1894-gas-certificate-14-month-expiry=false, pdjb-955-associated-parties=false, pdjb-1666-eic-issue-date=false, pdjb-973-asynchronous-email-sending=false.

| Pages | Variants | Edges | Gaps | Errors |
|-------|----------|-------|------|--------|
| 11 | 16 | 21 | 0 | 2 |

## Gaps

None.

## Errors

| Node | Kind | Detail |
|------|------|--------|
| tenancy-details-check-your-answers | http-500 | "Change Number of households" led to http://localhost:58584/landlord/property-details/1/update-tenancy-details/number-of-households?journeyId=01d61v9 |
| tenancy-details-check-your-answers | http-500 | "Change Number of tenants" led to http://localhost:58584/landlord/property-details/1/update-tenancy-details/number-of-people?journeyId=fhgg88f |

## Routing code

Every arrow the journey's routing code defines between pages and decisions, for every outcome.

| Arrows | Walked | Not walked | Depend on earlier answers |
|--------|--------|------------|---------------------------|
| 11 | 9 | 2 | 0 |

### Arrows not walked

| From | Outcome | To |
|------|---------|----|
| number-of-households | PROVIDE_THIS_LATER | provide-tenancy-details-later |
| provide-tenancy-details-later | COMPLETE | rent-includes-bills |
