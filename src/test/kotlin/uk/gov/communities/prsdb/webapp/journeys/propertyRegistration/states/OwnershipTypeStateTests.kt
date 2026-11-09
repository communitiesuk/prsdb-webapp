package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import uk.gov.communities.prsdb.webapp.constants.enums.OwnershipType
import uk.gov.communities.prsdb.webapp.helpers.converters.OwnershipTypeAnswersConverter
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

    @ParameterizedTest
    @EnumSource(OwnershipType::class)
    fun `ownershipType delegates to OwnershipTypeAnswersConverter`(ownershipType: OwnershipType) {
        // Arrange
        val answers = OwnershipTypeAnswersConverter.convert(ownershipType)
        val state = buildTestOwnershipTypeState(answers.freeholdType, answers.ownsProperty, answers.ownershipKind)

        // Act & Assert
        assertEquals(ownershipType, state.ownershipType)
    }

    @Test
    fun `ownershipType returns null when the ownership type task is unanswered`() {
        // Arrange
        val state = buildTestOwnershipTypeState(freeholdType = null)

        // Act & Assert
        assertNull(state.ownershipType)
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
    }
}
