package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.shared.YesOrNo
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.AnyoneInOwnershipChainFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosButtonViewModel

// TODO PDJB-1971: replace this placeholder with the real "is there anyone else in the ownership chain" page.
@JourneyFrameworkComponent
class AnyoneInOwnershipChainStepConfig : AbstractRequestableStepConfig<YesOrNo, AnyoneInOwnershipChainFormModel, JourneyState>() {
    override val formModelClass = AnyoneInOwnershipChainFormModel::class

    override fun getStepSpecificContent(state: JourneyState): Map<String, Any?> =
        mapOf(
            "fieldName" to "anyoneInOwnershipChain",
            "fieldSetHeading" to "registerProperty.superiorLandlords.anyoneInOwnershipChain.fieldSetHeading",
            "radioOptions" to
                listOf(
                    RadiosButtonViewModel(value = YesOrNo.YES, labelMsgKey = "forms.radios.option.yes.label"),
                    RadiosButtonViewModel(value = YesOrNo.NO, labelMsgKey = "forms.radios.option.no.label"),
                ),
        )

    override fun chooseTemplate(state: JourneyState) = "forms/todoWithRadios"

    override fun mode(state: JourneyState) = getFormModelFromStateOrNull(state)?.anyoneInOwnershipChain
}

@JourneyFrameworkComponent
final class AnyoneInOwnershipChainStep(
    stepConfig: AnyoneInOwnershipChainStepConfig,
) : RequestableStep<YesOrNo, AnyoneInOwnershipChainFormModel, JourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "anyone-in-ownership-chain"
    }
}
