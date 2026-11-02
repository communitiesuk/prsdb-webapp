package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.OwnershipStructureState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.OwnershipTypeTask
import uk.gov.communities.prsdb.webapp.journeys.shared.YesOrNo

class OwnershipStructureRoutingStepConfigTests {
    @ParameterizedTest(name = "owns={0}, kind={1}, freehold={2} -> {3}")
    @MethodSource("provideAnsweredScenarios")
    fun `mode decides which associated parties to ask about from the ownership answers`(
        ownsProperty: YesOrNo,
        ownershipKind: OwnershipKind?,
        freeholdType: FreeholdType?,
        expectedMode: OwnershipStructureRouteMode,
    ) {
        val result = OwnershipStructureRoutingStepConfig().mode(stateWith(ownsProperty, ownershipKind, freeholdType))

        assertEquals(expectedMode, result)
    }

    private fun stateWith(
        ownsProperty: YesOrNo,
        ownershipKind: OwnershipKind?,
        freeholdType: FreeholdType?,
    ): OwnershipStructureState {
        val ownsPropertyStep = mock<OwnsPropertyStep> { on { outcome } doReturn ownsProperty }
        val ownershipKindStep = mock<OwnershipKindStep> { on { outcome } doReturn ownershipKind }
        val freeholdTypeStep = mock<FreeholdTypeStep> { on { outcome } doReturn freeholdType }
        val ownershipTypeTask =
            mock<OwnershipTypeTask> {
                on { this.ownsPropertyStep } doReturn ownsPropertyStep
                on { this.ownershipKindStep } doReturn ownershipKindStep
                on { this.freeholdTypeStep } doReturn freeholdTypeStep
            }
        return mock<OwnershipStructureState> { on { this.ownershipTypeTask } doReturn ownershipTypeTask }
    }

    companion object {
        // ownsProperty, ownershipKind, freeholdType, expectedMode
        @JvmStatic
        private fun provideAnsweredScenarios() =
            arrayOf(
                arguments(
                    YesOrNo.NO,
                    null,
                    null,
                    OwnershipStructureRouteMode.ASK_FREEHOLDERS_AND_SUPERIOR_LANDLORDS,
                ),
                arguments(
                    YesOrNo.YES,
                    OwnershipKind.LEASEHOLD_ONLY,
                    null,
                    OwnershipStructureRouteMode.ASK_FREEHOLDERS_AND_SUPERIOR_LANDLORDS,
                ),
                arguments(
                    YesOrNo.YES,
                    OwnershipKind.FREEHOLD_ONLY,
                    FreeholdType.SOLE_OWNERSHIP,
                    OwnershipStructureRouteMode.SKIP_ALL,
                ),
                arguments(
                    YesOrNo.YES,
                    OwnershipKind.FREEHOLD_ONLY,
                    FreeholdType.COMMONHOLD,
                    OwnershipStructureRouteMode.SKIP_ALL,
                ),
                arguments(
                    YesOrNo.YES,
                    OwnershipKind.FREEHOLD_ONLY,
                    FreeholdType.JOINT_TENANTS,
                    OwnershipStructureRouteMode.ASK_FREEHOLDERS,
                ),
                arguments(
                    YesOrNo.YES,
                    OwnershipKind.FREEHOLD_ONLY,
                    FreeholdType.TENANTS_IN_COMMON,
                    OwnershipStructureRouteMode.ASK_FREEHOLDERS,
                ),
                arguments(
                    YesOrNo.YES,
                    OwnershipKind.FREEHOLD_AND_LEASEHOLD,
                    FreeholdType.SOLE_OWNERSHIP,
                    OwnershipStructureRouteMode.ASK_SUPERIOR_LANDLORDS,
                ),
                arguments(
                    YesOrNo.YES,
                    OwnershipKind.FREEHOLD_AND_LEASEHOLD,
                    FreeholdType.COMMONHOLD,
                    OwnershipStructureRouteMode.ASK_SUPERIOR_LANDLORDS,
                ),
                arguments(
                    YesOrNo.YES,
                    OwnershipKind.FREEHOLD_AND_LEASEHOLD,
                    FreeholdType.JOINT_TENANTS,
                    OwnershipStructureRouteMode.ASK_FREEHOLDERS_AND_SUPERIOR_LANDLORDS,
                ),
                arguments(
                    YesOrNo.YES,
                    OwnershipKind.FREEHOLD_AND_LEASEHOLD,
                    FreeholdType.TENANTS_IN_COMMON,
                    OwnershipStructureRouteMode.ASK_FREEHOLDERS_AND_SUPERIOR_LANDLORDS,
                ),
            )
    }
}
