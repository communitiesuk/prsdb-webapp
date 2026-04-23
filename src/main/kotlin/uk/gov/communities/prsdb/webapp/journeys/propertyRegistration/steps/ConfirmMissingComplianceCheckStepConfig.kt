package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractInternalStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.CombinedComplianceCheckState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.ElectricalSafetyState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.EpcState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.GasSafetyState

@JourneyFrameworkComponent
class ConfirmMissingComplianceCheckStepConfig :
    AbstractInternalStepConfig<ConfirmMissingComplianceCheckResult, CombinedComplianceCheckState>() {
    override fun mode(state: CombinedComplianceCheckState): ConfirmMissingComplianceCheckResult =
        if (!state.isOccupied || (!isGasCertMissing(state) && !isElectricalCertMissing(state) && !isEpcMissing(state))) {
            ConfirmMissingComplianceCheckResult.UNOCCUPIED_OR_ALL_CERTIFICATES
        } else {
            ConfirmMissingComplianceCheckResult.OCCUPIED_AND_HAS_MISSING_CERTIFICATES
        }

    companion object {
        fun isGasCertMissing(state: GasSafetyState): Boolean {
            val hasGasSupply =
                state.hasGasSupplyStep.formModelIfReachableOrNull?.hasGasSupply
                    ?: return false
            if (!hasGasSupply) return false
            return state.getGasSafetyCertificateIsOutdated() == true
        }

        fun isElectricalCertMissing(state: ElectricalSafetyState): Boolean {
            state.getElectricalCertificateExpiryDateIfReachable()
                ?: return true
            return state.getElectricalCertificateIsOutdated() == true
        }

        fun isEpcMissing(state: EpcState): Boolean {
            if (state.acceptedEpc != null) return false
            return state.epcExemptionStep.formModelIfReachableOrNull?.exemptionReason == null
        }
    }
}

@JourneyFrameworkComponent
class ConfirmMissingComplianceCheckStep(
    stepConfig: ConfirmMissingComplianceCheckStepConfig,
) : JourneyStep.InternalStep<ConfirmMissingComplianceCheckResult, CombinedComplianceCheckState>(stepConfig)

enum class ConfirmMissingComplianceCheckResult {
    UNOCCUPIED_OR_ALL_CERTIFICATES,
    OCCUPIED_AND_HAS_MISSING_CERTIFICATES,
}
