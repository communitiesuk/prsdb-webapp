package uk.gov.communities.prsdb.webapp.journeys.acceptOrganisationalLandlordInvitation.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.constants.enums.InvitationStatus
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator.RedirectingStepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.acceptOrganisationalLandlordInvitation.AcceptOrganisationalLandlordUserInvitationJourneyState
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel
import uk.gov.communities.prsdb.webapp.services.OrganisationalLandlordInvitationService

enum class TokenValidity {
    VALID,
    INVALID,
}

@JourneyFrameworkComponent("acceptOrganisationalLandlordInvitationValidateTokenStepConfig")
class ValidateTokenStepConfig(
    private val invitationService: OrganisationalLandlordInvitationService,
) : AbstractRequestableStepConfig<TokenValidity, NoInputFormModel, AcceptOrganisationalLandlordUserInvitationJourneyState>() {
    override val formModelClass = NoInputFormModel::class

    override fun getStepLifecycleOrchestrator(journeyStep: JourneyStep<*, *, *>) = RedirectingStepLifecycleOrchestrator(journeyStep)

    override fun getStepSpecificContent(state: AcceptOrganisationalLandlordUserInvitationJourneyState): Map<String, Any?> = emptyMap()

    override fun chooseTemplate(state: AcceptOrganisationalLandlordUserInvitationJourneyState): String = ""

    override fun afterStepIsReached(state: AcceptOrganisationalLandlordUserInvitationJourneyState) {
        val invitation = invitationService.getInvitationForJourneyIdOrNull(state.journeyId)

        val pendingInvitation = invitation?.takeIf { it.status == InvitationStatus.PENDING }

        state.tokenIsValid = pendingInvitation != null
        state.organisationName = pendingInvitation?.organisationalLandlord?.name
    }

    override fun mode(state: AcceptOrganisationalLandlordUserInvitationJourneyState): TokenValidity? =
        when (state.tokenIsValid) {
            true -> TokenValidity.VALID
            false -> TokenValidity.INVALID
            null -> null
        }
}

@JourneyFrameworkComponent("acceptOrganisationalLandlordInvitationValidateTokenStep")
final class ValidateTokenStep(
    stepConfig: ValidateTokenStepConfig,
) : RequestableStep<TokenValidity, NoInputFormModel, AcceptOrganisationalLandlordUserInvitationJourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "validate-token"
    }
}
