# Accept or reject a joint landlord invitation journey map report

Generated 2026-10-23T09:42:19.934191300Z at commit `3a5832daa`. Feature flags: failover-test-endpoints=false, subject-identifier-page=true, pdjb-939-property-registration-phase-two=false, pdjb-1053-dashboard-nav-link=true, pdjb-1022-delegate-to-letting-agent=true, pdjb-1664-password-benchmark-endpoint=false, pdjb-238-gatling-post-endpoint=false, pdjb-1040-correspondence-address=true, pdjb-1009-payments=false, pdjb-926-landlord-dashboard-update=true, pdjb-1210-multi-user-organisations=false.

| Pages | Variants | Edges | Gaps | Errors |
|-------|----------|-------|------|--------|
| 70 | 80 | 152 | 0 | 0 |

## Gaps

None.

## Errors

None.

## Routing code

Every arrow the journey's routing code defines between pages and decisions, for every outcome.

| Arrows | Walked | Not walked | Depend on earlier answers |
|--------|--------|------------|---------------------------|
| 83 | 81 | 2 | 6 |

### Arrows not walked

| From | Outcome | To |
|------|---------|----|
| remove-governing-body-member | SOME_MEMBERS | organisation-governing-body-member-list |
| internal:HasAnyGovBodyMembersStep@task:LandlordRegistrationTask>task:OrgLandlordRegistrationTask>task:OrgGovBodyTask>task:OrgGovBodyMembersTask | SOME_MEMBERS | organisation-governing-body-member-list |

### Arrows that depend on earlier answers

The routing code for these arrows also reads the journey state listed, so with other earlier answers they can lead elsewhere. Where they lead is only known once walked.

| From | Outcome | Walked | Reads |
|------|---------|--------|-------|
| accept-or-reject | NO | yes | outcome:accept-or-reject |
| landlord-type | INDIVIDUAL | yes | outcome:accept-or-reject, outcome:confirm-identity, outcome:date-of-birth, outcome:email, outcome:exit:task:IdentityTask, outcome:identity-not-verified, outcome:landlord-type, outcome:name, outcome:phone-number, outcome:privacy-notice, outcome:validate-token, outcome:verify-identity, step:accept-or-reject, tokenIsValid |
| landlord-type | ORGANISATION | yes | outcome:landlord-type |
| organisation-companies-house | NO | yes | outcome:exit:task:LeadTrusteeTask, outcome:exit:task:OrgAddressTask/organisation-address, outcome:exit:task:OrgCharityTask, outcome:exit:task:TrusteeAddressTask/lead-trustee-address, outcome:landlord-type, outcome:lead-trustee-address/lookup-address, outcome:lead-trustee-address/manual-address, outcome:lead-trustee-address/no-address-found, outcome:lead-trustee-address/select-address, outcome:lead-trustee-dob, outcome:lead-trustee-email, outcome:lead-trustee-name, outcome:lead-trustee-phone, outcome:organisation-address/lookup-address, outcome:organisation-address/manual-address, outcome:organisation-address/no-address-found, outcome:organisation-address/select-address, outcome:organisation-charity, outcome:organisation-charity-number, outcome:organisation-charity-number-northern-ireland, outcome:organisation-charity-number-scotland, outcome:organisation-charity-registered-with, outcome:organisation-companies-house, outcome:organisation-email, outcome:organisation-name, outcome:organisation-phone-number, outcome:organisation-type |
| organisation-company-number | COMPLETE | yes | outcome:exit:task:LeadTrusteeTask, outcome:exit:task:OrgAddressTask/organisation-address, outcome:exit:task:OrgCharityTask, outcome:exit:task:TrusteeAddressTask/lead-trustee-address, outcome:landlord-type, outcome:lead-trustee-address/lookup-address, outcome:lead-trustee-address/manual-address, outcome:lead-trustee-address/no-address-found, outcome:lead-trustee-address/select-address, outcome:lead-trustee-dob, outcome:lead-trustee-email, outcome:lead-trustee-name, outcome:lead-trustee-phone, outcome:organisation-address/lookup-address, outcome:organisation-address/manual-address, outcome:organisation-address/no-address-found, outcome:organisation-address/select-address, outcome:organisation-charity, outcome:organisation-charity-number, outcome:organisation-charity-number-northern-ireland, outcome:organisation-charity-number-scotland, outcome:organisation-charity-registered-with, outcome:organisation-companies-house, outcome:organisation-email, outcome:organisation-name, outcome:organisation-phone-number, outcome:organisation-type |
| confirm-landlord-for-property | COMPLETE | yes | outcome:accept-or-reject, tokenIsValid |

## Decisions and the answers they read

| Decision | Steps whose answers it reads | Reads every answer |
|----------|------------------------------|--------------------|
| validate-token | confirm-landlord-for-property | no |
| organisation-governing-body-details | organisation-companies-house-interruption | no |
| remove-governing-body-member | organisation-governing-body-member-address/manual-address, organisation-governing-body-member-address/select-address | no |
| organisation-governing-body-member-address/lookup-address | organisation-governing-body-member-address/manual-address, organisation-governing-body-member-address/select-address | no |
| organisation-governing-body-member-address/select-address | organisation-governing-body-member-address/manual-address | no |
| internal:HasAnyGovBodyMembersStep@task:LandlordRegistrationTask>task:OrgLandlordRegistrationTask>task:OrgGovBodyTask>task:OrgGovBodyMembersTask | organisation-governing-body-member-address/manual-address, organisation-governing-body-member-address/select-address, remove-governing-body-member | no |
| internal:GovBodyMembersBackRoutingStep@task:LandlordRegistrationTask>task:OrgLandlordRegistrationTask>task:OrgGovBodyTask | organisation-governing-body-member-address/manual-address, organisation-governing-body-member-address/select-address, remove-governing-body-member | no |
| internal:LandlordTypeChangeRedirectStep | country-of-residence, landlord-type, lead-trustee-address/lookup-address, lead-trustee-address/manual-address, lead-trustee-address/no-address-found, lead-trustee-address/select-address, lead-trustee-dob, lead-trustee-email, lead-trustee-name, lead-trustee-phone, lookup-address, organisation-address/lookup-address, organisation-address/manual-address, organisation-address/no-address-found, organisation-address/select-address, organisation-charity, organisation-charity-number, organisation-charity-number-northern-ireland, organisation-charity-number-scotland, organisation-charity-registered-with, organisation-companies-house, organisation-companies-house-interruption, organisation-company-number, organisation-email, organisation-governing-body-details, organisation-governing-body-member-address/manual-address, organisation-governing-body-member-address/select-address, organisation-governing-body-member-list, organisation-main-contact, organisation-name, organisation-phone-number, organisation-type, remove-governing-body-member, select-address | no |
| internal:OrgTypeUpdateRoutingStep | organisation-type | no |
| internal:OrgCompaniesHouseUpdateRoutingStep | organisation-companies-house | no |
| internal:HasAnyGovBodyMembersStep@task:OrgCompaniesHouseChangeGovBodyTask>task:OrgGovBodyMembersTask | organisation-governing-body-member-address/manual-address, organisation-governing-body-member-address/select-address, remove-governing-body-member | no |
| internal:HasAnyGovBodyMembersStep@task:OrgGovBodyTask>task:OrgGovBodyMembersTask | organisation-governing-body-member-address/manual-address, organisation-governing-body-member-address/select-address, remove-governing-body-member | no |
| internal:GovBodyMembersBackRoutingStep@task:OrgGovBodyTask | organisation-governing-body-member-address/manual-address, organisation-governing-body-member-address/select-address, remove-governing-body-member | no |
