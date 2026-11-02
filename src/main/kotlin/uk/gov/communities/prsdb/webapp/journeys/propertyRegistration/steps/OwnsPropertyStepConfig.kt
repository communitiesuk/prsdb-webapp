package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.shared.YesOrNo
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.OwnsPropertyFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosViewModel

// TODO PDJB-1959: replace this placeholder with the real "Do you own the property?" question
//  (content, validation and persistence). Rendered with the shared forms/todoWithRadios placeholder page.
@JourneyFrameworkComponent
class OwnsPropertyStepConfig : AbstractRequestableStepConfig<YesOrNo, OwnsPropertyFormModel, JourneyState>() {
    override val formModelClass = OwnsPropertyFormModel::class

    override fun getStepSpecificContent(state: JourneyState): Map<String, Any?> =
        mapOf(
            "fieldName" to "ownsProperty",
            "fieldSetHeading" to "registerProperty.ownsProperty.fieldSetHeading",
            "radioOptions" to RadiosViewModel.yesOrNoRadios(),
        )

    override fun chooseTemplate(state: JourneyState) = "forms/todoWithRadios"

    override fun mode(state: JourneyState) = getFormModelFromStateOrNull(state)?.ownsProperty?.let { if (it) YesOrNo.YES else YesOrNo.NO }
}

@JourneyFrameworkComponent
final class OwnsPropertyStep(
    stepConfig: OwnsPropertyStepConfig,
) : RequestableStep<YesOrNo, OwnsPropertyFormModel, JourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "owns-property"
    }
}
