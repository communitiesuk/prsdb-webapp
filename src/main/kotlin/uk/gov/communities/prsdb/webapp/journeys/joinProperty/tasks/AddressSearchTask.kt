package uk.gov.communities.prsdb.webapp.journeys.joinProperty.tasks

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.controllers.JoinPropertyController.Companion.JOIN_PROPERTY_ROUTE
import uk.gov.communities.prsdb.webapp.journeys.OrParents
import uk.gov.communities.prsdb.webapp.journeys.Task
import uk.gov.communities.prsdb.webapp.journeys.hasOutcome
import uk.gov.communities.prsdb.webapp.journeys.isComplete
import uk.gov.communities.prsdb.webapp.journeys.joinProperty.FindPropertySearchResult
import uk.gov.communities.prsdb.webapp.journeys.joinProperty.states.AddressSearchState
import uk.gov.communities.prsdb.webapp.journeys.joinProperty.steps.FindPropertyStep
import uk.gov.communities.prsdb.webapp.journeys.joinProperty.steps.NoMatchingPropertiesStep
import uk.gov.communities.prsdb.webapp.journeys.joinProperty.steps.PropertyNotRegisteredStep
import uk.gov.communities.prsdb.webapp.journeys.joinProperty.steps.SelectPropertyStep

@JourneyFrameworkComponent
class AddressSearchTask : Task<AddressSearchState>() {
    override fun makeSubJourney(state: AddressSearchState) =
        subJourney(state) {
            step(journey.findPropertyStep) {
                routeSegment(FindPropertyStep.ROUTE_SEGMENT)
                backUrl { JOIN_PROPERTY_ROUTE }
                nextStep { mode ->
                    when (mode) {
                        FindPropertySearchResult.RESULTS_FOUND -> journey.selectPropertyStep
                        FindPropertySearchResult.NO_RESULTS -> journey.noMatchingPropertiesStep
                    }
                }
            }
            // TODO: PDJB-276 - Connect when no properties match search
            step(journey.noMatchingPropertiesStep) {
                routeSegment(NoMatchingPropertiesStep.ROUTE_SEGMENT)
                parents { journey.findPropertyStep.hasOutcome(FindPropertySearchResult.NO_RESULTS) }
                nextStep { exitStep }
            }
            // TODO: PDJB-275 - Add conditional routing to error pages
            step(journey.selectPropertyStep) {
                routeSegment(SelectPropertyStep.ROUTE_SEGMENT)
                parents { journey.findPropertyStep.hasOutcome(FindPropertySearchResult.RESULTS_FOUND) }
                nextStep { journey.propertyNotRegisteredStep }
            }
            // TODO: PDJB-283 - Connect when property is not registered
            step(journey.propertyNotRegisteredStep) {
                routeSegment(PropertyNotRegisteredStep.ROUTE_SEGMENT)
                parents { journey.selectPropertyStep.isComplete() }
                nextStep { exitStep }
            }
            exitStep {
                parents {
                    OrParents(
                        journey.propertyNotRegisteredStep.isComplete(),
                        journey.noMatchingPropertiesStep.isComplete(),
                    )
                }
            }
        }
}
