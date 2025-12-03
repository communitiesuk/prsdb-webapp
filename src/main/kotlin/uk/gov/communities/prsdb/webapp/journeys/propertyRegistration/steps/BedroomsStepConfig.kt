package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import org.springframework.context.annotation.Scope
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractGenericStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.example.OccupiedJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NumberOfBedroomsFormModel

@Scope("prototype")
@PrsdbWebComponent
class BedroomsStepConfig : AbstractGenericStepConfig<Complete, NumberOfBedroomsFormModel, OccupiedJourneyState>() {
    override val formModelClass = NumberOfBedroomsFormModel::class

    override fun getStepSpecificContent(state: OccupiedJourneyState) =
        mapOf(
            "fieldSetHeading" to "forms.numberOfBedrooms.fieldSetHeading",
        )

    override fun chooseTemplate(state: OccupiedJourneyState): String = "forms/numberOfBedroomsForm"

    override fun mode(state: OccupiedJourneyState) = getFormModelFromStateOrNull(state)?.numberOfBedrooms?.let { Complete.COMPLETE }
}

@Scope("prototype")
@PrsdbWebComponent
final class BedroomsStep(
    stepConfig: BedroomsStepConfig,
) : RequestableStep<Complete, NumberOfBedroomsFormModel, OccupiedJourneyState>(stepConfig)
