package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.constants.enums.EpcExemptionReason
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EpcExemptionFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosButtonViewModel

@JourneyFrameworkComponent
class EpcExemptionStepConfig : AbstractRequestableStepConfig<Complete, EpcExemptionFormModel, JourneyState>() {
    override val formModelClass = EpcExemptionFormModel::class

    override fun getStepSpecificContent(state: JourneyState) =
        mapOf(
            "title" to "propertyCompliance.epcTask.epcExemption.fieldSetHeading",
            "fieldSetHeading" to "propertyCompliance.epcTask.epcExemption.fieldSetHeading",
            "radioOptions" to
                listOf(
                    RadiosButtonViewModel(
                        value = EpcExemptionReason.PROTECTED_ARCHITECTURAL_OR_HISTORICAL_MERIT,
                        labelMsgKey = "propertyCompliance.epcTask.epcExemption.radios.protectedArchitecturalOrHistoricalMerit.label",
                        hintMsgKey = "propertyCompliance.epcTask.epcExemption.radios.protectedArchitecturalOrHistoricalMerit.hint",
                    ),
                    RadiosButtonViewModel(
                        value = EpcExemptionReason.STANDALONE_SMALL_BUILDING,
                        labelMsgKey = "propertyCompliance.epcTask.epcExemption.radios.standaloneSmallBuilding.label",
                        hintMsgKey = "propertyCompliance.epcTask.epcExemption.radios.standaloneSmallBuilding.hint",
                    ),
                    RadiosButtonViewModel(
                        value = EpcExemptionReason.DUE_FOR_DEMOLITION,
                        labelMsgKey = "propertyCompliance.epcTask.epcExemption.radios.dueForDemolition.label",
                        hintMsgKey = "propertyCompliance.epcTask.epcExemption.radios.dueForDemolition.hint",
                    ),
                    RadiosButtonViewModel(
                        value = EpcExemptionReason.TEMPORARY_BUILDING,
                        labelMsgKey = "propertyCompliance.epcTask.epcExemption.radios.temporaryBuilding.label",
                        hintMsgKey = "propertyCompliance.epcTask.epcExemption.radios.temporaryBuilding.hint",
                    ),
                    RadiosButtonViewModel(
                        value = EpcExemptionReason.ANNUAL_USE_LESS_THAN_4_MONTHS,
                        labelMsgKey = "propertyCompliance.epcTask.epcExemption.radios.annualUseLessThan4Months.label",
                    ),
                ),
        )

    override fun chooseTemplate(state: JourneyState) = "forms/epcExemptionForm"

    override fun mode(state: JourneyState): Complete? = getFormModelFromStateOrNull(state)?.exemptionReason?.let { Complete.COMPLETE }
}

@JourneyFrameworkComponent
final class EpcExemptionStep(
    stepConfig: EpcExemptionStepConfig,
) : RequestableStep<Complete, EpcExemptionFormModel, JourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "epc-exemption"
    }
}
