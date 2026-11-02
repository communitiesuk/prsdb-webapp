package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.FreeholdTypeFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosButtonViewModel

// TODO PDJB-1961: replace this placeholder with the real "What kind of freehold do you have?" question
//  (content, validation and persistence). Rendered with the shared forms/todoWithRadios placeholder page.
@JourneyFrameworkComponent
class FreeholdTypeStepConfig : AbstractRequestableStepConfig<FreeholdType, FreeholdTypeFormModel, JourneyState>() {
    override val formModelClass = FreeholdTypeFormModel::class

    override fun getStepSpecificContent(state: JourneyState): Map<String, Any?> =
        mapOf(
            "fieldName" to "freeholdType",
            "fieldSetHeading" to "registerProperty.freeholdType.fieldSetHeading",
            "radioOptions" to FreeholdType.entries.map { RadiosButtonViewModel(value = it) },
        )

    override fun chooseTemplate(state: JourneyState) = "forms/todoWithRadios"

    override fun mode(state: JourneyState) = getFormModelFromStateOrNull(state)?.freeholdType
}

@JourneyFrameworkComponent
final class FreeholdTypeStep(
    stepConfig: FreeholdTypeStepConfig,
) : RequestableStep<FreeholdType, FreeholdTypeFormModel, JourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "freehold-type"
    }
}

enum class FreeholdType {
    SOLE_OWNERSHIP,
    JOINT_TENANTS,
    TENANTS_IN_COMMON,
    COMMONHOLD,
}
