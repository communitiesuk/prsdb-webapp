package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.TaskWithoutDependencies
import uk.gov.communities.prsdb.webapp.journeys.isComplete
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.SuperiorLandlordAddressState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.SuperiorLandlordAddressStep

// TODO PDJB-1976: replace the placeholder step with the real address search, select and manual entry steps
@JourneyFrameworkComponent
class SuperiorLandlordAddressTask(
    journeyStateService: JourneyStateService,
    override val superiorLandlordAddressStep: SuperiorLandlordAddressStep,
) : TaskWithoutDependencies<SuperiorLandlordAddressState>(journeyStateService),
    SuperiorLandlordAddressState {
    override val taskState get() = this

    override fun makeSubJourney(state: SuperiorLandlordAddressState) =
        subJourney(state) {
            step(journey.superiorLandlordAddressStep) {
                routeSegment(SuperiorLandlordAddressStep.ROUTE_SEGMENT)
                nextStep { exitStep }
            }
            exitStep {
                parents { journey.superiorLandlordAddressStep.isComplete() }
            }
        }
}
