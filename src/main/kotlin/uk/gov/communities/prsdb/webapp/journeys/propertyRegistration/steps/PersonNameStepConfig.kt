package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.FreeholdersState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.FreeholderNameFormModel

@JourneyFrameworkComponent
class PersonNameStepConfig : AbstractRequestableStepConfig<Complete, FreeholderNameFormModel, FreeholdersState>() {
    override val formModelClass = FreeholderNameFormModel::class

    override fun getStepSpecificContent(state: FreeholdersState): Map<String, Any?> =
        mapOf(
            "fieldSetHeading" to "registerProperty.freeholders.name.fieldSetHeading.${jointOrSingle(state)}",
            "fieldSetHint" to "registerProperty.freeholders.name.fieldSetHint",
            "submitButtonText" to "forms.buttons.saveAndContinue",
        )

    override fun chooseTemplate(state: FreeholdersState) = "forms/nameForm"

    override fun mode(state: FreeholdersState): Complete? = getFormModelFromStateOrNull(state)?.let { Complete.COMPLETE }

    private fun jointOrSingle(state: FreeholdersState) = if (state.isJointFreeholder) "joint" else "single"
}

@JourneyFrameworkComponent
final class PersonNameStep(
    stepConfig: PersonNameStepConfig,
) : RequestableStep<Complete, FreeholderNameFormModel, FreeholdersState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "freeholder-name"
    }
}
