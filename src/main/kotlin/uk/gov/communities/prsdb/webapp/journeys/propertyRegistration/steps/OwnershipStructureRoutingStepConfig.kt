package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractInternalStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.InternalStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.OwnershipStructureState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.OwnershipTypeState
import uk.gov.communities.prsdb.webapp.journeys.shared.YesOrNo

enum class OwnershipStructureRouteMode {
    SKIP_ALL,
    ASK_FREEHOLDERS,
    ASK_SUPERIOR_LANDLORDS,
    ASK_FREEHOLDERS_AND_SUPERIOR_LANDLORDS,
}

@JourneyFrameworkComponent
class OwnershipStructureRoutingStepConfig : AbstractInternalStepConfig<OwnershipStructureRouteMode, OwnershipStructureState>() {
    override fun mode(state: OwnershipStructureState): OwnershipStructureRouteMode {
        val asksFreeholders = asksFreeholders(state.ownershipTypeTask)
        val asksSuperiorLandlords = asksSuperiorLandlords(state.ownershipTypeTask)

        return when (Pair(asksFreeholders, asksSuperiorLandlords)) {
            Pair(true, true) -> OwnershipStructureRouteMode.ASK_FREEHOLDERS_AND_SUPERIOR_LANDLORDS
            Pair(true, false) -> OwnershipStructureRouteMode.ASK_FREEHOLDERS
            Pair(false, true) -> OwnershipStructureRouteMode.ASK_SUPERIOR_LANDLORDS
            else -> OwnershipStructureRouteMode.SKIP_ALL
        }
    }

    // TODO PDJB-2015: this is the same rule as OwnershipType.requiresFreeholderRecords(), expressed
    //  over the journey's answers. The enabling step is the answers -> OwnershipType mapping on
    //  OwnershipTypeState; once that exists, delegate to it so the rule is only stated once.
    // TODO PDJB-1968: pre-populating the freeholders list needs a second, narrower predicate - the
    //  joint freehold types, where the landlord is themselves one of the freeholders.
    private fun asksFreeholders(ownershipType: OwnershipTypeState): Boolean =
        ownershipType.ownsPropertyStep.outcome == YesOrNo.NO ||
            ownershipType.ownershipKindStep.outcome == OwnershipKind.LEASEHOLD_ONLY ||
            ownershipType.isJointFreeholder

    private fun asksSuperiorLandlords(ownershipType: OwnershipTypeState): Boolean =
        ownershipType.ownsPropertyStep.outcome == YesOrNo.NO ||
            ownershipType.ownershipKindStep.outcome in setOf(OwnershipKind.LEASEHOLD_ONLY, OwnershipKind.FREEHOLD_AND_LEASEHOLD)
}

@JourneyFrameworkComponent
class OwnershipStructureRoutingStep(
    stepConfig: OwnershipStructureRoutingStepConfig,
) : InternalStep<OwnershipStructureRouteMode, OwnershipStructureState>(stepConfig)
