package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import uk.gov.communities.prsdb.webapp.journeys.AbstractJourneyState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.FreeholdType
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.FreeholdTypeStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.OwnershipKind
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.OwnershipKindStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.OwnsPropertyStep
import uk.gov.communities.prsdb.webapp.journeys.shared.YesOrNo

class OwnershipTypeStateTests {
    @ParameterizedTest(name = "{3} when ownsProperty is {0}, ownershipKind is {1} and freeholdType is {2}")
    @MethodSource("provideFreeholdTypeScenarios")
    fun `isJointFreeholder returns`(
        ownsProperty: YesOrNo?,
        ownershipKind: OwnershipKind?,
        freeholdType: FreeholdType?,
        expectedResult: Boolean,
    ) {
        // Arrange
        val state = buildTestOwnershipTypeState(ownsProperty, ownershipKind, freeholdType)

        // Act & Assert
        assertEquals(expectedResult, state.isJointFreeholder)
    }

    private fun buildTestOwnershipTypeState(
        ownsProperty: YesOrNo?,
        ownershipKind: OwnershipKind?,
        freeholdType: FreeholdType?,
    ): OwnershipTypeState =
        object : AbstractJourneyState(journeyStateService = mock()), OwnershipTypeState {
            override val ownsPropertyStep = mock<OwnsPropertyStep> { on { outcome } doReturn ownsProperty }
            override val ownershipKindStep = mock<OwnershipKindStep> { on { outcome } doReturn ownershipKind }
            override val freeholdTypeStep = mock<FreeholdTypeStep> { on { outcome } doReturn freeholdType }
        }

    companion object {
        @JvmStatic
        private fun provideFreeholdTypeScenarios() =
            arrayOf(
                arguments(YesOrNo.YES, OwnershipKind.FREEHOLD_ONLY, FreeholdType.JOINT_TENANTS, true),
                arguments(YesOrNo.YES, OwnershipKind.FREEHOLD_ONLY, FreeholdType.TENANTS_IN_COMMON, true),
                arguments(YesOrNo.YES, OwnershipKind.FREEHOLD_ONLY, FreeholdType.SOLE_OWNERSHIP, false),
                arguments(YesOrNo.YES, OwnershipKind.FREEHOLD_ONLY, FreeholdType.COMMONHOLD, false),
                arguments(YesOrNo.YES, OwnershipKind.FREEHOLD_AND_LEASEHOLD, FreeholdType.JOINT_TENANTS, true),
                arguments(YesOrNo.YES, OwnershipKind.FREEHOLD_AND_LEASEHOLD, FreeholdType.TENANTS_IN_COMMON, true),
                arguments(YesOrNo.YES, OwnershipKind.FREEHOLD_AND_LEASEHOLD, FreeholdType.SOLE_OWNERSHIP, false),
                arguments(YesOrNo.YES, OwnershipKind.FREEHOLD_AND_LEASEHOLD, FreeholdType.COMMONHOLD, false),
                arguments(YesOrNo.YES, OwnershipKind.LEASEHOLD_ONLY, null, false),
                arguments(YesOrNo.NO, null, null, false),
                // The ownership type task is not yet complete, so there is no ownership type to judge
                arguments(YesOrNo.YES, OwnershipKind.FREEHOLD_ONLY, null, false),
                arguments(null, null, null, false),
            )
    }
}
