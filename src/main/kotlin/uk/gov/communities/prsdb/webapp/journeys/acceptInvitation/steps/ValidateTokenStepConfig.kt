package uk.gov.communities.prsdb.webapp.journeys.acceptInvitation.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.acceptInvitation.AcceptInvitationJourneyState
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.TokenValidityFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosButtonViewModel

enum class TokenValidity {
    VALID,
    INVALID,
}

@JourneyFrameworkComponent("acceptInvitationValidateTokenStepConfig")
class ValidateTokenStepConfig :
    AbstractRequestableStepConfig<TokenValidity, TokenValidityFormModel, AcceptInvitationJourneyState>() {
    override val formModelClass = TokenValidityFormModel::class

    // TODO PDJB-1822: Validate token step (stub with radios)
    override fun getStepSpecificContent(state: AcceptInvitationJourneyState): Map<String, Any?> =
        mapOf(
            "fieldName" to "tokenValidity",
            "fieldSetHeading" to "acceptInvitation.validateToken.fieldSetHeading",
            "radioOptions" to
                listOf(
                    RadiosButtonViewModel(
                        value = TokenValidity.VALID,
                        labelMsgKey = "acceptInvitation.validateToken.radios.option.valid",
                    ),
                    RadiosButtonViewModel(
                        value = TokenValidity.INVALID,
                        labelMsgKey = "acceptInvitation.validateToken.radios.option.invalid",
                    ),
                ),
        )

    override fun chooseTemplate(state: AcceptInvitationJourneyState) = "forms/todoWithRadios"

    override fun mode(state: AcceptInvitationJourneyState) = getFormModelFromStateOrNull(state)?.tokenValidity
}

@JourneyFrameworkComponent("acceptInvitationValidateTokenStep")
final class ValidateTokenStep(
    stepConfig: ValidateTokenStepConfig,
) : RequestableStep<TokenValidity, TokenValidityFormModel, AcceptInvitationJourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "validate-token"
    }
}
