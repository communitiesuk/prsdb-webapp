package uk.gov.communities.prsdb.webapp.journeys.acceptOrRejectJointLandlordInvitation.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.database.repository.JointLandlordInvitationRepository
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator.RedirectingStepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.acceptOrRejectJointLandlordInvitation.AcceptOrRejectJointLandlordInvitationJourneyState
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel
import uk.gov.communities.prsdb.webapp.services.JointLandlordInvitationService
import java.util.UUID

@JourneyFrameworkComponent
class ValidateTokenStepConfig(
    private val jointLandlordInvitationRepository: JointLandlordInvitationRepository,
    private val invitationService: JointLandlordInvitationService,
) : AbstractRequestableStepConfig<TokenValidationResult, NoInputFormModel, AcceptOrRejectJointLandlordInvitationJourneyState>() {
    override val formModelClass = NoInputFormModel::class

    override fun getStepLifecycleOrchestrator(journeyStep: JourneyStep<*, *, *>) = RedirectingStepLifecycleOrchestrator(journeyStep)

    override fun getStepSpecificContent(state: AcceptOrRejectJointLandlordInvitationJourneyState): Map<String, Any?> = emptyMap()

    override fun chooseTemplate(state: AcceptOrRejectJointLandlordInvitationJourneyState): String = ""

    override fun mode(state: AcceptOrRejectJointLandlordInvitationJourneyState): TokenValidationResult? =
        when (state.tokenIsValid) {
            true -> TokenValidationResult.VALID
            false -> TokenValidationResult.INVALID
            null -> null
        }

    override fun afterStepIsReached(state: AcceptOrRejectJointLandlordInvitationJourneyState) {
        val token = state.invitationToken

        state.tokenIsValid = isTokenValid(token)
    }

    private fun isTokenValid(token: String): Boolean {
        val tokenUuid =
            try {
                UUID.fromString(token)
            } catch (_: IllegalArgumentException) {
                return false
            }

        val invitation = jointLandlordInvitationRepository.findByToken(tokenUuid) ?: return false

        return !(invitationService.getInvitationHasExpired(invitation))
    }
}

@JourneyFrameworkComponent
final class ValidateTokenStep(
    stepConfig: ValidateTokenStepConfig,
) : JourneyStep.RequestableStep<TokenValidationResult, NoInputFormModel, AcceptOrRejectJointLandlordInvitationJourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "validate-token"
    }
}

enum class TokenValidationResult {
    VALID,
    INVALID,
}
