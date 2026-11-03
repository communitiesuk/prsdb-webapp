# Update the organisation type journey map report

Generated 2026-11-03T12:33:16.145742400Z at commit `f960f1073`. Feature flags: failover-test-endpoints=false, subject-identifier-page=true, pdjb-939-property-registration-phase-two=true, pdjb-939-property-registration-phase-three=false, pdjb-1664-password-benchmark-endpoint=false, pdjb-238-gatling-post-endpoint=false, pdjb-1009-payments=true, pdjb-1210-multi-user-organisations=true, pdjb-1894-gas-certificate-14-month-expiry=false, pdjb-955-associated-parties=false, pdjb-1666-eic-issue-date=false, pdjb-973-asynchronous-email-sending=false.

| Pages | Variants | Edges | Gaps | Errors |
|-------|----------|-------|------|--------|
| 16 | 23 | 31 | 0 | 1 |

## Gaps

None.

## Errors

| Node | Kind | Detail |
|------|------|--------|
| organisation-type-check-your-answers | http-500 | "Change Address" led to http://localhost:58584/landlord/landlord-details/update-organisation-type/lead-trustee-address/lookup-address?journeyId=h4v5xrn |

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
