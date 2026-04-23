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
        val anyMissing = isGasCertMissingOrExpired(state) || isElectricalCertMissingOrExpired(state) || isEpcMissing(state)
        return if (state.isOccupied &&
            anyMissing
        ) {
            ConfirmMissingComplianceCheckResult.OCCUPIED_AND_HAS_MISSING_CERTIFICATES
        } else {
            ConfirmMissingComplianceCheckResult.UNOCCUPIED_OR_ALL_CERTIFICATES
        }
    }

    companion object {
        fun isGasCertMissingOrExpired(state: GasSafetyState): Boolean {
            if (state.hasGasSupplyStep.formModelIfReachableOrNull?.hasGasSupply != true) return false
            val isOutdated = state.getGasSafetyCertificateIsOutdated()
            return isOutdated == null || isOutdated
        }

        fun isElectricalCertMissingOrExpired(state: ElectricalSafetyState): Boolean {
            val isOutdated = state.getElectricalCertificateIsOutdated()
            return isOutdated == null || isOutdated
        }

        fun isEpcMissing(state: EpcState): Boolean {
            if (state.acceptedEpcIfReachable != null) return false
            return state.epcExemptionStep.formModelIfReachableOrNull?.exemptionReason == null
        }
    }
}

@JourneyFrameworkComponent
class HasMissingComplianceStep(
    stepConfig: HasMissingComplianceStepConfig,
) : JourneyStep.InternalStep<ConfirmMissingComplianceCheckResult, CombinedComplianceCheckState>(stepConfig)

enum class ConfirmMissingComplianceCheckResult {
    UNOCCUPIED_OR_ALL_CERTIFICATES,
    OCCUPIED_AND_HAS_MISSING_CERTIFICATES,
}
