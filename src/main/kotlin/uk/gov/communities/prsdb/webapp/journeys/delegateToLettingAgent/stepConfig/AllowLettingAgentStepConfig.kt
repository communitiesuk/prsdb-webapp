package uk.gov.communities.prsdb.webapp.journeys.delegateToLettingAgent.stepConfig

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.FormData
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.delegateToLettingAgent.DelegateToLettingAgentJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.AllowLettingAgentEmailFormModel
import uk.gov.communities.prsdb.webapp.services.UserToLandlordService

@JourneyFrameworkComponent("delegateToLettingAgentAllowLettingAgentStepConfig")
class AllowLettingAgentStepConfig(
    private val userToLandlordService: UserToLandlordService,
) : AbstractRequestableStepConfig<Complete, AllowLettingAgentEmailFormModel, DelegateToLettingAgentJourneyState>() {
    override val formModelClass = AllowLettingAgentEmailFormModel::class

    override fun getStepSpecificContent(state: DelegateToLettingAgentJourneyState) = emptyMap<String, Any?>()

    override fun chooseTemplate(state: DelegateToLettingAgentJourneyState) = "forms/allowLettingAgentForm"

    override fun mode(state: DelegateToLettingAgentJourneyState): Complete? = getFormModelFromStateOrNull(state)?.let { Complete.COMPLETE }

    override fun enrichSubmittedDataBeforeValidation(
        state: DelegateToLettingAgentJourneyState,
        formData: FormData,
    ): FormData =
        super.enrichSubmittedDataBeforeValidation(state, formData) +
            (AllowLettingAgentEmailFormModel::landlordEmail.name to userToLandlordService.getCurrentLandlordForUser().email)
}

@JourneyFrameworkComponent("delegateToLettingAgentAllowLettingAgentStep")
final class AllowLettingAgentStep(
    stepConfig: AllowLettingAgentStepConfig,
) : RequestableStep<Complete, AllowLettingAgentEmailFormModel, DelegateToLettingAgentJourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "allow-letting-agent"
    }
}
