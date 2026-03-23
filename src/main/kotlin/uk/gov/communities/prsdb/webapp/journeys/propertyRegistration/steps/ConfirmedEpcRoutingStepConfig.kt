package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractInternalStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.EpcState

// TODO PDJB-661: Route based on EPC data once confirmedEpc is populated
@JourneyFrameworkComponent
class ConfirmedEpcRoutingStepConfig : AbstractInternalStepConfig<ConfirmedEpcRoutingMode, EpcState>() {
    override fun mode(state: EpcState): ConfirmedEpcRoutingMode? {
        val epc = state.confirmedEpc ?: return null
        if (epc.isPastExpiryDate() && !epc.isEnergyRatingEOrBetter()) return ConfirmedEpcRoutingMode.LOW_ENERGY_RATING
        return when (state.isOccupied) {
            true -> ConfirmedEpcRoutingMode.OCCUPIED
            false -> ConfirmedEpcRoutingMode.UNOCCUPIED
            null -> null
        }
    }
}

@JourneyFrameworkComponent
final class ConfirmedEpcRoutingStep(
    stepConfig: ConfirmedEpcRoutingStepConfig,
) : JourneyStep.InternalStep<ConfirmedEpcRoutingMode, EpcState>(stepConfig)

enum class ConfirmedEpcRoutingMode {
    LOW_ENERGY_RATING,
    OCCUPIED,
    UNOCCUPIED,
}
