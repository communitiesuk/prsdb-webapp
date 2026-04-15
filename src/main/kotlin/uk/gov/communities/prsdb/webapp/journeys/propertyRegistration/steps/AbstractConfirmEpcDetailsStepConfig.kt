package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.EpcState
import uk.gov.communities.prsdb.webapp.journeys.shared.YesOrNo
import uk.gov.communities.prsdb.webapp.models.dataModels.EpcDataModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.MatchedEpcFormModel

abstract class AbstractConfirmEpcDetailsStepConfig<FormModel : MatchedEpcFormModel> :
    AbstractRequestableStepConfig<YesOrNo, FormModel, EpcState>() {
    override fun mode(state: EpcState) =
        when (getFormModelFromStateOrNull(state)?.matchedEpcIsCorrect) {
            null -> null
            false -> YesOrNo.NO
            true -> YesOrNo.YES // continue with further checks
        }

    override fun isSubClassInitialised(): Boolean = ::getRelevantEpc.isInitialized

    lateinit var getRelevantEpc: (EpcState) -> EpcDataModel?

    fun usingEpc(getRelevantEpc: EpcState.() -> EpcDataModel?): AbstractConfirmEpcDetailsStepConfig<FormModel> {
        this.getRelevantEpc = getRelevantEpc
        return this
    }

    override fun afterStepDataIsAdded(state: EpcState) {
        if (getFormModelFromStateOrNull(state)?.matchedEpcIsCorrect == true) {
            state.acceptedEpc = getRelevantEpc(state)
        } else if (getFormModelFromStateOrNull(state)?.matchedEpcIsCorrect == false && state.acceptedEpc == getRelevantEpc(state)) {
            // User has now actively rejected the EPC details (even if they had previously accepted them), so we should clear the accepted EPC from the state
            state.acceptedEpc = null
        }
    }
}
