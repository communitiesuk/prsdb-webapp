package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states

import uk.gov.communities.prsdb.webapp.journeys.JourneyState

interface LettingAgentInterruptionState : JourneyState {
    val wasOccupied: Boolean
    val isDelegatedToLettingAgent: Boolean

    val showsLettingAgentInterruption: Boolean get() = wasOccupied && isDelegatedToLettingAgent
}
