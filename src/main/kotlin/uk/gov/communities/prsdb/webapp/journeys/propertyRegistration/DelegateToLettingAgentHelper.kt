package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration

import uk.gov.communities.prsdb.webapp.config.managers.FeatureFlagManager
import uk.gov.communities.prsdb.webapp.constants.DELEGATE_TO_LETTING_AGENT
import uk.gov.communities.prsdb.webapp.constants.PROPERTY_REGISTRATION_RESTRUCTURE_AND_SKIPPING
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.WhoProvidesRentalDetailsMode

// A letting agent providing the rented-out details means the landlord provides none of the rented-out
// compliance/tenancy details, so those tasks are skipped in the journey. The check-your-answers and save
// steps use this to substitute placeholder "provide later" values for the skipped sections.
// TODO PDJB-1391: replace the placeholder handling in the CYA/save steps with the real delegated-details flow.
//
// The who-provides-details step is only wired into the journey graph when both the restructure and
// delegate flags are on, so the restructure flag must be checked before reading the step's outcome:
// otherwise, with the delegate flag on but restructure off (the legacy journey), reading the outcome
// throws because the unwired step's parentage is never initialised.
fun PropertyRegistrationJourneyState.isDelegatedToLettingAgent(featureFlagManager: FeatureFlagManager): Boolean =
    featureFlagManager.checkFeature(DELEGATE_TO_LETTING_AGENT) &&
        featureFlagManager.checkFeature(PROPERTY_REGISTRATION_RESTRUCTURE_AND_SKIPPING) &&
        whoProvidesDetailsTask.whoProvidesRentalDetailsStep.outcome == WhoProvidesRentalDetailsMode.LETTING_AGENT_PROVIDES
