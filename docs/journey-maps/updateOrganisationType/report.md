# Update the organisation type journey map report

Generated 2026-10-23T09:38:37.749667400Z at commit `3a5832daa`. Feature flags: failover-test-endpoints=false, subject-identifier-page=true, pdjb-939-property-registration-phase-two=false, pdjb-1053-dashboard-nav-link=true, pdjb-1022-delegate-to-letting-agent=true, pdjb-1664-password-benchmark-endpoint=false, pdjb-238-gatling-post-endpoint=false, pdjb-1040-correspondence-address=true, pdjb-1009-payments=false, pdjb-926-landlord-dashboard-update=true, pdjb-1210-multi-user-organisations=false.

| Pages | Variants | Edges | Gaps | Errors |
|-------|----------|-------|------|--------|
| 16 | 23 | 31 | 0 | 1 |

## Gaps

None.

## Errors

| Node | Kind | Detail |
|------|------|--------|
| organisation-type-check-your-answers | http-500 | "Change Address" led to http://localhost:63436/landlord/landlord-details/update-organisation-type/lead-trustee-address/lookup-address?journeyId=y666lku |

## Routing code

Every arrow the journey's routing code defines between pages and decisions, for every outcome.

| Arrows | Walked | Not walked | Depend on earlier answers |
|--------|--------|------------|---------------------------|
| 18 | 18 | 0 | 2 |

### Arrows that depend on earlier answers

The routing code for these arrows also reads the journey state listed, so with other earlier answers they can lead elsewhere. Where they lead is only known once walked.

| From | Outcome | Walked | Reads |
|------|---------|--------|-------|
| organisation-type-trust-interruption | COMPLETE | yes | outcome:internal:OrgTypeUpdateRoutingStep, outcome:organisation-type, step:organisation-type |
| organisation-type-check-your-answers | COMPLETE | yes | outcome:internal:OrgTypeUpdateRoutingStep |

## Decisions and the answers they read

| Decision | Steps whose answers it reads | Reads every answer |
|----------|------------------------------|--------------------|
| internal:OrgTypeUpdateRoutingStep | organisation-type | no |
