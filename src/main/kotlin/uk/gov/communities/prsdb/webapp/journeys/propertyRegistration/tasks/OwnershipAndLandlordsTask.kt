package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.DuplicableTaskWithDependencies
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.isComplete
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.OwnershipAndLandlordsState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.OwnershipTypeStep
import uk.gov.communities.prsdb.webapp.journeys.shared.inviteJointLandlord.InviteJointLandlordsTaskDependencies

@JourneyFrameworkComponent
class OwnershipAndLandlordsTask(
    journeyStateService: JourneyStateService,
    override val ownershipTypeStep: OwnershipTypeStep,
    override val jointLandlordsTask: JointLandlordsPropertyRegistrationTask,
) : DuplicableTaskWithDependencies<OwnershipAndLandlordsState, InviteJointLandlordsTaskDependencies>(
        journeyStateService,
    ),
    OwnershipAndLandlordsState {
    override val taskState get() = this

    override fun makeSubJourney(state: OwnershipAndLandlordsState) =
        subJourney(state) {
            step(journey.ownershipTypeStep) {
                routeSegment(OwnershipTypeStep.ROUTE_SEGMENT)
                nextStep { journey.jointLandlordsTask.firstStep }
                savable()
            }
            duplicableTask(journey.jointLandlordsTask) {
                withDependencies { this@OwnershipAndLandlordsTask.dependencies }
                parents { journey.ownershipTypeStep.isComplete() }
                nextStep { exitStep }
                savable()
            }
            exitStep {
                parents { journey.jointLandlordsTask.isComplete() }
            }
        }
}
