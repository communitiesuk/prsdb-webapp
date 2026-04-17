package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.EpcRegistrationCyaSummaryRowsFactory
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.EpcState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel
import uk.gov.communities.prsdb.webapp.services.EpcCertificateUrlProvider

@JourneyFrameworkComponent
class CheckEpcAnswersStepConfig(
    private val epcCertificateUrlProvider: EpcCertificateUrlProvider,
) : AbstractRequestableStepConfig<Complete, NoInputFormModel, EpcState>() {
    override val formModelClass = NoInputFormModel::class

    override fun getStepSpecificContent(state: EpcState): Map<String, Any?> {
        val factory = EpcRegistrationCyaSummaryRowsFactory(epcCertificateUrlProvider, state)
        return mapOf(
            "epcCardTitle" to factory.createEpcCardTitle(),
            "epcCardActions" to factory.createEpcCardActions(),
            "epcCardRows" to factory.createEpcCardRows(),
            "epcExpiredTextKey" to factory.getEpcExpiredTextKey(),
            "tenancyCheckRows" to factory.createTenancyCheckRows(),
            "lowRatingTextKey" to factory.getLowRatingTextKey(),
            "exemptionReasonRows" to factory.createExemptionReasonRows(),
            "nonEpcRows" to factory.createNonEpcRows(),
            "insetTextKey" to factory.getInsetTextKey(),
        )
    }

    override fun chooseTemplate(state: EpcState) = "forms/checkEpcAnswersForm"

    override fun mode(state: EpcState) = getFormModelFromStateOrNull(state)?.let { Complete.COMPLETE }
}

enum class EpcScenario {
    NO_EPC_EXEMPT,
    LOW_ENERGY_EPC_MEES_EXEMPTION,
    VALID_EPC,
    SKIPPED_UNOCCUPIED,
    NO_EPC_NO_EXEMPTION_UNOCCUPIED,
    EPC_EXPIRED_UNOCCUPIED,
    LOW_ENERGY_EPC_NO_EXEMPTION_UNOCCUPIED,
    SKIPPED_OCCUPIED,
    NO_EPC_NO_EXEMPTION_OCCUPIED,
    EPC_EXPIRED_NOT_IN_DATE_OCCUPIED,
    LOW_ENERGY_EPC_NO_EXEMPTION_OCCUPIED,
    EPC_EXPIRED_IN_DATE_OCCUPIED,
    LOW_ENERGY_EPC_EXPIRED_IN_DATE_MEES_EXEMPTION_OCCUPIED,
    LOW_ENERGY_EPC_EXPIRED_IN_DATE_NO_EXEMPTION_OCCUPIED,
}

@JourneyFrameworkComponent
final class CheckEpcAnswersStep(
    stepConfig: CheckEpcAnswersStepConfig,
) : RequestableStep<Complete, NoInputFormModel, EpcState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "check-epc-answers"
    }
}
