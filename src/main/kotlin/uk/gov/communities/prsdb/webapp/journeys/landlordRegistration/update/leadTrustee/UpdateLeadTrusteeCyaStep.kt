package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.update.leadTrustee

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel

@JourneyFrameworkComponent
class UpdateLeadTrusteeCyaStepConfig : AbstractRequestableStepConfig<Complete, NoInputFormModel, UpdateLeadTrusteeJourneyState>() {
    override val formModelClass = NoInputFormModel::class

    override fun getStepSpecificContent(state: UpdateLeadTrusteeJourneyState) =
        mapOf("todoComment" to "TODO: PDJB-1470 - Lead trustee check your answers page")

    override fun chooseTemplate(state: UpdateLeadTrusteeJourneyState) = "forms/todo"

    override fun mode(state: UpdateLeadTrusteeJourneyState) = getFormModelFromStateOrNull(state)?.let { Complete.COMPLETE }
}

@JourneyFrameworkComponent
final class UpdateLeadTrusteeCyaStep(
    stepConfig: UpdateLeadTrusteeCyaStepConfig,
) : RequestableStep<Complete, NoInputFormModel, UpdateLeadTrusteeJourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "lead-trustee-check-your-answers"
    }
}
