package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.checkAnswersChangeJourneys

import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.builders.JourneyBuilder
import uk.gov.communities.prsdb.webapp.journeys.hasOutcome
import uk.gov.communities.prsdb.webapp.journeys.isComplete
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.PropertyRegistrationJourneyState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.OccupancyChangeInterruptionStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.OccupancyChangeRouteMode
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.OccupancyChangeRoutingStepConfig
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.OccupiedStep

fun <T : PropertyRegistrationJourneyState> JourneyBuilder<T>.occupancyChangeCyaJourney() {
    step(journey.occupied) {
        initialStep()
        routeSegment(OccupiedStep.ROUTE_SEGMENT)
        nextStep { journey.occupancyChangeRoutingStep }
    }
    step<OccupancyChangeRouteMode, OccupancyChangeRoutingStepConfig>(journey.occupancyChangeRoutingStep) {
        stepSpecificInitialisation {
            usingPreviousDelegation { getWasDelegatedToLettingAgentFromBaseJourney(journey) }
        }
        parents { journey.occupied.isComplete() }
        nextDestination { mode ->
            when (mode) {
                OccupancyChangeRouteMode.NO_INTERRUPTION -> Destination(journey.finishCyaStep)
                OccupancyChangeRouteMode.REMOVING_DELEGATION -> Destination(journey.occupancyChangeInterruptionStep)
            }
        }
    }
    step(journey.occupancyChangeInterruptionStep) {
        routeSegment(OccupancyChangeInterruptionStep.ROUTE_SEGMENT)
        parents { journey.occupancyChangeRoutingStep.hasOutcome(OccupancyChangeRouteMode.REMOVING_DELEGATION) }
        nextStep { journey.finishCyaStep }
    }
}
