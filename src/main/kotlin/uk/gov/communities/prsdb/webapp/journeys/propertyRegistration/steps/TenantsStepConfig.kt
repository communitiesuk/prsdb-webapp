package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import org.springframework.context.annotation.Scope
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebComponent
import uk.gov.communities.prsdb.webapp.forms.PageData
import uk.gov.communities.prsdb.webapp.journeys.AbstractGenericStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.OccupationState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NumberOfPeopleFormModel

@Scope("prototype")
@PrsdbWebComponent
class TenantsStepConfig : AbstractGenericStepConfig<Complete, NumberOfPeopleFormModel, OccupationState>() {
    override val formModelClass = NumberOfPeopleFormModel::class

    override fun getStepSpecificContent(state: OccupationState) =
        mapOf(
            "title" to "registerProperty.title",
            "fieldSetHeading" to "forms.numberOfPeople.fieldSetHeading",
            "fieldSetHint" to "forms.numberOfPeople.fieldSetHint",
            "label" to "forms.numberOfPeople.label",
        )

    override fun chooseTemplate(state: OccupationState): String = "forms/numberOfPeopleForm"

    override fun mode(state: OccupationState) = getFormModelFromStateOrNull(state)?.numberOfPeople?.let { Complete.COMPLETE }

    override fun beforeValidateSubmittedData(
        formData: PageData,
        state: OccupationState,
    ): PageData {
        super.beforeValidateSubmittedData(formData, state)

        return formData + (NumberOfPeopleFormModel::numberOfHouseholds.name to state.households?.formModelOrNull?.numberOfHouseholds)
    }
}

@Scope("prototype")
@PrsdbWebComponent
final class TenantsStep(
    stepConfig: TenantsStepConfig,
) : RequestableStep<Complete, NumberOfPeopleFormModel, OccupationState>(stepConfig)
