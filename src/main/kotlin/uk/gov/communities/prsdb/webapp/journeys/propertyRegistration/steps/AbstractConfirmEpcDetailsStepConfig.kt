package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.EpcState
import uk.gov.communities.prsdb.webapp.journeys.shared.YesOrNo
import uk.gov.communities.prsdb.webapp.models.dataModels.EpcDataModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.CheckMatchedEpcFormModel

abstract class AbstractConfirmEpcDetailsStepConfig : AbstractRequestableStepConfig<YesOrNo, CheckMatchedEpcFormModel, EpcState>() {
    override val formModelClass = CheckMatchedEpcFormModel::class

    override fun mode(state: EpcState) =
        when (getFormModelFromStateOrNull(state)?.matchedEpcIsCorrect) {
            null -> null
            false -> YesOrNo.NO
            true -> YesOrNo.YES // continue with further checks
        }

    override fun isSubClassInitialised(): Boolean = ::getReleventEpc.isInitialized

    lateinit var getReleventEpc: (EpcState) -> EpcDataModel?

    fun usingEpc(getReleventEpc: EpcState.() -> EpcDataModel?): AbstractConfirmEpcDetailsStepConfig {
        this.getReleventEpc = getReleventEpc
        return this
    }

    override fun afterStepDataIsAdded(state: EpcState) {
        if (getFormModelFromStateOrNull(state)?.matchedEpcIsCorrect == true) {
            state.acceptedEpc = getReleventEpc(state)
        }
        // TODO PDJB-746 - consider whether we need to set acceptedEpc to null if the user answers "No" here
    }
}
