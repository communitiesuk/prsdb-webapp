package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states

import uk.gov.communities.prsdb.webapp.journeys.JourneyState

interface LettingAgentInterruptionState : JourneyState {
    val propertyIsOccupied: Boolean
    val propertyIsDelegatedToLettingAgent: Boolean
}
