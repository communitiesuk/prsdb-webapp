package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.constants.enums.MeesExemptionReason
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.MeesExemptionReasonFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosButtonViewModel

@JourneyFrameworkComponent
class MeesExemptionStepConfig : AbstractRequestableStepConfig<Complete, MeesExemptionReasonFormModel, JourneyState>() {
    override val formModelClass = MeesExemptionReasonFormModel::class

    override fun getStepSpecificContent(state: JourneyState) =
        mapOf(
            "title" to "forms.meesExemptionReason.registration.heading",
            "radioOptions" to
                listOf(
                    RadiosButtonViewModel(
                        value = MeesExemptionReason.ALL_IMPROVEMENTS_MADE,
                        labelMsgKey = "forms.meesExemptionReason.registration.radios.allImprovementsMade.label",
                    ),
                    RadiosButtonViewModel(
                        value = MeesExemptionReason.HIGH_COST,
                        labelMsgKey = "forms.meesExemptionReason.registration.radios.highCost.label",
                    ),
                    RadiosButtonViewModel(
                        value = MeesExemptionReason.WALL_INSULATION,
                        labelMsgKey = "forms.meesExemptionReason.registration.radios.wallInsulation.label",
                    ),
                    RadiosButtonViewModel(
                        value = MeesExemptionReason.THIRD_PARTY_CONSENT,
                        labelMsgKey = "forms.meesExemptionReason.registration.radios.thirdPartyConsent.label",
                    ),
                    RadiosButtonViewModel(
                        value = MeesExemptionReason.PROPERTY_DEVALUATION,
                        labelMsgKey = "forms.meesExemptionReason.registration.radios.propertyDevaluation.label",
                    ),
                    RadiosButtonViewModel(
                        value = MeesExemptionReason.NEW_LANDLORD,
                        labelMsgKey = "forms.meesExemptionReason.registration.radios.newLandlord.label",
                    ),
                ),
        )

    override fun chooseTemplate(state: JourneyState) = "forms/meesExemptionReasonForm"

    override fun mode(state: JourneyState): Complete? = getFormModelFromStateOrNull(state)?.exemptionReason?.let { Complete.COMPLETE }
}

@JourneyFrameworkComponent
final class MeesExemptionStep(
    stepConfig: MeesExemptionStepConfig,
) : RequestableStep<Complete, MeesExemptionReasonFormModel, JourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "mees-exemption"
    }
}
