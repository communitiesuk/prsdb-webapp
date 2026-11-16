package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractInternalStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.InternalStep
import uk.gov.communities.prsdb.webapp.journeys.UnrecoverableJourneyStateException
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.OwnershipStructureState

enum class OwnershipStructureRouteMode {
    SKIP_ALL,
    ASK_FREEHOLDERS,
    ASK_SUPERIOR_LANDLORDS,
    ASK_FREEHOLDERS_AND_SUPERIOR_LANDLORDS,
}

@JourneyFrameworkComponent
class OwnershipStructureRoutingStepConfig : AbstractInternalStepConfig<OwnershipStructureRouteMode, OwnershipStructureState>() {
    override fun mode(state: OwnershipStructureState): OwnershipStructureRouteMode {
        val ownershipType =
            state.ownershipTypeTask.ownershipType
                ?: throw UnrecoverableJourneyStateException(
                    state.journeyId,
                    "Ownership structure routing was reached before the ownership type task was complete",
                )

        val asksFreeholders = ownershipType.requiresFreeholderRecords()
        val asksSuperiorLandlords = ownershipType.requiresSuperiorLandlordInfo()

        return when (Pair(asksFreeholders, asksSuperiorLandlords)) {
            Pair(true, true) -> OwnershipStructureRouteMode.ASK_FREEHOLDERS_AND_SUPERIOR_LANDLORDS
            Pair(true, false) -> OwnershipStructureRouteMode.ASK_FREEHOLDERS
            Pair(false, true) -> OwnershipStructureRouteMode.ASK_SUPERIOR_LANDLORDS
            else -> OwnershipStructureRouteMode.SKIP_ALL
        }
    }
}

@JourneyFrameworkComponent
class OwnershipStructureRoutingStep(
    stepConfig: OwnershipStructureRoutingStepConfig,
) : InternalStep<OwnershipStructureRouteMode, OwnershipStructureState>(stepConfig)
