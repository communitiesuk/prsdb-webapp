package uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.constants.CHECK_GAS_SAFE_REGISTER_URL
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.GasSafeEngineerNumFormModel

@JourneyFrameworkComponent
class GasSafetyEngineerNumberStepConfig : AbstractRequestableStepConfig<Complete, GasSafeEngineerNumFormModel, JourneyState>() {
    override val formModelClass = GasSafeEngineerNumFormModel::class

    override fun getStepSpecificContent(state: JourneyState): Map<String, Any?> =
        mapOf(
            "fieldSetHeading" to "forms.gasSafeEngineerNum.fieldSetHeading",
            "fieldSetHint" to "forms.gasSafeEngineerNum.fieldSetHint",
            "gasSafeRegisterURL" to CHECK_GAS_SAFE_REGISTER_URL,
        )

    override fun chooseTemplate(state: JourneyState): String = "forms/gasSafeEngineerNumForm"

    override fun mode(state: JourneyState) = getFormModelFromStateOrNull(state)?.engineerNumber?.let { Complete.COMPLETE }
}

@JourneyFrameworkComponent
final class GasSafetyEngineerNumberStep(
    stepConfig: GasSafetyEngineerNumberStepConfig,
) : RequestableStep<Complete, GasSafeEngineerNumFormModel, JourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "gas-safety-engineer-number"
    }
}
