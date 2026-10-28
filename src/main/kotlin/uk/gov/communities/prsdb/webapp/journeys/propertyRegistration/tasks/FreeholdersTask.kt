package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.TaskWithoutDependencies
import uk.gov.communities.prsdb.webapp.journeys.isComplete
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.FreeholdersState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.FreeholdersInfoStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.FreeholdersListStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.PersonContactTypeStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.PersonEmailStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.PersonNameStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.PersonProvideDetailsStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.PersonRemoveConfirmationStep

@JourneyFrameworkComponent
class FreeholdersTask(
    journeyStateService: JourneyStateService,
    override val freeholdersInfoStep: FreeholdersInfoStep,
    override val freeholderProvideDetailsStep: PersonProvideDetailsStep,
    override val freeholderNameStep: PersonNameStep,
    override val freeholderContactTypeStep: PersonContactTypeStep,
    override val freeholderEmailStep: PersonEmailStep,
    override val freeholderAddressTask: FreeholderAddressTask,
    override val freeholdersListStep: FreeholdersListStep,
    override val removeFreeholderStep: PersonRemoveConfirmationStep,
) : TaskWithoutDependencies<FreeholdersState>(journeyStateService),
    FreeholdersState {
    override val taskState get() = this

    override fun makeSubJourney(state: FreeholdersState) =
        subJourney(state) {
            step(journey.freeholdersInfoStep) {
                routeSegment(FreeholdersInfoStep.ROUTE_SEGMENT)
                nextStep { journey.freeholderProvideDetailsStep }
                savable()
            }
            step(journey.freeholderProvideDetailsStep) {
                routeSegment(PersonProvideDetailsStep.ROUTE_SEGMENT)
                parents { journey.freeholdersInfoStep.isComplete() }
                nextStep { journey.freeholderNameStep }
                savable()
            }
            step(journey.freeholderNameStep) {
                routeSegment(PersonNameStep.ROUTE_SEGMENT)
                parents { journey.freeholderProvideDetailsStep.isComplete() }
                nextStep { journey.freeholderContactTypeStep }
                savable()
            }
            step(journey.freeholderContactTypeStep) {
                routeSegment(PersonContactTypeStep.ROUTE_SEGMENT)
                parents { journey.freeholderNameStep.isComplete() }
                nextStep { journey.freeholderEmailStep }
                savable()
            }
            step(journey.freeholderEmailStep) {
                routeSegment(PersonEmailStep.ROUTE_SEGMENT)
                parents { journey.freeholderContactTypeStep.isComplete() }
                nextStep { journey.freeholderAddressTask.firstStep }
                savable()
            }
            task(journey.freeholderAddressTask) {
                parents { journey.freeholderEmailStep.isComplete() }
                nextStep { journey.freeholdersListStep }
                savable()
            }
            step(journey.freeholdersListStep) {
                routeSegment(FreeholdersListStep.ROUTE_SEGMENT)
                parents { journey.freeholderAddressTask.isComplete() }
                nextStep { journey.removeFreeholderStep }
                savable()
            }
            step(journey.removeFreeholderStep) {
                routeSegment(PersonRemoveConfirmationStep.ROUTE_SEGMENT)
                parents { journey.freeholdersListStep.isComplete() }
                nextStep { exitStep }
                savable()
            }
            exitStep {
                parents { journey.removeFreeholderStep.isComplete() }
            }
        }
}
