package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states

import uk.gov.communities.prsdb.webapp.journeys.JourneyState

interface LettingAgentInterruptionState : JourneyState {
    val wasOccupied: Boolean
    val isDelegatedToLettingAgent: Boolean

    /**
     * The letting agent interruption is only shown when the property was occupied at the start of the journey and is
     * delegated to a letting agent - otherwise the "you've changed this property to being unoccupied" copy would not
     * be true.
     */
    val showsLettingAgentInterruption: Boolean get() = wasOccupied && isDelegatedToLettingAgent
}
