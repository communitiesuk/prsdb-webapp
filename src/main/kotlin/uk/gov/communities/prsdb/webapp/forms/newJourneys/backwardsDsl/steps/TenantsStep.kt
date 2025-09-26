package uk.gov.communities.prsdb.webapp.forms.newJourneys.backwardsDsl.steps

import org.springframework.context.annotation.Scope
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebComponent
import uk.gov.communities.prsdb.webapp.forms.PageData
import uk.gov.communities.prsdb.webapp.forms.newJourneys.Complete
import uk.gov.communities.prsdb.webapp.forms.newJourneys.shared.OccupiedJourneyState
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NumberOfPeopleFormModel

@Scope("prototype")
@PrsdbWebComponent
class TenantsStep : BackwardsDslInitialisableStep<Complete, NumberOfPeopleFormModel, OccupiedJourneyState>() {
    override val formModelClazz = NumberOfPeopleFormModel::class

    override fun getStepContent(state: OccupiedJourneyState) =
        mapOf(
            "title" to "registerProperty.title",
            "fieldSetHeading" to "forms.numberOfPeople.fieldSetHeading",
            "fieldSetHint" to "forms.numberOfPeople.fieldSetHint",
            "label" to "forms.numberOfPeople.label",
        )

    override fun chooseTemplate(state: OccupiedJourneyState): String = "forms/numberOfPeopleForm"

    override fun mode(state: OccupiedJourneyState) = getFormModelFromState(state)?.numberOfPeople?.let { Complete.COMPLETE }

    override fun beforeValidateSubmittedData(
        state: OccupiedJourneyState,
        formData: PageData,
    ): PageData {
        super.beforeValidateSubmittedData(state, formData)

        return formData + (NumberOfPeopleFormModel::numberOfHouseholds.name to state.numberOfHouseholds())
    }
}
