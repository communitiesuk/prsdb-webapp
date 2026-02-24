package uk.gov.communities.prsdb.webapp.journeys.joinProperty.tasks

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.Task
import uk.gov.communities.prsdb.webapp.journeys.isComplete
import uk.gov.communities.prsdb.webapp.journeys.joinProperty.states.PropertyAddressSearchState
import uk.gov.communities.prsdb.webapp.journeys.joinProperty.steps.FindPropertyStep
import uk.gov.communities.prsdb.webapp.journeys.joinProperty.steps.NoMatchingPropertiesStep
import uk.gov.communities.prsdb.webapp.journeys.joinProperty.steps.PropertyNotRegisteredStep
import uk.gov.communities.prsdb.webapp.journeys.joinProperty.steps.SelectPropertyStep

@JourneyFrameworkComponent
class PropertyAddressSearchTask : Task<PropertyAddressSearchState>() {
    override fun makeSubJourney(state: PropertyAddressSearchState) =
        subJourney(state) {
            // TODO: PDJB-274 - Add conditional routing based on search results
            step(journey.findPropertyStep) {
                routeSegment(FindPropertyStep.ROUTE_SEGMENT)
                nextStep { journey.noMatchingPropertiesStep }
            }
            // TODO: PDJB-276 - Connect when no properties match search
            step(journey.noMatchingPropertiesStep) {
                routeSegment(NoMatchingPropertiesStep.ROUTE_SEGMENT)
                parents { journey.findPropertyStep.isComplete() }
                nextStep { journey.selectPropertyStep }
            }
            // TODO: PDJB-275 - Add conditional routing to error pages
            step(journey.selectPropertyStep) {
                routeSegment(SelectPropertyStep.ROUTE_SEGMENT)
                parents { journey.noMatchingPropertiesStep.isComplete() }
                nextStep { journey.propertyNotRegisteredStep }
            }
            // TODO: PDJB-283 - Connect when property is not registered
            step(journey.propertyNotRegisteredStep) {
                routeSegment(PropertyNotRegisteredStep.ROUTE_SEGMENT)
                parents { journey.selectPropertyStep.isComplete() }
                nextStep { exitStep }
            }
            exitStep {
                parents { journey.propertyNotRegisteredStep.isComplete() }
            }
        }
}
