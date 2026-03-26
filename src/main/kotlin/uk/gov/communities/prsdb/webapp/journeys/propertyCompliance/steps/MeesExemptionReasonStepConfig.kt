package uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.constants.enums.MeesExemptionReason
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.states.EpcState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.MeesExemptionReasonFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosButtonViewModel

@JourneyFrameworkComponent
class MeesExemptionReasonStepConfig : AbstractRequestableStepConfig<Complete, MeesExemptionReasonFormModel, EpcState>() {
    override val formModelClass = MeesExemptionReasonFormModel::class

    override fun getStepSpecificContent(state: EpcState) =
        mapOf(
            "fieldSetHeading" to "propertyCompliance.epcTask.meesExemptionReason.fieldSetHeading",
            "radioOptions" to
                listOf(
                    RadiosButtonViewModel(
                        value = MeesExemptionReason.HIGH_COST,
                        labelMsgKey = "propertyCompliance.epcTask.meesExemptionReason.radios.highCost.label",
                        hintMsgKey = "propertyCompliance.epcTask.meesExemptionReason.radios.highCost.hint",
                    ),
                    RadiosButtonViewModel(
                        value = MeesExemptionReason.ALL_IMPROVEMENTS_MADE,
                        labelMsgKey = "propertyCompliance.epcTask.meesExemptionReason.radios.allImprovementsMade.label",
                        hintMsgKey = "propertyCompliance.epcTask.meesExemptionReason.radios.allImprovementsMade.hint",
                    ),
                    RadiosButtonViewModel(
                        value = MeesExemptionReason.WALL_INSULATION,
                        labelMsgKey = "propertyCompliance.epcTask.meesExemptionReason.radios.wallInsulation.label",
                        hintMsgKey = "propertyCompliance.epcTask.meesExemptionReason.radios.wallInsulation.hint",
                    ),
                    RadiosButtonViewModel(
                        value = MeesExemptionReason.THIRD_PARTY_CONSENT,
                        labelMsgKey = "propertyCompliance.epcTask.meesExemptionReason.radios.thirdPartyConsent.label",
                        hintMsgKey = "propertyCompliance.epcTask.meesExemptionReason.radios.thirdPartyConsent.hint",
                    ),
                    RadiosButtonViewModel(
                        value = MeesExemptionReason.PROPERTY_DEVALUATION,
                        labelMsgKey = "propertyCompliance.epcTask.meesExemptionReason.radios.propertyDevaluation.label",
                        hintMsgKey = "propertyCompliance.epcTask.meesExemptionReason.radios.propertyDevaluation.hint",
                    ),
                    RadiosButtonViewModel(
                        value = MeesExemptionReason.NEW_LANDLORD,
                        labelMsgKey = "propertyCompliance.epcTask.meesExemptionReason.radios.newLandlord.label",
                        hintMsgKey = "propertyCompliance.epcTask.meesExemptionReason.radios.newLandlord.hint",
                    ),
                ),
        )

    override fun chooseTemplate(state: EpcState): String = "forms/exemptionReasonForm"

    override fun mode(state: EpcState): Complete? = getFormModelFromStateOrNull(state)?.exemptionReason?.let { Complete.COMPLETE }
}

@JourneyFrameworkComponent
final class MeesExemptionReasonStep(
    stepConfig: MeesExemptionReasonStepConfig,
) : RequestableStep<Complete, MeesExemptionReasonFormModel, EpcState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "mees-exemption-reason"
    }
}
