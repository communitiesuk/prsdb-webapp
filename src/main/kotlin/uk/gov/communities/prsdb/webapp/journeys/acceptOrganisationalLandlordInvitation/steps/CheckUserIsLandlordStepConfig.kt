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

// This step must stay requestable. Unlike the preceding validate token step, its route is not permitAll'd in
// LandlordSecurityConfig, so it is the first authenticated URL in the journey and is what redirects an anonymous
// invitee to One Login. An InternalStep has no URL, so it would never hit the security filter chain and the invitee
// would reach the rest of the journey unauthenticated. A RedirectingStepLifecycleOrchestrator keeps the URL, and so
// the authentication trigger, while never rendering a page.
@JourneyFrameworkComponent("acceptOrganisationalLandlordInvitationCheckUserIsLandlordStep")
final class CheckUserIsLandlordStep(
    stepConfig: CheckUserIsLandlordStepConfig,
) : RequestableStep<LandlordAccountStatus, NoInputFormModel, AcceptOrganisationalLandlordUserInvitationJourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "check-if-user-is-landlord"
    }
}
