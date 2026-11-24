package uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig

import uk.gov.communities.prsdb.webapp.exceptions.CyaDataHasChangedException
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.shared.states.MissingComplianceState
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.ConfirmMissingComplianceFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosButtonViewModel

abstract class AbstractConfirmMissingComplianceStepConfig<TFormModel : ConfirmMissingComplianceFormModel> :
    AbstractRequestableStepConfig<ConfirmMissingComplianceMode, TFormModel, MissingComplianceState>() {
    override val changesAnswers = false

    override fun getStepSpecificContent(state: MissingComplianceState) =
        mapOf(
            "isGasMissing" to state.isGasCertMissing,
            "isElectricalMissing" to state.isElectricalCertMissing,
            "isEpcMissing" to state.isEpcMissing,
            "radioOptions" to
                listOf(
                    RadiosButtonViewModel(
                        value = true,
                        valueStr = "yes",
                        labelMsgKey = "propertyCompliance.confirmMissingCompliance.radios.yes",
                    ),
                    RadiosButtonViewModel(
                        value = false,
                        valueStr = "no",
                        labelMsgKey = "propertyCompliance.confirmMissingCompliance.radios.no",
                    ),
                ),
        )

    override fun chooseTemplate(state: MissingComplianceState) = "forms/confirmMissingCompliance"

    override fun mode(state: MissingComplianceState): ConfirmMissingComplianceMode? =
        getFormModelFromStateOrNull(state)?.wantsToProceed?.let {
            if (it) ConfirmMissingComplianceMode.CONFIRMED else ConfirmMissingComplianceMode.GO_BACK
        }

    override fun afterStepIsReached(state: MissingComplianceState) {
        if (state.answersChangedSinceConfirmation) {
            throw CyaDataHasChangedException("Answers changed after confirmation for journey ${state.journeyId}")
        }
    }
}

enum class ConfirmMissingComplianceMode {
    CONFIRMED,
    GO_BACK,
}
