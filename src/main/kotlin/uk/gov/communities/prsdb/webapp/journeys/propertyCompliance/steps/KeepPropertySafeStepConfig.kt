package uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.constants.HOMES_ACT_2018_URL
import uk.gov.communities.prsdb.webapp.constants.HOUSING_HEALTH_AND_SAFETY_RATING_SYSTEM_URL
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.KeepPropertySafeFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.CheckboxViewModel

@JourneyFrameworkComponent
class KeepPropertySafeStepConfig : AbstractRequestableStepConfig<Complete, KeepPropertySafeFormModel, JourneyState>() {
    override val formModelClass = KeepPropertySafeFormModel::class

    override fun getStepSpecificContent(state: JourneyState): Map<String, Any?> =
        mapOf(
            "housingHealthAndSafetyRatingSystemUrl" to HOUSING_HEALTH_AND_SAFETY_RATING_SYSTEM_URL,
            "homesAct2018Url" to HOMES_ACT_2018_URL,
            "options" to
                listOf(
                    CheckboxViewModel(
                        value = "true",
                        labelMsgKey = "forms.landlordResponsibilities.keepPropertySafe.checkbox.label",
                    ),
                ),
        )

    override fun chooseTemplate(state: JourneyState): String = "forms/keepPropertySafeForm"

    override fun mode(state: JourneyState) = getFormModelFromStateOrNull(state)?.agreesToResponsibility?.let { Complete.COMPLETE }
}

@JourneyFrameworkComponent
final class KeepPropertySafeStep(
    stepConfig: KeepPropertySafeStepConfig,
) : RequestableStep<Complete, KeepPropertySafeFormModel, JourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "keep-property-safe"
    }
}
