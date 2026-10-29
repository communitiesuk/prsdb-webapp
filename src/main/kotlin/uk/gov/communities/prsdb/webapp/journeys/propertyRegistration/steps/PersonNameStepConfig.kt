package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import org.springframework.validation.BindingResult
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.FreeholderNameFormModel

@JourneyFrameworkComponent
class PersonNameStepConfig : AbstractRequestableStepConfig<Complete, FreeholderNameFormModel, JourneyState>() {
    override val formModelClass = FreeholderNameFormModel::class

    override fun getStepSpecificContent(state: JourneyState): Map<String, Any?> =
        mapOf(
            "fieldSetHeading" to "registerProperty.freeholders.name.fieldSetHeading.${jointOrSingle(state)}",
            "fieldSetHint" to "registerProperty.freeholders.name.fieldSetHint",
            "submitButtonText" to "forms.buttons.saveAndContinue",
        )

    override fun afterPrimaryValidation(
        state: JourneyState,
        bindingResult: BindingResult,
    ) {
        if (bindingResult.getFormModel().name.isNullOrBlank()) {
            bindingResult.rejectValueWithMessageKey(
                FreeholderNameFormModel::name.name,
                "registerProperty.freeholders.name.error.missing.${jointOrSingle(state)}",
            )
        }
    }

    override fun chooseTemplate(state: JourneyState) = "forms/nameForm"

    override fun mode(state: JourneyState): Complete? = getFormModelFromStateOrNull(state)?.let { Complete.COMPLETE }

    private fun jointOrSingle(state: JourneyState) = if (isJointFreeholder(state)) "joint" else "single"

    // TODO PDJB-1959/PDJB-1960: return true unless the user is a leaseholder or does not own the property,
    //  once the owns-property and ownership-kind answers are captured
    @Suppress("UNUSED_PARAMETER")
    private fun isJointFreeholder(state: JourneyState): Boolean = false
}

@JourneyFrameworkComponent
final class PersonNameStep(
    stepConfig: PersonNameStepConfig,
) : RequestableStep<Complete, FreeholderNameFormModel, JourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "freeholder-name"
    }
}
