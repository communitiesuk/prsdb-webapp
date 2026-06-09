package uk.gov.communities.prsdb.webapp.journeys.acceptOrRejectJointLandlordInvitation.steps

import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator.RedirectingStepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.acceptOrRejectJointLandlordInvitation.AcceptOrRejectJointLandlordInvitationJourneyState
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel
import uk.gov.communities.prsdb.webapp.services.UserRolesService

@JourneyFrameworkComponent
class CheckUserRoleStepConfig(
    private val userRolesService: UserRolesService,
) : AbstractRequestableStepConfig<UserRoleStatus, NoInputFormModel, AcceptOrRejectJointLandlordInvitationJourneyState>() {
    override val formModelClass = NoInputFormModel::class

    override fun getStepLifecycleOrchestrator(journeyStep: JourneyStep<*, *, *>) = RedirectingStepLifecycleOrchestrator(journeyStep)

    override fun getStepSpecificContent(state: AcceptOrRejectJointLandlordInvitationJourneyState): Map<String, Any?> = emptyMap()

    override fun chooseTemplate(state: AcceptOrRejectJointLandlordInvitationJourneyState): String = ""

    override fun mode(state: AcceptOrRejectJointLandlordInvitationJourneyState) =
        when (state.userIsLandlord) {
            true -> UserRoleStatus.USER_IS_ALREADY_REGISTERED_AS_LANDLORD

            false -> UserRoleStatus.USER_NOT_REGISTERED_AS_LANDLORD

            null -> throw PrsdbWebException(
                "userIsLandlord has not been set on journey state",
            )
        }

    override fun afterStepIsReached(state: AcceptOrRejectJointLandlordInvitationJourneyState) {
        val principal = SecurityContextHolder.getContext().authentication.principal as OidcUser
        state.userIsLandlord = userRolesService.getHasLandlordUserRole(principal.name)
        super.afterStepIsReached(state)
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
