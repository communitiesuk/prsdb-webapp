package uk.gov.communities.prsdb.webapp.constants.enums

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource

class OwnershipTypeTests {
    @ParameterizedTest(name = "{1} for {0}")
    @MethodSource("provideFreeholderScenarios")
    fun `requiresFreeholderRecords returns`(
        ownershipType: OwnershipType,
        expectedResult: Boolean,
    ) {
        // Act & Assert
        assertEquals(expectedResult, ownershipType.requiresFreeholderRecords())
    }

    @ParameterizedTest(name = "{1} for {0}")
    @MethodSource("provideSuperiorLandlordScenarios")
    fun `requiresSuperiorLandlordInfo returns`(
        ownershipType: OwnershipType,
        expectedResult: Boolean,
    ) {
        // Act & Assert
        assertEquals(expectedResult, ownershipType.requiresSuperiorLandlordInfo())
    }

    @ParameterizedTest(name = "{1} for {0}")
    @MethodSource("provideJointFreeholderScenarios")
    fun `isJointFreeholder returns`(
        ownershipType: OwnershipType,
        expectedResult: Boolean,
    ) {
        // Act & Assert
        assertEquals(expectedResult, ownershipType.isJointFreeholder())
    }

    companion object {
        @JvmStatic
        private fun provideFreeholderScenarios() =
            arrayOf(
                arguments(OwnershipType.NONE, true),
                arguments(OwnershipType.LEASEHOLD_ONLY, true),
                arguments(OwnershipType.SOLE_FREEHOLD, false),
                arguments(OwnershipType.JOINT_TENANTS, true),
                arguments(OwnershipType.TENANTS_IN_COMMON, true),
                arguments(OwnershipType.COMMONHOLD, false),
                arguments(OwnershipType.SHARE_OF_SOLE_FREEHOLD, false),
                arguments(OwnershipType.SHARE_OF_JOINT_TENANTS, true),
                arguments(OwnershipType.SHARE_OF_TENANTS_IN_COMMON, true),
                arguments(OwnershipType.SHARE_OF_COMMONHOLD, false),
            )

        @JvmStatic
        private fun provideSuperiorLandlordScenarios() =
            arrayOf(
                arguments(OwnershipType.NONE, true),
                arguments(OwnershipType.LEASEHOLD_ONLY, true),
                arguments(OwnershipType.SOLE_FREEHOLD, false),
                arguments(OwnershipType.JOINT_TENANTS, false),
                arguments(OwnershipType.TENANTS_IN_COMMON, false),
                arguments(OwnershipType.COMMONHOLD, false),
                arguments(OwnershipType.SHARE_OF_SOLE_FREEHOLD, true),
                arguments(OwnershipType.SHARE_OF_JOINT_TENANTS, true),
                arguments(OwnershipType.SHARE_OF_TENANTS_IN_COMMON, true),
                arguments(OwnershipType.SHARE_OF_COMMONHOLD, true),
            )

        @JvmStatic
        private fun provideJointFreeholderScenarios() =
            arrayOf(
                arguments(OwnershipType.NONE, false),
                arguments(OwnershipType.LEASEHOLD_ONLY, false),
                arguments(OwnershipType.SOLE_FREEHOLD, false),
                arguments(OwnershipType.JOINT_TENANTS, true),
                arguments(OwnershipType.TENANTS_IN_COMMON, true),
                arguments(OwnershipType.COMMONHOLD, false),
                arguments(OwnershipType.SHARE_OF_SOLE_FREEHOLD, false),
                arguments(OwnershipType.SHARE_OF_JOINT_TENANTS, true),
                arguments(OwnershipType.SHARE_OF_TENANTS_IN_COMMON, true),
                arguments(OwnershipType.SHARE_OF_COMMONHOLD, false),
            )
    }
}
