package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractGenericStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.OccupationState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NumberOfBedroomsFormModel

@JourneyFrameworkComponent
class BedroomsStepConfig : AbstractGenericStepConfig<Complete, NumberOfBedroomsFormModel, OccupationState>() {
    override val formModelClass = NumberOfBedroomsFormModel::class

    override fun getStepSpecificContent(state: OccupationState) =
        mapOf(
            "title" to "registerProperty.title",
            "fieldSetHeading" to "forms.numberOfBedrooms.fieldsetHeading",
        )

    override fun chooseTemplate(state: OccupationState): String = "forms/numberOfBedroomsForm"

    override fun mode(state: OccupationState) = getFormModelFromStateOrNull(state)?.numberOfBedrooms?.let { Complete.COMPLETE }
}

@JourneyFrameworkComponent
final class BedroomsStep(
    stepConfig: BedroomsStepConfig,
) : RequestableStep<Complete, NumberOfBedroomsFormModel, OccupationState>(stepConfig)
