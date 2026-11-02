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

    // TODO PDJB-2015: this is the same rule as OwnershipType.impliesAnotherFreeholder(), expressed
    //  over the journey's answers. Once those answers can be mapped to an OwnershipType, delegate
    //  to it so the rule is only stated once.
    private fun asksFreeholders(ownershipType: OwnershipTypeState): Boolean =
        ownershipType.ownsPropertyStep.outcome == YesOrNo.NO ||
            ownershipType.ownershipKindStep.outcome == OwnershipKind.LEASEHOLD_ONLY ||
            ownershipType.freeholdTypeStep.outcome in setOf(FreeholdType.JOINT_TENANTS, FreeholdType.TENANTS_IN_COMMON)

    // Note this is whether the question is asked, not the answer, which is recorded in
    // property_ownership.has_superior_landlords.
    private fun asksSuperiorLandlords(ownershipType: OwnershipTypeState): Boolean =
        ownershipType.ownsPropertyStep.outcome == YesOrNo.NO ||
            ownershipType.ownershipKindStep.outcome in setOf(OwnershipKind.LEASEHOLD_ONLY, OwnershipKind.FREEHOLD_AND_LEASEHOLD)
}

@JourneyFrameworkComponent
class OwnershipStructureRoutingStep(
    stepConfig: OwnershipStructureRoutingStepConfig,
) : InternalStep<OwnershipStructureRouteMode, OwnershipStructureState>(stepConfig)
