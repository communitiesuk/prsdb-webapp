package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states

import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.OccupiedStep

interface OccupationState : JourneyState {
    val occupied: OccupiedStep
    var cachedOccupied: Boolean?
}
