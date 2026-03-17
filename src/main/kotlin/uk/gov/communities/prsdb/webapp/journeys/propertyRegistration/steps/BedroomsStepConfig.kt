package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.BedroomsState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NumberOfBedroomsFormModel

@JourneyFrameworkComponent
class BedroomsStepConfig : AbstractRequestableStepConfig<Complete, NumberOfBedroomsFormModel, BedroomsState>() {
    override val formModelClass = NumberOfBedroomsFormModel::class

    override fun getStepSpecificContent(state: BedroomsState) =
        mapOf(
            "heading" to "forms.numberOfBedrooms.heading",
            "fieldSetHeading" to "forms.numberOfBedrooms.fieldsetHeading",
            "submitButtonText" to "forms.buttons.saveAndContinue",
        )

    override fun chooseTemplate(state: BedroomsState): String = "forms/numberOfBedroomsForm"

    override fun mode(state: BedroomsState) = getFormModelFromStateOrNull(state)?.numberOfBedrooms?.let { Complete.COMPLETE }
}

@JourneyFrameworkComponent
final class BedroomsStep(
    stepConfig: BedroomsStepConfig,
) : RequestableStep<Complete, NumberOfBedroomsFormModel, BedroomsState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "number-of-bedrooms"
    }
}
