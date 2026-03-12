package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.journeys.AbstractJourneyState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.BillsIncludedStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.RentIncludesBillsStep
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.RentIncludesBillsFormModel

class RentIncludesBillsStateTests {
    @ParameterizedTest(name = "{1} when rentIncludesBills is {0}")
    @MethodSource("provideRentIncludesBillsScenarios")
    fun `doesRentIncludeBills returns`(
        rentIncludesBillsValue: Boolean?,
        expectedResult: Boolean,
    ) {
        // Arrange
        val rentIncludesBillsFormModel =
            RentIncludesBillsFormModel().apply {
                rentIncludesBills = rentIncludesBillsValue
            }
        val state = buildTestRentIncludesBillsState(rentIncludesBillsFormModel = rentIncludesBillsFormModel)

        // Act & Assert
        assertEquals(expectedResult, state.doesRentIncludeBills())
    }

    @Test
    fun `doesRentIncludeBills returns false when formModelOrNull is null`() {
        // Arrange
        val state = buildTestRentIncludesBillsState(formModelShouldBeNull = true)

        // Act & Assert
        assertFalse(state.doesRentIncludeBills())
    }

    private fun buildTestRentIncludesBillsState(
        rentIncludesBillsFormModel: RentIncludesBillsFormModel = RentIncludesBillsFormModel(),
        formModelShouldBeNull: Boolean = false,
    ): RentIncludesBillsState =
        object : AbstractJourneyState(journeyStateService = mock()), RentIncludesBillsState {
            override val rentIncludesBills =
                mock<RentIncludesBillsStep>().apply {
                    if (formModelShouldBeNull) {
                        whenever(this.formModelOrNull).thenReturn(null)
                    } else {
                        whenever(this.formModelOrNull).thenReturn(rentIncludesBillsFormModel)
                    }
                }

            override val billsIncluded = mock<BillsIncludedStep>()
        }

    companion object {
        @JvmStatic
        private fun provideRentIncludesBillsScenarios() =
            arrayOf(
                arguments(true, true),
                arguments(false, false),
                arguments(null, false),
            )
    }
}
