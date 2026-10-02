package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NumberOfBedroomsFormModel

@JourneyFrameworkComponent
class BedroomsStepConfig : AbstractRequestableStepConfig<Complete, NumberOfBedroomsFormModel, JourneyState>() {
    override val formModelClass = NumberOfBedroomsFormModel::class

    override fun getStepSpecificContent(state: JourneyState) =
        mapOf(
            "heading" to "forms.numberOfBedrooms.heading",
            "label" to "forms.numberOfBedrooms.label",
            "submitButtonText" to "forms.buttons.saveAndContinue",
        )

    override fun chooseTemplate(state: JourneyState): String = "forms/numberOfBedroomsForm"

    override fun mode(state: JourneyState) = getFormModelFromStateOrNull(state)?.numberOfBedrooms?.let { Complete.COMPLETE }
}

@JourneyFrameworkComponent
final class BedroomsStep(
    stepConfig: BedroomsStepConfig,
) : RequestableStep<Complete, NumberOfBedroomsFormModel, JourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "number-of-bedrooms"
    }
}
