package uk.gov.communities.prsdb.webapp.journeys.example.steps

import org.springframework.context.annotation.Scope
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebComponent
import uk.gov.communities.prsdb.webapp.forms.PageData
import uk.gov.communities.prsdb.webapp.journeys.AbstractUninitialisableInnerStep
import uk.gov.communities.prsdb.webapp.journeys.Complete
import uk.gov.communities.prsdb.webapp.journeys.example.OccupiedJourneyState
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NumberOfPeopleFormModel

@Scope("prototype")
@PrsdbWebComponent
class TenantsStep : AbstractUninitialisableInnerStep<Complete, NumberOfPeopleFormModel, OccupiedJourneyState>() {
    override val formModelClazz = NumberOfPeopleFormModel::class

    override fun getStepSpecificContent(state: OccupiedJourneyState) =
        mapOf(
            "title" to "registerProperty.title",
            "fieldSetHeading" to "forms.numberOfPeople.fieldSetHeading",
            "fieldSetHint" to "forms.numberOfPeople.fieldSetHint",
            "label" to "forms.numberOfPeople.label",
        )

    override fun chooseTemplate(): String = "forms/numberOfPeopleForm"

    override fun mode(state: OccupiedJourneyState) = getFormModelFromState(state)?.numberOfPeople?.let { Complete.COMPLETE }

    override fun beforeValidateSubmittedData(
        formData: PageData,
        state: OccupiedJourneyState,
    ): PageData {
        super.beforeValidateSubmittedData(formData, state)

        return formData + (NumberOfPeopleFormModel::numberOfHouseholds.name to state.households?.formModel?.numberOfHouseholds)
    }
}
