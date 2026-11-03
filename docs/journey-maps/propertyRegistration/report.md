# Register a property journey map report

Generated 2026-11-03T12:27:59.318447800Z at commit `f960f1073`. Feature flags: failover-test-endpoints=false, subject-identifier-page=true, pdjb-939-property-registration-phase-two=true, pdjb-939-property-registration-phase-three=false, pdjb-1664-password-benchmark-endpoint=false, pdjb-238-gatling-post-endpoint=false, pdjb-1009-payments=true, pdjb-1210-multi-user-organisations=true, pdjb-1894-gas-certificate-14-month-expiry=false, pdjb-955-associated-parties=false, pdjb-1666-eic-issue-date=false, pdjb-973-asynchronous-email-sending=false.

| Pages | Variants | Edges | Gaps | Errors |
|-------|----------|-------|------|--------|
| 110 | 196 | 290 | 0 | 0 |

## Gaps

None.

## Errors

None.

## Routing code

Every arrow the journey's routing code defines between pages and decisions, for every outcome.

| Arrows | Walked | Not walked | Depend on earlier answers |
|--------|--------|------------|---------------------------|
| 139 | 135 | 4 | 6 |

### Arrows not walked

| From | Outcome | To |
|------|---------|----|
| internal:HasAnyJointLandlordsInvitedStep@section:registerProperty.taskList.aboutYourProperty.ownershipAndLandlords>task:OwnershipAndLandlordsTaskBeforeAssociatedParties>task:JointLandlordsPropertyRegistrationTask | SOME_LANDLORDS | internal:HasAnyJointLandlordsInvitedStep@section:registerProperty.taskList.aboutYourProperty.ownershipAndLandlords>task:OwnershipAndLandlordsTaskBeforeAssociatedParties>task:JointLandlordsPropertyRegistrationTask>task:InviteJointLandlordsTask |
| internal:HasAnyJointLandlordsInvitedStep@section:registerProperty.taskList.aboutYourProperty.ownershipAndLandlords>task:OwnershipAndLandlordsTaskBeforeAssociatedParties>task:JointLandlordsPropertyRegistrationTask>task:InviteJointLandlordsTask | SOME_LANDLORDS | check-joint-landlords |
| internal:HasAnyInCollectionStep@section:registerProperty.taskList.electricalSafety>task:ElectricalSafetyTask>task:ElectricalSafetyDetailsTask#2 | NO_MEMBERS | eicr-file-upload |
| internal:HasAnyInCollectionStep@section:registerProperty.taskList.electricalSafety>task:ElectricalSafetyTask>task:ElectricalSafetyDetailsTask#2 | SOME_MEMBERS | check-eicr-uploads |

### Arrows that depend on earlier answers

The routing code for these arrows also reads the journey state listed, so with other earlier answers they can lead elsewhere. Where they lead is only known once walked.

| From | Outcome | Walked | Reads |
|------|---------|--------|-------|
| check-joint-landlords | COMPLETE | yes | invitedJointLandlordEmails |
| remove-joint-landlord | NO_LANDLORDS | yes | invitedJointLandlordEmails |
| who-provides-rental-details | LANDLORD_PROVIDES | yes | cachedAddresses, outcome:check-joint-landlords, outcome:correspondence-address/lookup-address, outcome:correspondence-address/manual-address, outcome:correspondence-address/no-address-found, outcome:correspondence-address/select-address, outcome:correspondence-email, outcome:exit:task:CorrespondenceAddressTask/correspondence-address, outcome:exit:task:CorrespondenceTask, outcome:exit:task:InviteJointLandlordsTask, outcome:exit:task:JointLandlordsPropertyRegistrationTask, outcome:exit:task:OwnershipAndLandlordsTaskBeforeAssociatedParties, outcome:exit:task:PropertyDetailsTask, outcome:exit:task:PropertyRegistrationAddressTask, outcome:has-joint-landlords, outcome:internal:HasAnyJointLandlordsInvitedStep@section:registerProperty.taskList.aboutYourProperty.ownershipAndLandlords>task:OwnershipAndLandlordsTaskBeforeAssociatedParties>task:JointLandlordsPropertyRegistrationTask, outcome:internal:HasAnyJointLandlordsInvitedStep@section:registerProperty.taskList.aboutYourProperty.ownershipAndLandlords>task:OwnershipAndLandlordsTaskBeforeAssociatedParties>task:JointLandlordsPropertyRegistrationTask>task:InviteJointLandlordsTask, outcome:invite-joint-landlord, outcome:local-council, outcome:lookup-address, outcome:manual-address, outcome:no-address-found, outcome:number-of-bedrooms, outcome:occupancy, outcome:ownership-type, outcome:property-type, outcome:remove-joint-landlord, outcome:select-address, outcome:who-provides-rental-details |
| letting-agent-email | COMPLETE | yes | cachedAddresses, outcome:check-joint-landlords, outcome:correspondence-address/lookup-address, outcome:correspondence-address/manual-address, outcome:correspondence-address/no-address-found, outcome:correspondence-address/select-address, outcome:correspondence-email, outcome:exit:task:CorrespondenceAddressTask/correspondence-address, outcome:exit:task:CorrespondenceTask, outcome:exit:task:InviteJointLandlordsTask, outcome:exit:task:JointLandlordsPropertyRegistrationTask, outcome:exit:task:OwnershipAndLandlordsTaskBeforeAssociatedParties, outcome:exit:task:PropertyDetailsTask, outcome:exit:task:PropertyRegistrationAddressTask, outcome:has-joint-landlords, outcome:internal:HasAnyJointLandlordsInvitedStep@section:registerProperty.taskList.aboutYourProperty.ownershipAndLandlords>task:OwnershipAndLandlordsTaskBeforeAssociatedParties>task:JointLandlordsPropertyRegistrationTask, outcome:internal:HasAnyJointLandlordsInvitedStep@section:registerProperty.taskList.aboutYourProperty.ownershipAndLandlords>task:OwnershipAndLandlordsTaskBeforeAssociatedParties>task:JointLandlordsPropertyRegistrationTask>task:InviteJointLandlordsTask, outcome:invite-joint-landlord, outcome:local-council, outcome:lookup-address, outcome:manual-address, outcome:no-address-found, outcome:number-of-bedrooms, outcome:occupancy, outcome:ownership-type, outcome:property-type, outcome:remove-joint-landlord, outcome:select-address, outcome:who-provides-rental-details |
| number-of-people | COMPLETE | yes | outcome:check-eic-uploads, outcome:check-eicr-uploads, outcome:check-electrical-safety-answers, outcome:check-epc-answers, outcome:check-epc-details, outcome:check-gas-safety-answers, outcome:check-gas-safety-certificate-uploads, outcome:confirm-epc-details, outcome:eic-expired, outcome:eic-expiry-date, outcome:eic-file-upload, outcome:eicr-expired, outcome:eicr-expiry-date, outcome:eicr-file-upload, outcome:electrical-safety-certificate-missing, outcome:epc-exemption, outcome:epc-expired, outcome:epc-in-date-at-start-of-tenancy-check, outcome:epc-missing, outcome:epc-not-found, outcome:epc-superseded, outcome:exit:task:ElectricalSafetyDetailsTask, outcome:exit:task:ElectricalSafetyTask, outcome:exit:task:EpcDetailsTask, outcome:exit:task:EpcTask, outcome:exit:task:GasSafetyDetailsTask, outcome:exit:task:GasSafetyTask, outcome:exit:task:LicensingTask, outcome:exit:task:WhoProvidesDetailsTask, outcome:find-your-epc, outcome:gas-safety-certificate-expired, outcome:gas-safety-certificate-file-upload, outcome:gas-safety-certificate-issue-date, outcome:gas-safety-certificate-missing, outcome:has-electrical-safety, outcome:has-epc, outcome:has-gas-safety, outcome:has-gas-supply, outcome:has-mees-exemption, outcome:hmo-additional-licence, outcome:hmo-mandatory-licence, outcome:internal:EpcAgeCheckStep@section:registerProperty.taskList.epc>task:EpcTask>task:EpcDetailsTask, outcome:internal:EpcEnergyRatingCheckStep@section:registerProperty.taskList.epc>task:EpcTask>task:EpcDetailsTask, outcome:internal:EpcLookupByUprnStep@section:registerProperty.taskList.epc>task:EpcTask>task:EpcDetailsTask, outcome:internal:PropertyOccupiedCheckStep@section:registerProperty.taskList.epc>task:EpcTask>task:EpcDetailsTask, outcome:is-epc-required, outcome:letting-agent-email, outcome:licensing-type, outcome:low-energy-rating, outcome:mees-exemption, outcome:number-of-households, outcome:occupancy, outcome:provide-electrical-safety-certificate-later, outcome:provide-epc-later, outcome:provide-gas-safety-certificate-later, outcome:provide-licensing-later, outcome:selective-licence, outcome:start, outcome:who-provides-rental-details |
| provide-tenancy-details-later | COMPLETE | yes | outcome:check-eic-uploads, outcome:check-eicr-uploads, outcome:check-electrical-safety-answers, outcome:check-epc-answers, outcome:check-epc-details, outcome:check-gas-safety-answers, outcome:check-gas-safety-certificate-uploads, outcome:confirm-epc-details, outcome:eic-expired, outcome:eic-expiry-date, outcome:eic-file-upload, outcome:eicr-expired, outcome:eicr-expiry-date, outcome:eicr-file-upload, outcome:electrical-safety-certificate-missing, outcome:epc-exemption, outcome:epc-expired, outcome:epc-in-date-at-start-of-tenancy-check, outcome:epc-missing, outcome:epc-not-found, outcome:epc-superseded, outcome:exit:task:ElectricalSafetyDetailsTask, outcome:exit:task:ElectricalSafetyTask, outcome:exit:task:EpcDetailsTask, outcome:exit:task:EpcTask, outcome:exit:task:GasSafetyDetailsTask, outcome:exit:task:GasSafetyTask, outcome:exit:task:LicensingTask, outcome:exit:task:WhoProvidesDetailsTask, outcome:find-your-epc, outcome:gas-safety-certificate-expired, outcome:gas-safety-certificate-file-upload, outcome:gas-safety-certificate-issue-date, outcome:gas-safety-certificate-missing, outcome:has-electrical-safety, outcome:has-epc, outcome:has-gas-safety, outcome:has-gas-supply, outcome:has-mees-exemption, outcome:hmo-additional-licence, outcome:hmo-mandatory-licence, outcome:internal:EpcAgeCheckStep@section:registerProperty.taskList.epc>task:EpcTask>task:EpcDetailsTask, outcome:internal:EpcEnergyRatingCheckStep@section:registerProperty.taskList.epc>task:EpcTask>task:EpcDetailsTask, outcome:internal:EpcLookupByUprnStep@section:registerProperty.taskList.epc>task:EpcTask>task:EpcDetailsTask, outcome:internal:PropertyOccupiedCheckStep@section:registerProperty.taskList.epc>task:EpcTask>task:EpcDetailsTask, outcome:is-epc-required, outcome:letting-agent-email, outcome:licensing-type, outcome:low-energy-rating, outcome:mees-exemption, outcome:number-of-households, outcome:occupancy, outcome:provide-electrical-safety-certificate-later, outcome:provide-epc-later, outcome:provide-gas-safety-certificate-later, outcome:provide-licensing-later, outcome:selective-licence, outcome:start, outcome:who-provides-rental-details |

## Decisions and the answers they read

| Decision | Steps whose answers it reads | Reads every answer |
|----------|------------------------------|--------------------|
| select-address | payment-return | no |
| remove-joint-landlord | invite-another-joint-landlord, invite-joint-landlord | no |
| remove-gas-safety-certificate-upload | gas-safety-certificate-file-upload | no |
| remove-eic-upload | eic-file-upload | no |
| remove-eicr-upload | eicr-file-upload | no |
| internal:HasAnyJointLandlordsInvitedStep@section:registerProperty.taskList.aboutYourProperty.ownershipAndLandlords>task:OwnershipAndLandlordsTaskBeforeAssociatedParties>task:JointLandlordsPropertyRegistrationTask | invite-another-joint-landlord, invite-joint-landlord, remove-joint-landlord | no |
| internal:HasAnyJointLandlordsInvitedStep@section:registerProperty.taskList.aboutYourProperty.ownershipAndLandlords>task:OwnershipAndLandlordsTaskBeforeAssociatedParties>task:JointLandlordsPropertyRegistrationTask>task:InviteJointLandlordsTask | invite-another-joint-landlord, invite-joint-landlord, remove-joint-landlord | no |
| internal:EpcLookupByUprnStep@section:registerProperty.taskList.epc>task:EpcTask>task:EpcDetailsTask | start | no |
| internal:EpcAgeCheckStep@section:registerProperty.taskList.epc>task:EpcTask>task:EpcDetailsTask | check-epc-details, confirm-epc-details, epc-superseded | no |
| internal:PropertyOccupiedCheckStep@section:registerProperty.taskList.epc>task:EpcTask>task:EpcDetailsTask | occupancy | no |
| internal:EpcEnergyRatingCheckStep@section:registerProperty.taskList.epc>task:EpcTask>task:EpcDetailsTask | check-epc-details, confirm-epc-details, epc-superseded | no |
| internal:HasMissingComplianceStep@section:registerProperty.taskList.submitYourRegistration.headingWithPayment | check-epc-details, confirm-epc-details, eic-expiry-date, epc-superseded, gas-safety-certificate-issue-date, has-electrical-safety, has-epc, has-gas-safety, has-gas-supply, occupancy, start, who-provides-rental-details | no |
| internal:PaymentStatusCheckStep@section:registerProperty.taskList.submitYourRegistration.headingWithPayment | payment-return, payment-summary | no |
| internal:HasAnyJointLandlordsInvitedStep@task:JointLandlordsPropertyRegistrationTask | invite-another-joint-landlord, invite-joint-landlord, remove-joint-landlord | no |
| internal:HasAnyJointLandlordsInvitedStep@task:JointLandlordsPropertyRegistrationTask>task:InviteJointLandlordsTask | invite-another-joint-landlord, invite-joint-landlord, remove-joint-landlord | no |
| internal:OccupancyChangeRoutingStep | occupancy, who-provides-rental-details | no |
| internal:WhoProvidesUpdateRoutingStep | who-provides-rental-details | no |
| internal:EpcLookupByUprnStep@task:EpcDetailsTask | start | no |
| internal:EpcAgeCheckStep@task:EpcDetailsTask | check-epc-details, confirm-epc-details, epc-superseded | no |
| internal:EpcEnergyRatingCheckStep@task:EpcDetailsTask | check-epc-details, confirm-epc-details, epc-superseded | no |
