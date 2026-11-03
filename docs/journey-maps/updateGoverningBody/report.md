# Update the governing body journey map report

Generated 2026-11-03T12:35:04.784212500Z at commit `f960f1073`. Feature flags: failover-test-endpoints=false, subject-identifier-page=true, pdjb-939-property-registration-phase-two=true, pdjb-939-property-registration-phase-three=false, pdjb-1664-password-benchmark-endpoint=false, pdjb-238-gatling-post-endpoint=false, pdjb-1009-payments=true, pdjb-1210-multi-user-organisations=true, pdjb-1894-gas-certificate-14-month-expiry=false, pdjb-955-associated-parties=false, pdjb-1666-eic-issue-date=false, pdjb-973-asynchronous-email-sending=false.

| Pages | Variants | Edges | Gaps | Errors |
|-------|----------|-------|------|--------|
| 15 | 23 | 28 | 0 | 0 |

## Gaps

None.

## Errors

None.

## Routing code

Every arrow the journey's routing code defines between pages and decisions, for every outcome.

| Arrows | Walked | Not walked | Depend on earlier answers |
|--------|--------|------------|---------------------------|
| 16 | 15 | 1 | 0 |

### Arrows not walked

| From | Outcome | To |
|------|---------|----|
| remove-governing-body-member | NO_MEMBERS | governing-body-check-your-answers |

## Decisions and the answers they read

| Decision | Steps whose answers it reads | Reads every answer |
|----------|------------------------------|--------------------|
| remove-governing-body-member | organisation-governing-body-member-address/manual-address, organisation-governing-body-member-address/select-address, start | no |
| organisation-governing-body-member-address/lookup-address | organisation-governing-body-member-address/manual-address, organisation-governing-body-member-address/select-address | no |
| organisation-governing-body-member-address/select-address | organisation-governing-body-member-address/manual-address | no |
| internal:HasAnyGovBodyMembersStep@task:OrgGovBodyMembersTask | organisation-governing-body-member-address/manual-address, organisation-governing-body-member-address/select-address, remove-governing-body-member, start | no |
