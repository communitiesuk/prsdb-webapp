package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.shared.YesOrNo
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.IsEpcRequiredFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosViewModel.Companion.yesOrNoRadios

// TODO PDJB-657 - check the implementation of this step
@JourneyFrameworkComponent
class IsEpcRequiredStepConfig : AbstractRequestableStepConfig<YesOrNo, IsEpcRequiredFormModel, JourneyState>() {
    override val formModelClass = IsEpcRequiredFormModel::class

    override fun getStepSpecificContent(state: JourneyState) =
        mapOf(
            "fieldSetHeading" to "forms.isEpcRequired.fieldSetHeading",
            "radioOptions" to yesOrNoRadios(),
        )

    override fun chooseTemplate(state: JourneyState) = "forms/isEpcRequiredForm"

    override fun mode(state: JourneyState) =
        getFormModelFromStateOrNull(state)?.let {
            when (it.epcRequired) {
                true -> YesOrNo.YES
                false -> YesOrNo.NO
                null -> null
            }
        }
}

@JourneyFrameworkComponent
final class IsEpcRequiredStep(
    stepConfig: IsEpcRequiredStepConfig,
) : RequestableStep<YesOrNo, IsEpcRequiredFormModel, JourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "is-epc-required"
    }
}
