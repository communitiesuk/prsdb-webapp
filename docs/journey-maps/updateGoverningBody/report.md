# Update the governing body journey map report

Generated 2026-10-23T09:40:18.143977500Z at commit `3a5832daa`. Feature flags: failover-test-endpoints=false, subject-identifier-page=true, pdjb-939-property-registration-phase-two=false, pdjb-1053-dashboard-nav-link=true, pdjb-1022-delegate-to-letting-agent=true, pdjb-1664-password-benchmark-endpoint=false, pdjb-238-gatling-post-endpoint=false, pdjb-1040-correspondence-address=true, pdjb-1009-payments=false, pdjb-926-landlord-dashboard-update=true, pdjb-1210-multi-user-organisations=false.

| Pages | Variants | Edges | Gaps | Errors |
|-------|----------|-------|------|--------|
| 16 | 23 | 29 | 0 | 0 |

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
