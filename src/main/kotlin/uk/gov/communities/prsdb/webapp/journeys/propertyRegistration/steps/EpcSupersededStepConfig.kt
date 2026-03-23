package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel

@JourneyFrameworkComponent("propertyRegistrationEpcSupersededStepConfig")
class EpcSupersededStepConfig : AbstractRequestableStepConfig<Complete, NoInputFormModel, JourneyState>() {
    override val formModelClass = NoInputFormModel::class

    // TODO PDJB-664: Provide actual certificate number from EPC state
    override fun getStepSpecificContent(state: JourneyState) =
        mapOf(
            "title" to "forms.epcSuperseded.heading",
            "certificateNumber" to "",
        )

    override fun chooseTemplate(state: JourneyState) = "forms/epcSupersededForm"

    override fun mode(state: JourneyState) = getFormModelFromStateOrNull(state)?.let { Complete.COMPLETE }
}

@JourneyFrameworkComponent("propertyRegistrationEpcSupersededStep")
final class EpcSupersededStep(
    stepConfig: EpcSupersededStepConfig,
) : RequestableStep<Complete, NoInputFormModel, JourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "epc-superseded"
    }
}
