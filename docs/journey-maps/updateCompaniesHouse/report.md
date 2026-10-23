# Update whether the organisation is registered with Companies House journey map report

Generated 2026-10-23T09:39:29.588225700Z at commit `3a5832daa`. Feature flags: failover-test-endpoints=false, subject-identifier-page=true, pdjb-939-property-registration-phase-two=false, pdjb-1053-dashboard-nav-link=true, pdjb-1022-delegate-to-letting-agent=true, pdjb-1664-password-benchmark-endpoint=false, pdjb-238-gatling-post-endpoint=false, pdjb-1040-correspondence-address=true, pdjb-1009-payments=false, pdjb-926-landlord-dashboard-update=true, pdjb-1210-multi-user-organisations=false.

| Pages | Variants | Edges | Gaps | Errors |
|-------|----------|-------|------|--------|
| 17 | 34 | 41 | 0 | 0 |

## Gaps

None.

## Errors

None.

## Routing code

Every arrow the journey's routing code defines between pages and decisions, for every outcome.

| Arrows | Walked | Not walked | Depend on earlier answers |
|--------|--------|------------|---------------------------|
| 26 | 26 | 0 | 2 |

### Arrows that depend on earlier answers

The routing code for these arrows also reads the journey state listed, so with other earlier answers they can lead elsewhere. Where they lead is only known once walked.

| From | Outcome | Walked | Reads |
|------|---------|--------|-------|
| organisation-companies-house-interruption | COMPLETE | yes | outcome:internal:InitialiseGovBodyMembersStep@task:UpdateCompaniesHouseTask, outcome:internal:OrgCompaniesHouseUpdateRoutingStep@task:UpdateCompaniesHouseTask, outcome:organisation-companies-house, step:organisation-companies-house |
| internal:GovBodyMembersBackRoutingStep@task:UpdateCompaniesHouseTask | NO_MEMBERS | yes | outcome:internal:OrgCompaniesHouseUpdateRoutingStep@task:UpdateCompaniesHouseTask |

## Decisions and the answers they read

| Decision | Steps whose answers it reads | Reads every answer |
|----------|------------------------------|--------------------|
| remove-governing-body-member | organisation-companies-house, organisation-governing-body-member-address/manual-address, organisation-governing-body-member-address/select-address | no |
| organisation-governing-body-member-address/lookup-address | organisation-governing-body-member-address/manual-address, organisation-governing-body-member-address/select-address | no |
| organisation-governing-body-member-address/select-address | organisation-governing-body-member-address/manual-address | no |
| internal:OrgCompaniesHouseUpdateRoutingStep@task:UpdateCompaniesHouseTask | organisation-companies-house | no |
| internal:HasAnyGovBodyMembersStep@task:UpdateCompaniesHouseTask>task:OrgGovBodyMembersTask | organisation-companies-house, organisation-governing-body-member-address/manual-address, organisation-governing-body-member-address/select-address, remove-governing-body-member | no |
| internal:GovBodyMembersBackRoutingStep@task:UpdateCompaniesHouseTask | organisation-companies-house, organisation-governing-body-member-address/manual-address, organisation-governing-body-member-address/select-address, remove-governing-body-member | no |
| internal:HasAnyGovBodyMembersStep@task:OrgGovBodyMembersTask | organisation-companies-house, organisation-governing-body-member-address/manual-address, organisation-governing-body-member-address/select-address, remove-governing-body-member | no |
