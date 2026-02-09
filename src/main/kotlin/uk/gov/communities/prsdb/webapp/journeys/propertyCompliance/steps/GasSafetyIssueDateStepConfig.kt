package uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.states.GasSafetyState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.TodayOrPastDateFormModel

@JourneyFrameworkComponent
class GasSafetyIssueDateStepConfig : AbstractRequestableStepConfig<Complete, TodayOrPastDateFormModel, GasSafetyState>() {
    override val formModelClass = TodayOrPastDateFormModel::class

    override fun getStepSpecificContent(state: GasSafetyState): Map<String, Any?> =
        mapOf(
            "title" to "propertyCompliance.title",
            "fieldSetHeading" to "forms.todayOrPastDate.gasSafetyCert.fieldSetHeading",
            "fieldSetHint" to "forms.todayOrPastDate.gasSafetyCert.fieldSetHint",
            "submitButtonText" to "forms.buttons.saveAndContinue",
        )

    override fun chooseTemplate(state: GasSafetyState): String = "forms/dateForm"

    override fun mode(state: GasSafetyState) = getFormModelFromStateOrNull(state)?.toLocalDateOrNull()?.let { Complete.COMPLETE }
}

@JourneyFrameworkComponent
final class GasSafetyIssueDateStep(
    stepConfig: GasSafetyIssueDateStepConfig,
) : RequestableStep<Complete, TodayOrPastDateFormModel, GasSafetyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "gas-safety-certificate-issue-date"
    }
}
