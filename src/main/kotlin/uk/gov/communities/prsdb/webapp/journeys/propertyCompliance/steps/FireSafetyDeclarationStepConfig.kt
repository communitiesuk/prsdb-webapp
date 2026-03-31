package uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.constants.HOUSES_IN_MULTIPLE_OCCUPATION_URL
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.FireSafetyDeclarationFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.CheckboxViewModel

@JourneyFrameworkComponent
class FireSafetyDeclarationStepConfig : AbstractRequestableStepConfig<Complete, FireSafetyDeclarationFormModel, JourneyState>() {
    override val formModelClass = FireSafetyDeclarationFormModel::class

    override fun getStepSpecificContent(state: JourneyState): Map<String, Any?> =
        mapOf(
            "housesInMultipleOccupationUrl" to HOUSES_IN_MULTIPLE_OCCUPATION_URL,
            "options" to
                listOf(
                    CheckboxViewModel(
                        value = true,
                        labelMsgKey = "forms.landlordResponsibilities.fireSafety.checkbox.label",
                    ),
                ),
        )

    override fun chooseTemplate(state: JourneyState): String = "forms/fireSafetyDeclarationForm"

    override fun mode(state: JourneyState) = getFormModelFromStateOrNull(state)?.hasDeclared?.let { Complete.COMPLETE }
}

@JourneyFrameworkComponent
final class FireSafetyDeclarationStep(
    stepConfig: FireSafetyDeclarationStepConfig,
) : RequestableStep<Complete, FireSafetyDeclarationFormModel, JourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "fire-safety-declaration"
    }
}
