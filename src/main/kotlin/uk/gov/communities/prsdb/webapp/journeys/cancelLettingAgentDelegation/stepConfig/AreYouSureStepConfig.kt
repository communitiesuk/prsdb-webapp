package uk.gov.communities.prsdb.webapp.journeys.cancelLettingAgentDelegation.stepConfig

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.cancelLettingAgentDelegation.CancelLettingAgentDelegationJourneyState
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.CancelLettingAgentDelegationAreYouSureFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosViewModel
import uk.gov.communities.prsdb.webapp.services.LettingAgentAccessService

@JourneyFrameworkComponent("cancelLettingAgentDelegationAreYouSureStepConfig")
class AreYouSureStepConfig(
    private val lettingAgentAccessService: LettingAgentAccessService,
) : AbstractRequestableStepConfig<
        AreYouSureMode,
        CancelLettingAgentDelegationAreYouSureFormModel,
        CancelLettingAgentDelegationJourneyState,
        >() {
    override val formModelClass = CancelLettingAgentDelegationAreYouSureFormModel::class

    override fun getStepSpecificContent(state: CancelLettingAgentDelegationJourneyState): Map<String, Any?> =
        mapOf(
            "radioOptions" to RadiosViewModel.yesOrNoRadios(),
            "fieldSetHeading" to "cancelLettingAgentDelegation.areYouSure.fieldSetHeading",
            "optionalFieldSetHeadingParam" to
                lettingAgentAccessService.getInvitationByPropertyOwnershipId(state.propertyOwnershipId)?.invitedEmail,
            "fieldSetHint" to "cancelLettingAgentDelegation.areYouSure.fieldSetHint",
            "submitButtonTextKey" to "cancelLettingAgentDelegation.areYouSure.confirmButton",
            "showCancelLink" to false,
        )

    override fun chooseTemplate(state: CancelLettingAgentDelegationJourneyState) = "forms/areYouSureForm"

    override fun mode(state: CancelLettingAgentDelegationJourneyState): AreYouSureMode? =
        getFormModelFromStateOrNull(state)?.wantsToProceed?.let {
            if (it) AreYouSureMode.WANTS_TO_PROCEED else AreYouSureMode.DOES_NOT_WANT_TO_PROCEED
        }
}

@JourneyFrameworkComponent("cancelLettingAgentDelegationAreYouSureStep")
final class AreYouSureStep(
    stepConfig: AreYouSureStepConfig,
) : RequestableStep<
        AreYouSureMode,
        CancelLettingAgentDelegationAreYouSureFormModel,
        CancelLettingAgentDelegationJourneyState,
        >(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "are-you-sure"
    }
}

enum class AreYouSureMode {
    WANTS_TO_PROCEED,
    DOES_NOT_WANT_TO_PROCEED,
}
