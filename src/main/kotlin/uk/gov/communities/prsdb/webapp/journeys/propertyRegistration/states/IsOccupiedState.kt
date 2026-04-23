package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states

import uk.gov.communities.prsdb.webapp.journeys.JourneyState

interface IsOccupiedState : JourneyState {
    val isOccupied: Boolean?
}
