package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.shared.YesOrNo
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.AnyoneInOwnershipChainFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosButtonViewModel

@JourneyFrameworkComponent
class AnyoneInOwnershipChainStepConfig : AbstractRequestableStepConfig<YesOrNo, AnyoneInOwnershipChainFormModel, JourneyState>() {
    override val formModelClass = AnyoneInOwnershipChainFormModel::class

    override fun getStepSpecificContent(state: JourneyState): Map<String, Any?> =
        mapOf(
            "radioOptions" to
                listOf(
                    RadiosButtonViewModel(
                        value = true,
                        labelMsgKey = "registerProperty.superiorLandlords.anyoneInOwnershipChain.radios.option.yes",
                    ),
                    RadiosButtonViewModel(value = false, labelMsgKey = "forms.radios.option.no.label"),
                ),
        )

    override fun chooseTemplate(state: JourneyState) = "forms/anyoneInOwnershipChainForm"

    override fun mode(state: JourneyState): YesOrNo? =
        getFormModelFromStateOrNull(state)?.anyoneInOwnershipChain?.let { if (it) YesOrNo.YES else YesOrNo.NO }
}

@JourneyFrameworkComponent
final class AnyoneInOwnershipChainStep(
    stepConfig: AnyoneInOwnershipChainStepConfig,
) : RequestableStep<YesOrNo, AnyoneInOwnershipChainFormModel, JourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "anyone-in-ownership-chain"
    }
}
