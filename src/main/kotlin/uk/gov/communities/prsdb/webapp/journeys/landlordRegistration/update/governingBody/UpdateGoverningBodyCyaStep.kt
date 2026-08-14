package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.update.governingBody

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel

@JourneyFrameworkComponent
class UpdateGoverningBodyCyaStepConfig : AbstractRequestableStepConfig<Complete, NoInputFormModel, UpdateGoverningBodyJourneyState>() {
    override val formModelClass = NoInputFormModel::class

    override fun getStepSpecificContent(state: UpdateGoverningBodyJourneyState) =
        mapOf("todoComment" to "TODO: PDJB-1472 - Governing body check your answers page")

    override fun chooseTemplate(state: UpdateGoverningBodyJourneyState) = "forms/todo"

    override fun mode(state: UpdateGoverningBodyJourneyState) = getFormModelFromStateOrNull(state)?.let { Complete.COMPLETE }
}

@JourneyFrameworkComponent
final class UpdateGoverningBodyCyaStep(
    stepConfig: UpdateGoverningBodyCyaStepConfig,
) : RequestableStep<Complete, NoInputFormModel, UpdateGoverningBodyJourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "governing-body-check-your-answers"
    }
}
