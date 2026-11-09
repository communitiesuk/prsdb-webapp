package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import uk.gov.communities.prsdb.webapp.constants.enums.OwnershipType
import uk.gov.communities.prsdb.webapp.journeys.AbstractJourneyState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.FreeholdType
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.FreeholdTypeStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.OwnershipKind
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.OwnershipKindStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.OwnsPropertyStep
import uk.gov.communities.prsdb.webapp.journeys.shared.YesOrNo

class OwnershipTypeStateTests {
    @ParameterizedTest(name = "{1} when freeholdType is {0}")
    @MethodSource("provideFreeholdTypeScenarios")
    fun `isJointFreeholder returns`(
        freeholdType: FreeholdType?,
        expectedResult: Boolean,
    ) {
        // Arrange
        val state = buildTestOwnershipTypeState(freeholdType)

        // Act & Assert
        assertEquals(expectedResult, state.isJointFreeholder)
    }

    @ParameterizedTest(name = "{3} when ownsProperty is {0}, ownershipKind is {1} and freeholdType is {2}")
    @MethodSource("provideOwnershipTypeScenarios")
    fun `ownershipType returns`(
        ownsProperty: YesOrNo?,
        ownershipKind: OwnershipKind?,
        freeholdType: FreeholdType?,
        expectedResult: OwnershipType?,
    ) {
        // Arrange
        val state = buildTestOwnershipTypeState(freeholdType, ownsProperty, ownershipKind)

        // Act & Assert
        assertEquals(expectedResult, state.ownershipType)
    }

    private fun buildTestOwnershipTypeState(
        freeholdType: FreeholdType?,
        ownsProperty: YesOrNo? = null,
        ownershipKind: OwnershipKind? = null,
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
                arguments(FreeholdType.JOINT_TENANTS, true),
                arguments(FreeholdType.TENANTS_IN_COMMON, true),
                arguments(FreeholdType.SOLE_OWNERSHIP, false),
                arguments(FreeholdType.COMMONHOLD, false),
                arguments(null, false),
            )

        @JvmStatic
        private fun provideOwnershipTypeScenarios() =
            arrayOf(
                // Does not own the property
                arguments(YesOrNo.NO, null, null, OwnershipType.NONE),
                // Leasehold only
                arguments(YesOrNo.YES, OwnershipKind.LEASEHOLD_ONLY, null, OwnershipType.LEASEHOLD_ONLY),
                // Freehold only
                arguments(YesOrNo.YES, OwnershipKind.FREEHOLD_ONLY, FreeholdType.SOLE_OWNERSHIP, OwnershipType.SOLE_FREEHOLD),
                arguments(YesOrNo.YES, OwnershipKind.FREEHOLD_ONLY, FreeholdType.JOINT_TENANTS, OwnershipType.JOINT_TENANTS),
                arguments(YesOrNo.YES, OwnershipKind.FREEHOLD_ONLY, FreeholdType.TENANTS_IN_COMMON, OwnershipType.TENANTS_IN_COMMON),
                arguments(YesOrNo.YES, OwnershipKind.FREEHOLD_ONLY, FreeholdType.COMMONHOLD, OwnershipType.COMMONHOLD),
                // Freehold and leasehold
                arguments(
                    YesOrNo.YES,
                    OwnershipKind.FREEHOLD_AND_LEASEHOLD,
                    FreeholdType.SOLE_OWNERSHIP,
                    OwnershipType.SHARE_OF_SOLE_FREEHOLD,
                ),
                arguments(
                    YesOrNo.YES,
                    OwnershipKind.FREEHOLD_AND_LEASEHOLD,
                    FreeholdType.JOINT_TENANTS,
                    OwnershipType.SHARE_OF_JOINT_TENANTS,
                ),
                arguments(
                    YesOrNo.YES,
                    OwnershipKind.FREEHOLD_AND_LEASEHOLD,
                    FreeholdType.TENANTS_IN_COMMON,
                    OwnershipType.SHARE_OF_TENANTS_IN_COMMON,
                ),
                arguments(
                    YesOrNo.YES,
                    OwnershipKind.FREEHOLD_AND_LEASEHOLD,
                    FreeholdType.COMMONHOLD,
                    OwnershipType.SHARE_OF_COMMONHOLD,
                ),
                // Incomplete answers map to null
                arguments(null, null, null, null),
                arguments(YesOrNo.YES, null, null, null),
                arguments(YesOrNo.YES, OwnershipKind.FREEHOLD_ONLY, null, null),
                arguments(YesOrNo.YES, OwnershipKind.FREEHOLD_AND_LEASEHOLD, null, null),
            )
    }
}
