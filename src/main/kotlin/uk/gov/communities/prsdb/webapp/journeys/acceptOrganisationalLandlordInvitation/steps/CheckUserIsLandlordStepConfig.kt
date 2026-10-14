package uk.gov.communities.prsdb.webapp.journeys.acceptOrganisationalLandlordInvitation.steps

import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator.RedirectingStepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.acceptOrganisationalLandlordInvitation.AcceptOrganisationalLandlordUserInvitationJourneyState
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel
import uk.gov.communities.prsdb.webapp.services.UserRolesService

enum class LandlordAccountStatus {
    ALREADY_A_LANDLORD,
    NOT_A_LANDLORD,
}

@JourneyFrameworkComponent("acceptOrganisationalLandlordInvitationCheckUserIsLandlordStepConfig")
class CheckUserIsLandlordStepConfig(
    private val userRolesService: UserRolesService,
) : AbstractRequestableStepConfig<
        LandlordAccountStatus,
        NoInputFormModel,
        AcceptOrganisationalLandlordUserInvitationJourneyState,
        >() {
    override val formModelClass = NoInputFormModel::class

    override fun getStepLifecycleOrchestrator(journeyStep: JourneyStep<*, *, *>) = RedirectingStepLifecycleOrchestrator(journeyStep)

    override fun getStepSpecificContent(state: AcceptOrganisationalLandlordUserInvitationJourneyState): Map<String, Any?> = emptyMap()

    override fun chooseTemplate(state: AcceptOrganisationalLandlordUserInvitationJourneyState): String = ""

    override fun afterStepIsReached(state: AcceptOrganisationalLandlordUserInvitationJourneyState) {
        val principal = SecurityContextHolder.getContext().authentication.principal as OidcUser
        state.userIsLandlord = userRolesService.getUserHasLandlordRole(principal.name)
    }

    override fun mode(state: AcceptOrganisationalLandlordUserInvitationJourneyState): LandlordAccountStatus? =
        when (state.userIsLandlord) {
            true -> LandlordAccountStatus.ALREADY_A_LANDLORD
            false -> LandlordAccountStatus.NOT_A_LANDLORD
            null -> null
        }
}

// This step stays requestable because its route, unlike the preceding validate token step's, is not permitAll'd, making
// it the first authenticated URL in the journey and so what redirects an anonymous invitee to One Login.
@JourneyFrameworkComponent("acceptOrganisationalLandlordInvitationCheckUserIsLandlordStep")
final class CheckUserIsLandlordStep(
    stepConfig: CheckUserIsLandlordStepConfig,
) : RequestableStep<LandlordAccountStatus, NoInputFormModel, AcceptOrganisationalLandlordUserInvitationJourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "check-if-user-is-landlord"
    }
}
