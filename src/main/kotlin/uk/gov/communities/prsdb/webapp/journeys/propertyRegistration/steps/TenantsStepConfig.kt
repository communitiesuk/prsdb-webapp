package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import org.springframework.context.annotation.Scope
import org.springframework.validation.BindingResult
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractGenericStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.OccupationState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NewNumberOfPeopleFormModel

@Scope("prototype")
@PrsdbWebComponent
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

    override fun applyAdditionalValidation(
        state: OccupationState,
        bindingResult: BindingResult,
    ) {
        super.applyAdditionalValidation(state, bindingResult)

        if (bindingResult.getFormModel().numberOfPeople.toInt() < state.numberOfHouseholds()) {
            bindingResult.rejectValue(
                NewNumberOfPeopleFormModel::numberOfPeople.name,
                "forms.numberOfPeople.input.error.invalidNumber",
            )
        }
    }

    private fun OccupationState.numberOfHouseholds(): Int =
        this
            .households
            .formModel
            .numberOfHouseholds
            .toInt()
}

@Scope("prototype")
@PrsdbWebComponent
final class TenantsStep(
    stepConfig: TenantsStepConfig,
) : RequestableStep<Complete, NewNumberOfPeopleFormModel, OccupationState>(stepConfig)
