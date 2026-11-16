package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.shared.YesOrNo
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.OwnsPropertyFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosViewModel

@JourneyFrameworkComponent
class OwnsPropertyStepConfig : AbstractRequestableStepConfig<YesOrNo, OwnsPropertyFormModel, JourneyState>() {
    override val formModelClass = OwnsPropertyFormModel::class

    override fun getStepSpecificContent(state: JourneyState): Map<String, Any?> =
        mapOf(
            "fieldSetHeading" to "registerProperty.ownsProperty.fieldSetHeading",
            "fieldSetHint" to "registerProperty.ownsProperty.fieldSetHint",
            "radioOptions" to
                RadiosViewModel.yesOrNoRadios(
                    yesHint = "registerProperty.ownsProperty.radios.option.yes.hint",
                    noHint = "registerProperty.ownsProperty.radios.option.no.hint",
                ),
        )

    override fun chooseTemplate(state: JourneyState) = "forms/ownsPropertyForm"

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
