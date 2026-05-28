package uk.gov.communities.prsdb.webapp.journeys.acceptOrRejectJointLandlordInvitation.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator.RedirectingStepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.acceptOrRejectJointLandlordInvitation.AcceptOrRejectJointLandlordInvitationJourneyState
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel

@JourneyFrameworkComponent
class CheckUserRoleStepConfig :
    AbstractRequestableStepConfig<UserRoleStatus, NoInputFormModel, AcceptOrRejectJointLandlordInvitationJourneyState>() {
    override val formModelClass = NoInputFormModel::class

    override fun getStepLifecycleOrchestrator(journeyStep: JourneyStep<*, *, *>) = RedirectingStepLifecycleOrchestrator(journeyStep)

    override fun getStepSpecificContent(state: AcceptOrRejectJointLandlordInvitationJourneyState): Map<String, Any?> = emptyMap()

    override fun chooseTemplate(state: AcceptOrRejectJointLandlordInvitationJourneyState): String = ""

    // TODO PDJB-260 - implement check for whether user is already registered as a landlord. Might be on the controller though
    override fun mode(state: AcceptOrRejectJointLandlordInvitationJourneyState) = UserRoleStatus.USER_IS_ALREADY_REGISTERED_AS_LANDLORD
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
