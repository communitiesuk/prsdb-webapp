package uk.gov.communities.prsdb.webapp.journeys.acceptOrganisationalLandlordInvitation.steps

import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.exceptions.NotNullFormModelValueIsNullException.Companion.notNullValue
import uk.gov.communities.prsdb.webapp.journeys.AbstractInternalStepConfig
import uk.gov.communities.prsdb.webapp.journeys.Destination
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

    // Re-check the invitation and account status before saving, as either may have changed while the invitee was in the journey.
    override fun beforeAttemptingToReachStep(state: AcceptOrganisationalLandlordUserInvitationJourneyState): Boolean =
        invitationService.getPendingInvitationForJourneyIdOrNull(state.journeyId) != null &&
            !userRolesService.getUserHasLandlordRole(getPrincipalName())

    override fun afterStepIsReached(state: AcceptOrganisationalLandlordUserInvitationJourneyState) {
        val email = state.emailAddressStep.formModel.notNullValue(AcceptOrganisationInvitationEmailFormModel::emailAddress)
        val acceptedToken =
            invitationService.acceptInvitation(
                journeyId = state.journeyId,
                baseUserId = getPrincipalName(),
                name = state.fullNameStep.formModel.notNullValue(NameFormModel::name),
                email = email,
            )
        invitationService.clearJourneyIdInvitationTokenPairsForTokenFromSession(acceptedToken)
        securityContextService.refreshContext()
        invitationService.storeAcceptedInvitationEmailInSession(email)
    }

    override fun resolveNextDestination(
        state: AcceptOrganisationalLandlordUserInvitationJourneyState,
        defaultDestination: Destination,
    ): Destination {
        state.deleteJourney()
        return defaultDestination
    }

    private fun getPrincipalName(): String = (SecurityContextHolder.getContext().authentication.principal as OidcUser).name
}

@JourneyFrameworkComponent("acceptOrganisationalLandlordInvitationCompleteInvitationAcceptanceStep")
class CompleteInvitationAcceptanceStep(
    stepConfig: CompleteInvitationAcceptanceStepConfig,
) : JourneyStep.InternalStep<Complete, AcceptOrganisationalLandlordUserInvitationJourneyState>(stepConfig)
