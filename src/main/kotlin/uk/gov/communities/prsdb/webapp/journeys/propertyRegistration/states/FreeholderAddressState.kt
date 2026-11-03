package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states

import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.FreeholderAddressStep

// TODO: PDJB-1967: Update this
interface FreeholderAddressState : JourneyState {
    val freeholderAddressStep: FreeholderAddressStep
}
