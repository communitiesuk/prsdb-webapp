package uk.gov.communities.prsdb.webapp.forms.newJourneys.backwardsDsl.steps

import org.springframework.context.annotation.Scope
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebComponent
import uk.gov.communities.prsdb.webapp.forms.PageData
import uk.gov.communities.prsdb.webapp.forms.newJourneys.shared.OccupiedJourneyState
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NumberOfPeopleFormModel
import uk.gov.communities.prsdb.webapp.theJourneyFramework.AbstractStep

@Scope("prototype")
@PrsdbWebComponent
class TenantsStep : AbstractStep<Complete, NumberOfPeopleFormModel, OccupiedJourneyState>() {
    override val formModelClazz = NumberOfPeopleFormModel::class

    override fun getStepContent(state: OccupiedJourneyState) =
        mapOf(
            "title" to "registerProperty.title",
            "fieldSetHeading" to "forms.numberOfPeople.fieldSetHeading",
            "fieldSetHint" to "forms.numberOfPeople.fieldSetHint",
            "label" to "forms.numberOfPeople.label",
        )

    override fun chooseTemplate(): String = "forms/numberOfPeopleForm"

    override fun mode(state: OccupiedJourneyState) = getFormModelFromState(state)?.numberOfPeople?.let { Complete.COMPLETE }

    override fun beforeValidateSubmittedData(formData: PageData): PageData {
        super.beforeValidateSubmittedData(formData)

        return formData + (NumberOfPeopleFormModel::numberOfHouseholds.name to state.households?.formModel?.numberOfHouseholds)
    }
}
