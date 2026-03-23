package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.EpcState
import uk.gov.communities.prsdb.webapp.journeys.shared.YesOrNo
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.CheckMatchedEpcFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosViewModel.Companion.yesOrNoRadios

@JourneyFrameworkComponent("propertyRegistrationCheckMatchedEpcStepConfig")
class CheckMatchedEpcStepConfig : AbstractRequestableStepConfig<YesOrNo, CheckMatchedEpcFormModel, EpcState>() {
    override val formModelClass = CheckMatchedEpcFormModel::class

    override fun getStepSpecificContent(state: EpcState) =
        mapOf(
            "radioOptions" to yesOrNoRadios(),
        )

    override fun chooseTemplate(state: EpcState) = "forms/checkEpcTodoForm"

    override fun mode(state: EpcState) =
        getFormModelFromStateOrNull(state)?.let {
            when (it.matchedEpcIsCorrect) {
                true -> YesOrNo.YES
                false -> YesOrNo.NO
                null -> null
            }
        }

    override fun afterStepDataIsAdded(state: EpcState) {
        if (getFormModelFromStateOrNull(state)?.matchedEpcIsCorrect == true) {
            // TODO PDJB-661: Store confirmed EPC data once automatchedEpc/searchedEpc is available in state
        }
    }
}

@JourneyFrameworkComponent("propertyRegistrationCheckMatchedEpcStep")
final class CheckMatchedEpcStep(
    stepConfig: CheckMatchedEpcStepConfig,
) : RequestableStep<YesOrNo, CheckMatchedEpcFormModel, EpcState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "check-matched-epc"
        const val AUTOMATCHED_ROUTE_SEGMENT = "check-automatched-epc"
    }
}
