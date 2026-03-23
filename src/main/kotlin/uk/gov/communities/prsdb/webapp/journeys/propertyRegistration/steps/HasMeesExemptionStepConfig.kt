package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel

// TODO PDJB-667: Implement Has MEES Exemption page
@JourneyFrameworkComponent
class HasMeesExemptionStepConfig : AbstractRequestableStepConfig<HasMeesExemptionMode, NoInputFormModel, JourneyState>() {
    override val formModelClass = NoInputFormModel::class

    override fun getStepSpecificContent(state: JourneyState) = mapOf("todoComment" to "TODO PDJB-667: Implement Has MEES Exemption page")

    override fun chooseTemplate(state: JourneyState) = "forms/todo"

    // TODO PDJB-667: Return correct mode based on user choice (has MEES exemption / no exemption)
    override fun mode(state: JourneyState) = getFormModelFromStateOrNull(state)?.let { HasMeesExemptionMode.HAS_EXEMPTION }
}

@JourneyFrameworkComponent
final class HasMeesExemptionStep(
    stepConfig: HasMeesExemptionStepConfig,
) : RequestableStep<HasMeesExemptionMode, NoInputFormModel, JourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "has-mees-exemption"
    }
}

enum class HasMeesExemptionMode {
    HAS_EXEMPTION,
    NO_EXEMPTION,
}
