package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractInternalStepConfig
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.PropertyRegistrationJourneyState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.ElectricalSafetyState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.EpcState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.GasSafetyState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete

@JourneyFrameworkComponent
class ConfirmMissingComplianceCheckStepConfig :
    AbstractInternalStepConfig<Complete, PropertyRegistrationJourneyState>() {
    override fun mode(state: PropertyRegistrationJourneyState): Complete = Complete.COMPLETE

    override fun resolveNextDestination(
        state: PropertyRegistrationJourneyState,
        defaultDestination: Destination,
    ): Destination {
        val isOccupied = state.occupied.formModelOrNull?.occupied == true

        return if (isOccupied && (isGasCertMissing(state) || isElectricalCertMissing(state) || isEpcMissing(state))) {
            Destination(state.confirmMissingComplianceStep)
        } else {
            defaultDestination
        }
    }

    companion object {
        fun isGasCertMissing(state: GasSafetyState): Boolean {
            val hasGasSupply =
                state.hasGasSupplyStep.formModelIfReachableOrNull?.hasGasSupply
                    ?: return false
            if (!hasGasSupply) return false
            val issueDate =
                state.getGasSafetyCertificateIssueDateIfReachable()
                    ?: return true
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
) : JourneyStep.InternalStep<Complete, PropertyRegistrationJourneyState>(stepConfig)
