package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration

import uk.gov.communities.prsdb.webapp.config.managers.FeatureFlagManager
import uk.gov.communities.prsdb.webapp.constants.DELEGATE_TO_LETTING_AGENT
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.WhoProvidesRentalDetailsMode

// A letting agent providing the rented-out details means the landlord provides none of the rented-out
// compliance/tenancy details, so those tasks are skipped in the journey. The check-your-answers and save
// steps use this to substitute placeholder "provide later" values for the skipped sections.
// TODO PDJB-1391: replace the placeholder handling in the CYA/save steps with the real delegated-details flow.
fun PropertyRegistrationJourneyState.isDelegatedToLettingAgent(featureFlagManager: FeatureFlagManager): Boolean =
    featureFlagManager.checkFeature(DELEGATE_TO_LETTING_AGENT) &&
        whoProvidesDetailsTask.whoProvidesRentalDetailsStep.outcome == WhoProvidesRentalDetailsMode.LETTING_AGENT_PROVIDES
