package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel

// TODO PDJB-665: Implement EPC Expiry Check page
@JourneyFrameworkComponent("propertyRegistrationEpcExpiryCheckStepConfig")
class EpcExpiryCheckStepConfig : AbstractRequestableStepConfig<EpcExpiryCheckMode, NoInputFormModel, JourneyState>() {
    override val formModelClass = NoInputFormModel::class

    override fun getStepSpecificContent(state: JourneyState) = mapOf("todoComment" to "TODO PDJB-665: Implement EPC Expiry Check page")

    override fun chooseTemplate(state: JourneyState) = "forms/todo"

    // TODO PDJB-665: Return correct mode based on user choice (EPC was in date / not in date when tenancy began)
    override fun mode(state: JourneyState) = getFormModelFromStateOrNull(state)?.let { EpcExpiryCheckMode.IN_DATE }
}

@JourneyFrameworkComponent("propertyRegistrationEpcExpiryCheckStep")
final class EpcExpiryCheckStep(
    stepConfig: EpcExpiryCheckStepConfig,
) : RequestableStep<EpcExpiryCheckMode, NoInputFormModel, JourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "epc-expiry-check"
    }
}

enum class EpcExpiryCheckMode {
    IN_DATE,
    NOT_IN_DATE,
}
