package uk.gov.communities.prsdb.webapp.helpers.converters

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.MethodSource
import uk.gov.communities.prsdb.webapp.constants.enums.OwnershipType
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.FreeholdType
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.OwnershipKind
import uk.gov.communities.prsdb.webapp.journeys.shared.YesOrNo

class OwnershipTypeAnswersConverterTests {
    @ParameterizedTest(name = "{0}")
    @MethodSource("provideOwnershipTypeAnswers")
    fun `convert maps each ownership type back to the answers that produce it`(
        ownershipType: OwnershipType,
        expected: OwnershipTypeAnswers,
    ) {
        // Act
        val result = OwnershipTypeAnswersConverter.convert(ownershipType)

        // Assert
        assertEquals(expected, result)
    }

    @ParameterizedTest
    @EnumSource(OwnershipType::class)
    fun `convert returns answers for every ownership type`(ownershipType: OwnershipType) {
        // Act, Assert - an exhaustive when means this cannot throw, but it guards against a future
        // ownership type being added without a mapping
        OwnershipTypeAnswersConverter.convert(ownershipType)
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("provideOwnershipTypeAnswers")
    fun `convert only leaves a later answer unanswered when that question is not asked`(
        ownershipType: OwnershipType,
        expected: OwnershipTypeAnswers,
    ) {
        // Act
        val result = OwnershipTypeAnswersConverter.convert(ownershipType)

        // Assert - ownershipKind is only unasked when the landlord does not own the property, and
        // freeholdType only when there is no freehold to describe
        assertEquals(result.ownsProperty == YesOrNo.YES, result.ownershipKind != null)
        assertEquals(result.ownershipKind != null && result.ownershipKind != OwnershipKind.LEASEHOLD_ONLY, result.freeholdType != null)
        assertEquals(expected, result)
    }

    @ParameterizedTest(name = "{3} when ownsProperty is {0}, ownershipKind is {1} and freeholdType is {2}")
    @MethodSource("provideAnswerScenarios")
    fun `convert maps the journey answers to an ownership type`(
        ownsProperty: YesOrNo?,
        ownershipKind: OwnershipKind?,
        freeholdType: FreeholdType?,
        expected: OwnershipType?,
    ) {
        // Act
        val result = OwnershipTypeAnswersConverter.convert(ownsProperty, ownershipKind, freeholdType)

        // Assert
        assertEquals(expected, result)
    }

    @ParameterizedTest
    @EnumSource(OwnershipType::class)
    fun `convert round-trips every ownership type through its answers`(ownershipType: OwnershipType) {
        // Arrange
        val answers = OwnershipTypeAnswersConverter.convert(ownershipType)

        // Act
        val result = OwnershipTypeAnswersConverter.convert(answers.ownsProperty, answers.ownershipKind, answers.freeholdType)

        // Assert
        assertEquals(ownershipType, result)
    }

    companion object {
        @JvmStatic
        fun provideOwnershipTypeAnswers() =
            listOf(
                Arguments.of(OwnershipType.NONE, OwnershipTypeAnswers(YesOrNo.NO, null, null)),
                Arguments.of(
                    OwnershipType.LEASEHOLD_ONLY,
                    OwnershipTypeAnswers(YesOrNo.YES, OwnershipKind.LEASEHOLD_ONLY, null),
                ),
                Arguments.of(
                    OwnershipType.SOLE_FREEHOLD,
                    OwnershipTypeAnswers(YesOrNo.YES, OwnershipKind.FREEHOLD_ONLY, FreeholdType.SOLE_OWNERSHIP),
                ),
                Arguments.of(
                    OwnershipType.JOINT_TENANTS,
                    OwnershipTypeAnswers(YesOrNo.YES, OwnershipKind.FREEHOLD_ONLY, FreeholdType.JOINT_TENANTS),
                ),
                Arguments.of(
                    OwnershipType.TENANTS_IN_COMMON,
                    OwnershipTypeAnswers(YesOrNo.YES, OwnershipKind.FREEHOLD_ONLY, FreeholdType.TENANTS_IN_COMMON),
                ),
                Arguments.of(
                    OwnershipType.COMMONHOLD,
                    OwnershipTypeAnswers(YesOrNo.YES, OwnershipKind.FREEHOLD_ONLY, FreeholdType.COMMONHOLD),
                ),
                Arguments.of(
                    OwnershipType.SHARE_OF_SOLE_FREEHOLD,
                    OwnershipTypeAnswers(YesOrNo.YES, OwnershipKind.FREEHOLD_AND_LEASEHOLD, FreeholdType.SOLE_OWNERSHIP),
                ),
                Arguments.of(
                    OwnershipType.SHARE_OF_JOINT_TENANTS,
                    OwnershipTypeAnswers(YesOrNo.YES, OwnershipKind.FREEHOLD_AND_LEASEHOLD, FreeholdType.JOINT_TENANTS),
                ),
                Arguments.of(
                    OwnershipType.SHARE_OF_TENANTS_IN_COMMON,
                    OwnershipTypeAnswers(YesOrNo.YES, OwnershipKind.FREEHOLD_AND_LEASEHOLD, FreeholdType.TENANTS_IN_COMMON),
                ),
                Arguments.of(
                    OwnershipType.SHARE_OF_COMMONHOLD,
                    OwnershipTypeAnswers(YesOrNo.YES, OwnershipKind.FREEHOLD_AND_LEASEHOLD, FreeholdType.COMMONHOLD),
                ),
            )

        @JvmStatic
        fun provideAnswerScenarios() =
            listOf(
                // Does not own the property
                Arguments.of(YesOrNo.NO, null, null, OwnershipType.NONE),
                // Leasehold only
                Arguments.of(YesOrNo.YES, OwnershipKind.LEASEHOLD_ONLY, null, OwnershipType.LEASEHOLD_ONLY),
                // Freehold only
                Arguments.of(YesOrNo.YES, OwnershipKind.FREEHOLD_ONLY, FreeholdType.SOLE_OWNERSHIP, OwnershipType.SOLE_FREEHOLD),
                Arguments.of(YesOrNo.YES, OwnershipKind.FREEHOLD_ONLY, FreeholdType.JOINT_TENANTS, OwnershipType.JOINT_TENANTS),
                Arguments.of(
                    YesOrNo.YES,
                    OwnershipKind.FREEHOLD_ONLY,
                    FreeholdType.TENANTS_IN_COMMON,
                    OwnershipType.TENANTS_IN_COMMON,
                ),
                Arguments.of(YesOrNo.YES, OwnershipKind.FREEHOLD_ONLY, FreeholdType.COMMONHOLD, OwnershipType.COMMONHOLD),
                // Freehold and leasehold
                Arguments.of(
                    YesOrNo.YES,
                    OwnershipKind.FREEHOLD_AND_LEASEHOLD,
                    FreeholdType.SOLE_OWNERSHIP,
                    OwnershipType.SHARE_OF_SOLE_FREEHOLD,
                ),
                Arguments.of(
                    YesOrNo.YES,
                    OwnershipKind.FREEHOLD_AND_LEASEHOLD,
                    FreeholdType.JOINT_TENANTS,
                    OwnershipType.SHARE_OF_JOINT_TENANTS,
                ),
                Arguments.of(
                    YesOrNo.YES,
                    OwnershipKind.FREEHOLD_AND_LEASEHOLD,
                    FreeholdType.TENANTS_IN_COMMON,
                    OwnershipType.SHARE_OF_TENANTS_IN_COMMON,
                ),
                Arguments.of(
                    YesOrNo.YES,
                    OwnershipKind.FREEHOLD_AND_LEASEHOLD,
                    FreeholdType.COMMONHOLD,
                    OwnershipType.SHARE_OF_COMMONHOLD,
                ),
                // Incomplete answers map to null
                Arguments.of(null, null, null, null),
                Arguments.of(YesOrNo.YES, null, null, null),
                Arguments.of(YesOrNo.YES, OwnershipKind.FREEHOLD_ONLY, null, null),
                Arguments.of(YesOrNo.YES, OwnershipKind.FREEHOLD_AND_LEASEHOLD, null, null),
            )
    }
}
