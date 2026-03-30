package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.EpcState
import uk.gov.communities.prsdb.webapp.models.dataModels.EpcDataModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.CheckMatchedEpcFormModel

abstract class AbstractConfirmEpcDetailsStepConfig :
    AbstractRequestableStepConfig<CheckMatchedEpcMode, CheckMatchedEpcFormModel, EpcState>() {
    override val formModelClass = CheckMatchedEpcFormModel::class

    override fun mode(state: EpcState): CheckMatchedEpcMode? {
        val formModelOrNull = getFormModelFromStateOrNull(state)
        if (formModelOrNull?.matchedEpcIsCorrect == null) return null
        if (formModelOrNull.matchedEpcIsCorrect == false) return CheckMatchedEpcMode.EPC_INCORRECT

        val epcDetails = getReleventEpc(state) ?: return null
        if (epcDetails.isPastExpiryDate()) return CheckMatchedEpcMode.EPC_OLDER_THAN_10_YEARS
        if (!(epcDetails.isEnergyRatingEOrBetter())) return CheckMatchedEpcMode.EPC_LOW_ENERGY_RATING
        return CheckMatchedEpcMode.EPC_COMPLIANT
    }

    override fun isSubClassInitialised(): Boolean = ::getReleventEpc.isInitialized

    private lateinit var getReleventEpc: (EpcState) -> EpcDataModel?

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

enum class CheckMatchedEpcMode {
    EPC_INCORRECT,
    EPC_COMPLIANT,
    EPC_OLDER_THAN_10_YEARS,
    EPC_LOW_ENERGY_RATING,
}
