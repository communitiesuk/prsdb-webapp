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
        val scenario = determineScenario(state)
        val factory = EpcRegistrationCyaSummaryRowsFactory(epcCertificateUrlProvider, state, scenario)
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

    private fun determineScenario(state: EpcState): EpcScenario {
        val isOccupied = state.isOccupied == true
        return when {
            state.hasEpcStep.outcome == HasEpcMode.PROVIDE_LATER -> {
                if (isOccupied) EpcScenario.SKIPPED_OCCUPIED else EpcScenario.SKIPPED_UNOCCUPIED
            }

            state.acceptedEpc == null -> {
                determineNoEpcScenario(state, isOccupied)
            }

            else -> {
                determineEpcPresentScenario(state, isOccupied)
            }
        }
    }

    private fun determineNoEpcScenario(
        state: EpcState,
        isOccupied: Boolean,
    ): EpcScenario =
        when {
            state.epcExemptionStep.outcome == Complete.COMPLETE -> EpcScenario.NO_EPC_EXEMPT
            isOccupied -> EpcScenario.NO_EPC_NO_EXEMPTION_OCCUPIED
            else -> EpcScenario.NO_EPC_NO_EXEMPTION_UNOCCUPIED
        }

    private fun determineEpcPresentScenario(
        state: EpcState,
        isOccupied: Boolean,
    ): EpcScenario {
        val isExpired = state.epcAgeCheckStep.outcome == EpcAgeCheckMode.EPC_OLDER_THAN_10_YEARS
        return if (!isExpired) {
            determineNotExpiredEpcScenario(state, isOccupied)
        } else {
            determineExpiredEpcScenario(state, isOccupied)
        }
    }

    private fun determineNotExpiredEpcScenario(
        state: EpcState,
        isOccupied: Boolean,
    ): EpcScenario {
        if (state.epcEnergyRatingCheckStep.outcome != EpcEnergyRatingCheckMode.EPC_LOW_ENERGY_RATING) {
            return EpcScenario.VALID_EPC
        }
        return when {
            state.meesExemptionStep.outcome == Complete.COMPLETE -> EpcScenario.LOW_ENERGY_EPC_MEES_EXEMPTION
            isOccupied -> EpcScenario.LOW_ENERGY_EPC_NO_EXEMPTION_OCCUPIED
            else -> EpcScenario.LOW_ENERGY_EPC_NO_EXEMPTION_UNOCCUPIED
        }
    }

    private fun determineExpiredEpcScenario(
        state: EpcState,
        isOccupied: Boolean,
    ): EpcScenario {
        if (!isOccupied) return EpcScenario.EPC_EXPIRED_UNOCCUPIED
        return when (state.epcInDateAtStartOfTenancyCheckStep.outcome) {
            EpcInDateAtStartOfTenancyCheckMode.NOT_IN_DATE -> {
                EpcScenario.EPC_EXPIRED_NOT_IN_DATE_OCCUPIED
            }

            EpcInDateAtStartOfTenancyCheckMode.IN_DATE -> {
                if (state.epcEnergyRatingCheckStep.outcome != EpcEnergyRatingCheckMode.EPC_LOW_ENERGY_RATING) {
                    EpcScenario.EPC_EXPIRED_IN_DATE_OCCUPIED
                } else if (state.meesExemptionStep.outcome == Complete.COMPLETE) {
                    EpcScenario.LOW_ENERGY_EPC_EXPIRED_IN_DATE_MEES_EXEMPTION_OCCUPIED
                } else {
                    EpcScenario.LOW_ENERGY_EPC_EXPIRED_IN_DATE_NO_EXEMPTION_OCCUPIED
                }
            }

            null -> {
                throw IllegalStateException(
                    "CheckEpcAnswersStep is not reachable for an occupied property " +
                        "with an expired EPC before the tenancy check is answered",
                )
            }
        }
    }
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
