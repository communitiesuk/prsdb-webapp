package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import org.springframework.context.annotation.Scope
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractGenericStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.example.OccupiedJourneyState
import uk.gov.communities.prsdb.webapp.journeys.example.steps.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NumberOfHouseholdsFormModel

@Scope("prototype")
@PrsdbWebComponent
class HouseholdStepConfig : AbstractGenericStepConfig<Complete, NumberOfHouseholdsFormModel, OccupiedJourneyState>() {
    override val formModelClass = NumberOfHouseholdsFormModel::class

    override fun getStepSpecificContent(state: OccupiedJourneyState) =
        mapOf(
            "title" to "propertyCompliance.title",
            "fieldSetHeading" to "forms.numberOfHouseholds.fieldSetHeading",
            "label" to "forms.numberOfHouseholds.label",
        )

    override fun chooseTemplate(state: OccupiedJourneyState): String = "forms/numberOfHouseholdsForm"

    override fun mode(state: OccupiedJourneyState) = getFormModelFromStateOrNull(state)?.numberOfHouseholds?.let { Complete.COMPLETE }
}

@Scope("prototype")
@PrsdbWebComponent
final class HouseholdStep(
    stepConfig: HouseholdStepConfig,
) : RequestableStep<Complete, NumberOfHouseholdsFormModel, OccupiedJourneyState>(stepConfig)
