package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.FreeholdersDetailsState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EntityNameFormModel

@JourneyFrameworkComponent
class FreeholderNameStepConfig : AbstractRequestableStepConfig<Complete, EntityNameFormModel, FreeholdersDetailsState>() {
    override val formModelClass = EntityNameFormModel::class

    override fun getStepSpecificContent(state: FreeholdersDetailsState): Map<String, Any?> =
        mapOf(
            "fieldSetHeading" to "registerProperty.freeholders.name.fieldSetHeading.${jointOrSingle(state)}",
            "fieldSetHint" to "registerProperty.freeholders.name.fieldSetHint",
            "submitButtonText" to "forms.buttons.saveAndContinue",
        )

    override fun chooseTemplate(state: FreeholdersDetailsState) = "forms/nameForm"

    override fun mode(state: FreeholdersDetailsState): Complete? = getFormModelFromStateOrNull(state)?.let { Complete.COMPLETE }

    private fun jointOrSingle(state: FreeholdersDetailsState) = if (state.isJointFreeholder) "joint" else "single"
}

@JourneyFrameworkComponent
final class FreeholderNameStep(
    stepConfig: FreeholderNameStepConfig,
) : RequestableStep<Complete, EntityNameFormModel, FreeholdersDetailsState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "freeholder-name"
    }
}
