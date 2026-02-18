package uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.constants.ELECTRICAL_SAFETY_STANDARDS_GUIDE_URL
import uk.gov.communities.prsdb.webapp.constants.ELECTRICAL_SAFETY_STANDARDS_INSPECTION_URL
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel

@JourneyFrameworkComponent
class EicrOutdatedStepConfig : AbstractRequestableStepConfig<Complete, NoInputFormModel, JourneyState>() {
    override val formModelClass = NoInputFormModel::class

    override fun getStepSpecificContent(state: JourneyState): Map<String, Any?> =
        mapOf(
            "electricalSafetyStandardsInspectionUrl" to ELECTRICAL_SAFETY_STANDARDS_INSPECTION_URL,
            "electricalSafetyStandardsGuideUrl" to ELECTRICAL_SAFETY_STANDARDS_GUIDE_URL,
            "submitButtonText" to "forms.buttons.saveAndContinueToEPC",
        )

    override fun chooseTemplate(state: JourneyState): String = "forms/eicrOutdatedForm"

    override fun mode(state: JourneyState) = getFormModelFromStateOrNull(state)?.let { Complete.COMPLETE }
}

@JourneyFrameworkComponent
final class EicrOutdatedStep(
    stepConfig: EicrOutdatedStepConfig,
) : RequestableStep<Complete, NoInputFormModel, JourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "eicr-outdated"
    }
}
