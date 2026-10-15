package uk.gov.communities.prsdb.webapp.journeys.acceptOrganisationalLandlordInvitation.steps

import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.exceptions.NotNullFormModelValueIsNullException.Companion.notNullValue
import uk.gov.communities.prsdb.webapp.journeys.AbstractInternalStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.acceptOrganisationalLandlordInvitation.AcceptOrganisationalLandlordUserInvitationJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.AcceptOrganisationInvitationEmailFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NameFormModel
import uk.gov.communities.prsdb.webapp.services.OrganisationalLandlordInvitationService
import uk.gov.communities.prsdb.webapp.services.SecurityContextService
import uk.gov.communities.prsdb.webapp.services.UserRolesService

@JourneyFrameworkComponent("acceptOrganisationalLandlordInvitationCompleteInvitationAcceptanceStepConfig")
class CompleteInvitationAcceptanceStepConfig(
    private val invitationService: OrganisationalLandlordInvitationService,
    private val userRolesService: UserRolesService,
    private val securityContextService: SecurityContextService,
) : AbstractInternalStepConfig<Complete, AcceptOrganisationalLandlordUserInvitationJourneyState>() {
    override fun mode(state: AcceptOrganisationalLandlordUserInvitationJourneyState): Complete = Complete.COMPLETE

    // The validity and landlord checks ran earlier in the journey, but the invitation may since have been cancelled or
    // accepted elsewhere, and the invitee may since have become a landlord. Re-check before saving: if either is now the
    // case the step is treated as unreachable, redirecting the invitee back to the start of the journey where the
    // validate token and check user is landlord steps re-run, update the journey state, and show the relevant error page
    // rather than saving an invalid organisational landlord user.
    override fun beforeAttemptingToReachStep(state: AcceptOrganisationalLandlordUserInvitationJourneyState): Boolean =
        invitationService.getPendingInvitationForJourneyIdOrNull(state.journeyId) != null &&
            !userRolesService.getUserHasLandlordRole(getPrincipalName())

    override fun afterStepIsReached(state: AcceptOrganisationalLandlordUserInvitationJourneyState) {
        val acceptedToken =
            invitationService.acceptInvitation(
                journeyId = state.journeyId,
                baseUserId = getPrincipalName(),
                name = state.fullNameStep.formModel.notNullValue(NameFormModel::name),
                email =
                    state.emailAddressStep.formModel.notNullValue(
                        AcceptOrganisationInvitationEmailFormModel::emailAddress,
                    ),
            )
        invitationService.clearJourneyIdInvitationTokenPairsForTokenFromSession(acceptedToken)
        securityContextService.refreshContext()
    }

    private fun getPrincipalName(): String = (SecurityContextHolder.getContext().authentication.principal as OidcUser).name

    // TODO: PDJB-1775: If this step becomes the final step in the journey, add a standard end of journey > delete block here
}

@JourneyFrameworkComponent("acceptOrganisationalLandlordInvitationCompleteInvitationAcceptanceStep")
class CompleteInvitationAcceptanceStep(
    stepConfig: CompleteInvitationAcceptanceStepConfig,
) : JourneyStep.InternalStep<Complete, AcceptOrganisationalLandlordUserInvitationJourneyState>(stepConfig)
