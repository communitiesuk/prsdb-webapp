package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.TaskWithoutDependencies
import uk.gov.communities.prsdb.webapp.journeys.isComplete
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.FreeholderAddressState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.FreeholderAddressStep

// TODO PDJB-1967: replace the placeholder step with the real address search, select and manual entry steps
@JourneyFrameworkComponent
class FreeholderAddressTask(
    journeyStateService: JourneyStateService,
    override val freeholderAddressStep: FreeholderAddressStep,
) : TaskWithoutDependencies<FreeholderAddressState>(journeyStateService),
    FreeholderAddressState {
    override val taskState get() = this

    override fun makeSubJourney(state: FreeholderAddressState) =
        subJourney(state) {
            step(journey.freeholderAddressStep) {
                routeSegment(FreeholderAddressStep.ROUTE_SEGMENT)
                nextStep { exitStep }
            }
            exitStep {
                parents { journey.freeholderAddressStep.isComplete() }
            }
        }
}
