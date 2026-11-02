package uk.gov.communities.prsdb.webapp.helpers.converters

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.MethodSource
import uk.gov.communities.prsdb.webapp.constants.enums.OwnershipType
import uk.gov.communities.prsdb.webapp.constants.enums.OwnershipTypeBeforePdjb955

// TODO PDJB-2019: delete this whole class along with OwnershipTypeConverter, once the legacy
//  enum and column are gone.
class OwnershipTypeConverterTests {
    @ParameterizedTest
    @MethodSource("provideLegacyToCurrentMappings")
    fun `convert maps each legacy ownership type to its replacement`(
        legacy: OwnershipTypeBeforePdjb955,
        expected: OwnershipType,
    ) {
        // Act
        val result = OwnershipTypeConverter.convert(legacy)

        // Assert
        assertEquals(expected, result)
    }

    @ParameterizedTest
    @EnumSource(OwnershipTypeBeforePdjb955::class)
    fun `convert returns a value for every legacy ownership type`(legacy: OwnershipTypeBeforePdjb955) {
        // Act, Assert - an exhaustive when means this cannot throw, but it guards against
        // a future legacy value being added without a mapping
        OwnershipTypeConverter.convert(legacy)
    }

    @ParameterizedTest
    @MethodSource("provideLegacyToCurrentMappings")
    fun `convert maps to the ordinals the V1_61_0 migration backfills`(
        legacy: OwnershipTypeBeforePdjb955,
        expected: OwnershipType,
    ) {
        // Arrange - these pairs are duplicated in the CASE expression in
        // V1_61_0__add_ownership_type_and_associated_party_tables.sql and in every seed script. If this fails,
        // the enum declaration order has changed and that SQL is now wrong.
        val expectedOrdinalPairs = mapOf(0 to 2, 1 to 1, 2 to 3, 3 to 5)

        // Act, Assert
        assertEquals(expectedOrdinalPairs[legacy.ordinal], expected.ordinal)
    }

    companion object {
        @JvmStatic
        fun provideLegacyToCurrentMappings() =
            listOf(
                Arguments.of(OwnershipTypeBeforePdjb955.FREEHOLD, OwnershipType.SOLE_FREEHOLD),
                Arguments.of(OwnershipTypeBeforePdjb955.LEASEHOLD, OwnershipType.LEASEHOLD_ONLY),
                Arguments.of(OwnershipTypeBeforePdjb955.SHARE_OF_FREEHOLD, OwnershipType.JOINT_TENANTS),
                Arguments.of(OwnershipTypeBeforePdjb955.COMMONHOLD, OwnershipType.COMMONHOLD),
            )
    }
}
