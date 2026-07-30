package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractInternalStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.CombinedComplianceCheckState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.ElectricalSafetyState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.EpcState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.GasSafetyState

@JourneyFrameworkComponent
class HasMissingComplianceStepConfig : AbstractInternalStepConfig<ConfirmMissingComplianceCheckResult, CombinedComplianceCheckState>() {
    override fun mode(state: CombinedComplianceCheckState): ConfirmMissingComplianceCheckResult {
        val anyInvalid =
            isGasCertInvalid(state.gasSafetyTask) ||
                isElectricalCertInvalid(state.electricalSafetyTask) ||
                isEpcInvalid(state.epcTask)
        return if (state.isOccupied && anyInvalid) {
            ConfirmMissingComplianceCheckResult.OCCUPIED_AND_HAS_INVALID_CERTIFICATES
        } else {
            ConfirmMissingComplianceCheckResult.UNOCCUPIED_OR_VALID_CERTIFICATES
        }
    }

    companion object {
        fun isGasCertInvalid(state: GasSafetyState): Boolean {
            if (state.gasSafetyDetailsTask.hasGasSupplyStep.formModelIfReachableOrNull
                    ?.hasGasSupply != true
            ) {
                return false
            }
            if (state.gasSafetyDetailsTask.hasGasCertStep.outcome == HasGasCertMode.PROVIDE_THIS_LATER) return false
            val isOutdated = state.gasSafetyDetailsTask.getGasSafetyCertificateIsOutdated()
            return isOutdated == null || isOutdated
        }

        fun isElectricalCertInvalid(state: ElectricalSafetyState): Boolean {
            if (state.electricalSafetyDetailsTask.hasElectricalCertStep.outcome == HasElectricalCertMode.PROVIDE_THIS_LATER) return false
            val isOutdated = state.electricalSafetyDetailsTask.getElectricalCertificateIsOutdated()
            return isOutdated == null || isOutdated
        }

        fun isEpcInvalid(state: EpcState): Boolean {
            if (state.epcDetailsTask.hasEpcStep.outcome == HasEpcMode.PROVIDE_LATER) return false
            val acceptedEpc =
                state.epcDetailsTask.acceptedEpcIfStillAccepted
                    ?: return state.epcDetailsTask.epcExemptionStep.formModelIfReachableOrNull?.exemptionReason == null
            return (
                (
                    acceptedEpc.isPastExpiryDate() &&
                        (state.epcDetailsTask.epcInDateAtStartOfTenancyCheckStep.outcome != EpcInDateAtStartOfTenancyCheckMode.IN_DATE)
                ) ||
                    (
                        !acceptedEpc.isEnergyRatingEOrBetter() &&
                            (state.epcDetailsTask.meesExemptionStep.formModelIfReachableOrNull?.exemptionReason == null)
                    )
            )
        }
    }
}

@JourneyFrameworkComponent
class HasMissingComplianceStep(
    stepConfig: HasMissingComplianceStepConfig,
) : JourneyStep.InternalStep<ConfirmMissingComplianceCheckResult, CombinedComplianceCheckState>(stepConfig)

enum class ConfirmMissingComplianceCheckResult {
    UNOCCUPIED_OR_VALID_CERTIFICATES,
    OCCUPIED_AND_HAS_INVALID_CERTIFICATES,
}
