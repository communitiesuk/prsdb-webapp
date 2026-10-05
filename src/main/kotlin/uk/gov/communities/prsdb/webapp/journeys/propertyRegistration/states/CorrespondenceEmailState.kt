package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states

import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.CorrespondenceEmailStep

interface CorrespondenceEmailState : JourneyState {
    val correspondenceEmailStep: CorrespondenceEmailStep
    val loggedInLandlordEmailAtStartOfJourney: String?
}
