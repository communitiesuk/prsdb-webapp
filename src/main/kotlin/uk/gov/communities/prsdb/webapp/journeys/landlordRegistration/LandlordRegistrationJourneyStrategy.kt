package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbFlip
import uk.gov.communities.prsdb.webapp.constants.ORGANISATION_LANDLORD_REGISTRATION
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import java.security.Principal

@PrsdbFlip(name = ORGANISATION_LANDLORD_REGISTRATION, alterBean = "landlordRegistrationOrgRedesignJourneyFactory")
interface LandlordRegistrationJourneyStrategy {
    fun createJourneySteps(): Map<String, StepLifecycleOrchestrator>

    fun initializeJourneyState(user: Principal): String
}
