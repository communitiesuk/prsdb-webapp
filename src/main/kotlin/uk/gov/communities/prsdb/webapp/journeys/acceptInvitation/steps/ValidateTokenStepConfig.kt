package uk.gov.communities.prsdb.webapp.journeys.acceptInvitation.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.acceptInvitation.AcceptOrganisationalLandlordUserInvitationJourneyState
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.TokenValidityFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosButtonViewModel

enum class TokenValidity {
    VALID,
    INVALID,
}

@JourneyFrameworkComponent("acceptInvitationValidateTokenStepConfig")
class ValidateTokenStepConfig :
    AbstractRequestableStepConfig<TokenValidity, TokenValidityFormModel, AcceptOrganisationalLandlordUserInvitationJourneyState>() {
    override val formModelClass = TokenValidityFormModel::class

    // TODO PDJB-1822: Validate token step (stub with radios)
    override fun getStepSpecificContent(state: AcceptOrganisationalLandlordUserInvitationJourneyState): Map<String, Any?> =
        mapOf(
            "fieldName" to "tokenValidity",
            "fieldSetHeading" to "acceptOrganisationInvitation.validateToken.fieldSetHeading",
            "radioOptions" to
                listOf(
                    RadiosButtonViewModel(
                        value = TokenValidity.VALID,
                        labelMsgKey = "acceptOrganisationInvitation.validateToken.radios.option.valid",
                    ),
                    RadiosButtonViewModel(
                        value = TokenValidity.INVALID,
                        labelMsgKey = "acceptOrganisationInvitation.validateToken.radios.option.invalid",
                    ),
                ),
        )

    override fun chooseTemplate(state: AcceptOrganisationalLandlordUserInvitationJourneyState) = "forms/todoWithRadios"

    override fun mode(state: AcceptOrganisationalLandlordUserInvitationJourneyState) = getFormModelFromStateOrNull(state)?.tokenValidity
}

@JourneyFrameworkComponent("acceptInvitationValidateTokenStep")
final class ValidateTokenStep(
    stepConfig: ValidateTokenStepConfig,
) : RequestableStep<TokenValidity, TokenValidityFormModel, AcceptOrganisationalLandlordUserInvitationJourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "validate-token"
    }
}
