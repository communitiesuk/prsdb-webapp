package uk.gov.communities.prsdb.webapp.journeys.acceptOrRejectJointLandlordInvitation.steps

import jakarta.servlet.http.HttpSession
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.constants.USER_DIRECTED_TO_LANDLORD_REGISTRATION_WHILE_ACCEPTING_JOINT_LANDLORD_INVITATION
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator.RedirectingStepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.acceptOrRejectJointLandlordInvitation.AcceptOrRejectJointLandlordInvitationJourneyState
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel

@JourneyFrameworkComponent
class CheckUserRoleStepConfig(
    private val session: HttpSession,
) : AbstractRequestableStepConfig<UserRoleStatus, NoInputFormModel, AcceptOrRejectJointLandlordInvitationJourneyState>() {
    override val formModelClass = NoInputFormModel::class

    override fun getStepLifecycleOrchestrator(journeyStep: JourneyStep<*, *, *>) = RedirectingStepLifecycleOrchestrator(journeyStep)

    override fun getStepSpecificContent(state: AcceptOrRejectJointLandlordInvitationJourneyState): Map<String, Any?> = emptyMap()

    override fun chooseTemplate(state: AcceptOrRejectJointLandlordInvitationJourneyState): String = ""

    override fun mode(state: AcceptOrRejectJointLandlordInvitationJourneyState) =
        when (
            (
                session.getAttribute(USER_DIRECTED_TO_LANDLORD_REGISTRATION_WHILE_ACCEPTING_JOINT_LANDLORD_INVITATION)
                    as? Pair<String, Boolean>
            )?.second
        ) {
            true -> UserRoleStatus.USER_NOT_REGISTERED_AS_LANDLORD

            false -> UserRoleStatus.USER_IS_ALREADY_REGISTERED_AS_LANDLORD

            null -> throw PrsdbWebException(
                "Session attribute $USER_DIRECTED_TO_LANDLORD_REGISTRATION_WHILE_ACCEPTING_JOINT_LANDLORD_INVITATION is missing",
            )
        }
}

@JourneyFrameworkComponent
final class CheckUserRoleStep(
    stepConfig: CheckUserRoleStepConfig,
) : JourneyStep.RequestableStep<UserRoleStatus, NoInputFormModel, AcceptOrRejectJointLandlordInvitationJourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "check-user-role"
    }
}

enum class UserRoleStatus {
    USER_IS_ALREADY_REGISTERED_AS_LANDLORD,
    USER_NOT_REGISTERED_AS_LANDLORD,
}
