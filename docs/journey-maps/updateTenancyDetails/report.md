# Update tenancy details journey map report

Generated 2026-10-23T09:37:45.643295500Z at commit `3a5832daa`. Feature flags: failover-test-endpoints=false, subject-identifier-page=true, pdjb-939-property-registration-phase-two=false, pdjb-1053-dashboard-nav-link=true, pdjb-1022-delegate-to-letting-agent=true, pdjb-1664-password-benchmark-endpoint=false, pdjb-238-gatling-post-endpoint=false, pdjb-1040-correspondence-address=true, pdjb-1009-payments=false, pdjb-926-landlord-dashboard-update=true, pdjb-1210-multi-user-organisations=false.

| Pages | Variants | Edges | Gaps | Errors |
|-------|----------|-------|------|--------|
| 11 | 16 | 21 | 0 | 2 |

## Gaps

None.

## Errors

| Node | Kind | Detail |
|------|------|--------|
| tenancy-details-check-your-answers | http-500 | "Change Number of households" led to http://localhost:63436/landlord/property-details/1/update-tenancy-details/number-of-households?journeyId=shqjwg6 |
| tenancy-details-check-your-answers | http-500 | "Change Number of tenants" led to http://localhost:63436/landlord/property-details/1/update-tenancy-details/number-of-people?journeyId=6u6sd2r |

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
