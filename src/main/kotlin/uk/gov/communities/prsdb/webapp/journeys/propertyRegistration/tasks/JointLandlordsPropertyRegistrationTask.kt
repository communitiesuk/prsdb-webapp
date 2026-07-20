package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.constants.enums.TaskStatus
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.DuplicableTaskWithDependencies
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.OrParents
import uk.gov.communities.prsdb.webapp.journeys.hasOutcome
import uk.gov.communities.prsdb.webapp.journeys.isComplete
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.AnyLandlordsInvited
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.InviteJointLandlordPropertyRegistrationState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasAnyJointLandlordsInvitedStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasJointLandlordsStep
import uk.gov.communities.prsdb.webapp.journeys.shared.YesOrNo
import uk.gov.communities.prsdb.webapp.journeys.shared.inviteJointLandlord.InviteJointLandlordsTask
import uk.gov.communities.prsdb.webapp.journeys.shared.inviteJointLandlord.InviteJointLandlordsTaskDependencies

@JourneyFrameworkComponent
class JointLandlordsPropertyRegistrationTask(
    journeyStateService: JourneyStateService,
    private val hasAnyJointLandlordsInvitedStep: HasAnyJointLandlordsInvitedStep,
    override val hasJointLandlordsStep: HasJointLandlordsStep,
    override val inviteJointLandlordsTask: InviteJointLandlordsTask,
) : DuplicableTaskWithDependencies<InviteJointLandlordPropertyRegistrationState, InviteJointLandlordsTaskDependencies>(
        journeyStateService,
    ),
    InviteJointLandlordPropertyRegistrationState {
    override val taskState get() = this

    override val invitedJointLandlords: List<String>
        get() = inviteJointLandlordsTask.invitedJointLandlords

    override fun makeSubJourney(state: InviteJointLandlordPropertyRegistrationState) =
        subJourney(state) {
            taskStatus {
                when {
                    exitStep.isStepReachable -> TaskStatus.COMPLETED
                    journey.hasJointLandlordsStep.outcome != null -> TaskStatus.IN_PROGRESS
                    journey.inviteJointLandlordsTask.invitedJointLandlords.isNotEmpty() -> TaskStatus.IN_PROGRESS
                    firstStep.isStepReachable -> TaskStatus.NOT_STARTED
                    else -> TaskStatus.CANNOT_START
                }
            }
            step(hasAnyJointLandlordsInvitedStep) {
                nextStep { mode ->
                    when (mode) {
                        AnyLandlordsInvited.NO_LANDLORDS -> journey.hasJointLandlordsStep
                        AnyLandlordsInvited.SOME_LANDLORDS -> journey.inviteJointLandlordsTask.firstStep
                    }
                }
            }
            step(journey.hasJointLandlordsStep) {
                routeSegment(HasJointLandlordsStep.ROUTE_SEGMENT)
                parents { hasAnyJointLandlordsInvitedStep.hasOutcome(AnyLandlordsInvited.NO_LANDLORDS) }
                nextStep { mode ->
                    when (mode) {
                        YesOrNo.YES -> journey.inviteJointLandlordsTask.firstStep
                        YesOrNo.NO -> exitStep
                    }
                }
                savable()
            }
            duplicableTask(journey.inviteJointLandlordsTask) {
                withDependencies { this@JointLandlordsPropertyRegistrationTask.dependencies }
                parents {
                    OrParents(
                        journey.hasJointLandlordsStep.hasOutcome(YesOrNo.YES),
                        hasAnyJointLandlordsInvitedStep.hasOutcome(AnyLandlordsInvited.SOME_LANDLORDS),
                    )
                }
                nextDestination { _ ->
                    if (journey.inviteJointLandlordsTask.invitedJointLandlords.isEmpty()) {
                        Destination(journey.hasJointLandlordsStep)
                    } else {
                        Destination(exitStep)
                    }
                }
            }
            exitStep {
                parents {
                    OrParents(
                        journey.inviteJointLandlordsTask.isComplete(),
                        journey.hasJointLandlordsStep.hasOutcome(YesOrNo.NO),
                    )
                }
            }
        }
}
