package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import org.springframework.validation.BindingResult
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractGenericStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.OccupationState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NewNumberOfPeopleFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NumberOfHouseholdsFormModel

@JourneyFrameworkComponent
class TenantsStepConfig : AbstractGenericStepConfig<Complete, NewNumberOfPeopleFormModel, OccupationState>() {
    override val formModelClass = NewNumberOfPeopleFormModel::class

    override fun getStepSpecificContent(state: OccupationState) =
        mapOf(
            "title" to "registerProperty.title",
            "fieldSetHeading" to "forms.numberOfPeople.fieldSetHeading",
            "fieldSetHint" to "forms.numberOfPeople.fieldSetHint",
            "label" to "forms.numberOfPeople.label",
        )

    override fun chooseTemplate(state: OccupationState): String = "forms/numberOfPeopleForm"

    override fun mode(state: OccupationState) = getFormModelFromStateOrNull(state)?.numberOfPeople?.let { Complete.COMPLETE }

    override fun afterPrimaryValidation(
        state: OccupationState,
        bindingResult: BindingResult,
    ) {
        super.afterPrimaryValidation(state, bindingResult)
        if (!bindingResult.hasErrors()) {
            bindingResult.validateNumberOfPeople(
                bindingResult.getFormModel(),
                state.households.formModel,
            )
        }
    }

    private fun BindingResult.validateNumberOfPeople(
        numberOfPeopleFormModel: NewNumberOfPeopleFormModel,
        numberOfHouseholdsFormModel: NumberOfHouseholdsFormModel,
    ) {
        if (numberOfPeopleFormModel.numberOfPeople.toInt() < numberOfHouseholdsFormModel.numberOfHouseholds.toInt()) {
            rejectValueWithMessageKey(
                numberOfPeopleFormModel::numberOfPeople.name,
                "forms.numberOfPeople.input.error.invalidNumber",
            )
        }
    }
}

@JourneyFrameworkComponent
final class TenantsStep(
    stepConfig: TenantsStepConfig,
) : RequestableStep<Complete, NewNumberOfPeopleFormModel, OccupationState>(stepConfig)
