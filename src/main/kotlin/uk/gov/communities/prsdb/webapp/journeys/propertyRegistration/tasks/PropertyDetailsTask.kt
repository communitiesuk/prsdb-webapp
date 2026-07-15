package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.Task
import uk.gov.communities.prsdb.webapp.journeys.isComplete
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.PropertyDetailsState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.BedroomsStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.PropertyTypeStep

@JourneyFrameworkComponent
class PropertyDetailsTask : Task<PropertyDetailsState>() {
    override fun makeSubJourney(state: PropertyDetailsState) =
        subJourney(state) {
            duplicableTask(journey.addressTask) {
                nextStep { journey.addToLandlordIncompletePropertiesStep }
                savable()
            }
            step(journey.addToLandlordIncompletePropertiesStep) {
                parents { journey.addressTask.isComplete() }
                nextStep { journey.propertyTypeStep }
                savable()
            }
            step(journey.propertyTypeStep) {
                routeSegment(PropertyTypeStep.ROUTE_SEGMENT)
                parents { journey.addressTask.isComplete() }
                nextStep { journey.bedrooms }
                savable()
            }
            step(journey.bedrooms) {
                routeSegment(BedroomsStep.ROUTE_SEGMENT)
                parents { journey.propertyTypeStep.isComplete() }
                nextStep { exitStep }
                savable()
            }
            exitStep {
                parents { journey.bedrooms.isComplete() }
            }
        }
}
