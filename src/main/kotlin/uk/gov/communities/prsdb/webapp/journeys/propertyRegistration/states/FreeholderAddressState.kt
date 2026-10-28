package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states

import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.FreeholderAddressStep

interface FreeholderAddressState : JourneyState {
    val freeholderAddressStep: FreeholderAddressStep
}
