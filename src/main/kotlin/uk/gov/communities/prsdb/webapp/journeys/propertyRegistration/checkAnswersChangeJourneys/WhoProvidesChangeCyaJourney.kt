package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.checkAnswersChangeJourneys

import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.builders.JourneyBuilder
import uk.gov.communities.prsdb.webapp.journeys.hasOutcome
import uk.gov.communities.prsdb.webapp.journeys.isComplete
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.PropertyRegistrationJourneyState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.ConfirmChangeToLettingAgentStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.LettingAgentEmailStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.WhoProvidesRentalDetailsStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.WhoProvidesUpdateRouteMode
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.WhoProvidesUpdateRoutingStepConfig

fun <T : PropertyRegistrationJourneyState> JourneyBuilder<T>.whoProvidesChangeCyaJourney() {
    fromTask(journey.whoProvidesDetailsTask, journey) {
        step(task.whoProvidesRentalDetailsStep) {
            initialStep()
            routeSegment(WhoProvidesRentalDetailsStep.ROUTE_SEGMENT)
            nextStep { journey.whoProvidesUpdateRoutingStep }
        }
    }
    step<WhoProvidesUpdateRouteMode, WhoProvidesUpdateRoutingStepConfig>(journey.whoProvidesUpdateRoutingStep) {
        stepSpecificInitialisation {
            usingPreviousDelegation { getWasDelegatedToLettingAgentFromBaseJourney(journey) }
        }
        parents { journey.whoProvidesDetailsTask.whoProvidesRentalDetailsStep.isComplete() }
        nextDestination { mode ->
            when (mode) {
                WhoProvidesUpdateRouteMode.UNCHANGED -> Destination(journey.finishCyaStep)
                WhoProvidesUpdateRouteMode.CHANGED_TO_LANDLORD -> Destination(journey.finishCyaStep)
                WhoProvidesUpdateRouteMode.CHANGED_TO_LETTING_AGENT -> Destination(journey.confirmChangeToLettingAgentStep)
            }
        }
    }
    step(journey.confirmChangeToLettingAgentStep) {
        routeSegment(ConfirmChangeToLettingAgentStep.ROUTE_SEGMENT)
        parents {
            journey.whoProvidesUpdateRoutingStep.hasOutcome(WhoProvidesUpdateRouteMode.CHANGED_TO_LETTING_AGENT)
        }
        nextStep { journey.whoProvidesDetailsTask.lettingAgentEmailStep }
    }
    fromTask(journey.whoProvidesDetailsTask) {
        step(task.lettingAgentEmailStep) {
            routeSegment(LettingAgentEmailStep.ROUTE_SEGMENT)
            parents { journey.confirmChangeToLettingAgentStep.isComplete() }
            nextStep { journey.finishCyaStep }
        }
    }
}
