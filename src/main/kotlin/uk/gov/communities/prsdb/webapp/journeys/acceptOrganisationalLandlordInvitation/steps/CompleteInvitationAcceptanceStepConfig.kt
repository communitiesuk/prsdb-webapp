package uk.gov.communities.prsdb.webapp.journeys.acceptOrganisationalLandlordInvitation.steps

import org.springframework.security.core.context.SecurityContextHolder
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

@JourneyFrameworkComponent("acceptOrganisationalLandlordInvitationCompleteInvitationAcceptanceStepConfig")
class CompleteInvitationAcceptanceStepConfig(
    private val invitationService: OrganisationalLandlordInvitationService,
    private val securityContextService: SecurityContextService,
) : AbstractInternalStepConfig<Complete, AcceptOrganisationalLandlordUserInvitationJourneyState>() {
    override fun mode(state: AcceptOrganisationalLandlordUserInvitationJourneyState): Complete = Complete.COMPLETE

    override fun afterStepIsReached(state: AcceptOrganisationalLandlordUserInvitationJourneyState) {
        val acceptedToken =
            invitationService.acceptInvitation(
                journeyId = state.journeyId,
                baseUserId = SecurityContextHolder.getContext().authentication.name,
                name = state.fullNameStep.formModel.notNullValue(NameFormModel::name),
                email =
                    state.emailAddressStep.formModel.notNullValue(
                        AcceptOrganisationInvitationEmailFormModel::emailAddress,
                    ),
            )
        invitationService.clearJourneyIdInvitationTokenPairsForTokenFromSession(acceptedToken)
        securityContextService.refreshContext()
    }
}

@JourneyFrameworkComponent("acceptOrganisationalLandlordInvitationCompleteInvitationAcceptanceStep")
class CompleteInvitationAcceptanceStep(
    stepConfig: CompleteInvitationAcceptanceStepConfig,
) : JourneyStep.InternalStep<Complete, AcceptOrganisationalLandlordUserInvitationJourneyState>(stepConfig)
