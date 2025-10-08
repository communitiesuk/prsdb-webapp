package uk.gov.communities.prsdb.webapp.forms.newJourneys.backwardsDsl.steps

import org.springframework.context.annotation.Scope
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebComponent
import uk.gov.communities.prsdb.webapp.forms.newJourneys.shared.OccupiedJourneyState
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NumberOfHouseholdsFormModel
import uk.gov.communities.prsdb.webapp.theJourneyFramework.AbstractStep

@Scope("prototype")
@PrsdbWebComponent
class HouseholdStep : AbstractStep<Complete, NumberOfHouseholdsFormModel, OccupiedJourneyState>() {
    override val formModelClazz = NumberOfHouseholdsFormModel::class

    override fun getStepContent(state: OccupiedJourneyState) =
        mapOf(
            "title" to "propertyCompliance.title",
            "fieldSetHeading" to "forms.numberOfHouseholds.fieldSetHeading",
            "label" to "forms.numberOfHouseholds.label",
        )

    override fun chooseTemplate(): String = "forms/numberOfHouseholdsForm"

    override fun mode(state: OccupiedJourneyState) = getFormModelFromState(state)?.numberOfHouseholds?.let { Complete.COMPLETE }
}
