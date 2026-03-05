package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.OccupationState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NumberOfBedroomsFormModel

@JourneyFrameworkComponent
class BedroomsStepConfig : AbstractRequestableStepConfig<Complete, NumberOfBedroomsFormModel, OccupationState>() {
    override val formModelClass = NumberOfBedroomsFormModel::class

    override fun getStepSpecificContent(state: OccupationState) =
        mapOf(
            "heading" to "forms.numberOfBedrooms.heading",
            "fieldSetHeading" to "forms.numberOfBedrooms.fieldsetHeading",
        )

    override fun chooseTemplate(state: OccupationState): String = "forms/numberOfBedroomsForm"

    override fun mode(state: OccupationState) = getFormModelFromStateOrNull(state)?.numberOfBedrooms?.let { Complete.COMPLETE }
}

@JourneyFrameworkComponent
final class BedroomsStep(
    stepConfig: BedroomsStepConfig,
) : RequestableStep<Complete, NumberOfBedroomsFormModel, OccupationState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "number-of-bedrooms"
    }
}
