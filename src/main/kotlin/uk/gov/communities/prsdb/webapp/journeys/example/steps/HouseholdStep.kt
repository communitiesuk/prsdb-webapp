package uk.gov.communities.prsdb.webapp.journeys.example.steps

import org.springframework.context.annotation.Scope
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractUninitialisableInnerStep
import uk.gov.communities.prsdb.webapp.journeys.example.OccupiedJourneyState
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NumberOfHouseholdsFormModel

@Scope("prototype")
@PrsdbWebComponent
class HouseholdStep : AbstractUninitialisableInnerStep<Complete, NumberOfHouseholdsFormModel, OccupiedJourneyState>() {
    override val formModelClazz = NumberOfHouseholdsFormModel::class

    override fun getStepSpecificContent(state: OccupiedJourneyState) =
        mapOf(
            "title" to "propertyCompliance.title",
            "fieldSetHeading" to "forms.numberOfHouseholds.fieldSetHeading",
            "label" to "forms.numberOfHouseholds.label",
        )

    override fun chooseTemplate(state: OccupiedJourneyState): String = "forms/numberOfHouseholdsForm"

    override fun mode(state: OccupiedJourneyState) = getFormModelFromState(state)?.numberOfHouseholds?.let { Complete.COMPLETE }
}
