package uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.constants.EPC_GUIDE_URL
import uk.gov.communities.prsdb.webapp.constants.enums.EpcExemptionReason
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.states.EpcState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EpcExemptionReasonFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosButtonViewModel

@JourneyFrameworkComponent
class EpcExemptionReasonStepConfig : AbstractRequestableStepConfig<Complete, EpcExemptionReasonFormModel, EpcState>() {
    override val formModelClass = EpcExemptionReasonFormModel::class

    override fun getStepSpecificContent(state: EpcState) =
        mapOf(
            "fieldSetHeading" to "forms.epcExemptionReason.fieldSetHeading",
            "epcGuideUrl" to EPC_GUIDE_URL,
            "radioOptions" to
                listOf(
                    RadiosButtonViewModel(
                        value = EpcExemptionReason.ANNUAL_USE_LESS_THAN_4_MONTHS,
                        labelMsgKey = "forms.epcExemptionReason.radios.annualUseLessThan4Months.label",
                    ),
                    RadiosButtonViewModel(
                        value = EpcExemptionReason.ANNUAL_ENERGY_CONSUMPTION_LESS_THAN_25_PERCENT,
                        labelMsgKey = "forms.epcExemptionReason.radios.annualEnergyConsumptionLessThan25Percent.label",
                    ),
                    RadiosButtonViewModel(
                        value = EpcExemptionReason.TEMPORARY_BUILDING,
                        labelMsgKey = "forms.epcExemptionReason.radios.temporaryBuilding.label",
                    ),
                    RadiosButtonViewModel(
                        value = EpcExemptionReason.STANDALONE_SMALL_BUILDING,
                        labelMsgKey = "forms.epcExemptionReason.radios.standaloneSmallBuilding.label",
                        hintMsgKey = "forms.epcExemptionReason.radios.standaloneSmallBuilding.hint",
                    ),
                    RadiosButtonViewModel(
                        value = EpcExemptionReason.DUE_FOR_DEMOLITION,
                        labelMsgKey = "forms.epcExemptionReason.radios.dueForDemolition.label",
                    ),
                ),
        )

    override fun chooseTemplate(state: EpcState): String = "forms/epcExemptionReasonForm"

    override fun mode(state: EpcState): Complete? = getFormModelFromStateOrNull(state)?.exemptionReason?.let { Complete.COMPLETE }
}

@JourneyFrameworkComponent
final class EpcExemptionReasonStep(
    stepConfig: EpcExemptionReasonStepConfig,
) : RequestableStep<Complete, EpcExemptionReasonFormModel, EpcState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "epc-exemption-reason"
    }
}
