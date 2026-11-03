package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.FreeholdersDetailsState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.FreeholderNameFormModel

@JourneyFrameworkComponent
class FreeholderNameStepConfig : AbstractRequestableStepConfig<Complete, FreeholderNameFormModel, FreeholdersDetailsState>() {
    override val formModelClass = FreeholderNameFormModel::class

    override fun getStepSpecificContent(state: FreeholdersDetailsState): Map<String, Any?> =
        mapOf(
            "fieldSetHeading" to "registerProperty.freeholders.name.fieldSetHeading.${jointOrNonJoint(state)}",
            "fieldSetHint" to "registerProperty.freeholders.name.fieldSetHint",
            "submitButtonText" to "forms.buttons.saveAndContinue",
        )

    override fun chooseTemplate(state: FreeholdersDetailsState) = "forms/nameForm"

    override fun mode(state: FreeholdersDetailsState): Complete? = getFormModelFromStateOrNull(state)?.let { Complete.COMPLETE }

    private fun jointOrNonJoint(state: FreeholdersDetailsState) = if (state.isJointFreeholder) "joint" else "nonJoint"
}

@JourneyFrameworkComponent
final class FreeholderNameStep(
    stepConfig: FreeholderNameStepConfig,
) : RequestableStep<Complete, FreeholderNameFormModel, FreeholdersDetailsState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "freeholder-name"
    }
}
