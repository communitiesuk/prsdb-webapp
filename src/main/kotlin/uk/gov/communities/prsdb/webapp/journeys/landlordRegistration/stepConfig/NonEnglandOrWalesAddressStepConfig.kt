package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel

@JourneyFrameworkComponent
class NonEnglandOrWalesAddressStepConfig : AbstractRequestableStepConfig<Nothing, NoInputFormModel, JourneyState>() {
    override val formModelClass = NoInputFormModel::class

    override fun getStepSpecificContent(state: JourneyState) = emptyMap<String, Any?>()

    override fun chooseTemplate(state: JourneyState) = "forms/nonEnglandOrWalesAddressForm"

    override fun mode(state: JourneyState) = null
}

@JourneyFrameworkComponent
final class NonEnglandOrWalesAddressStep(
    stepConfig: NonEnglandOrWalesAddressStepConfig,
) : RequestableStep<Nothing, NoInputFormModel, JourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "neither-england-nor-wales-address"
    }
}
