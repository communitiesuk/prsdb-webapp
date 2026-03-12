package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.journeys.AbstractJourneyState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.BillsIncludedStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.RentIncludesBillsStep
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.RentIncludesBillsFormModel

class RentIncludesBillsStateTests {
    @Test
    fun `doesRentIncludeBills returns true when rentIncludesBills is true`() {
        // Arrange
        val rentIncludesBillsFormModel =
            RentIncludesBillsFormModel().apply {
                rentIncludesBills = true
            }
        val state = buildTestRentIncludesBillsState(rentIncludesBillsFormModel = rentIncludesBillsFormModel)

        // Act & Assert
        assertTrue(state.doesRentIncludeBills())
    }

    @Test
    fun `doesRentIncludeBills returns false when rentIncludesBills is false`() {
        // Arrange
        val rentIncludesBillsFormModel =
            RentIncludesBillsFormModel().apply {
                rentIncludesBills = false
            }
        val state = buildTestRentIncludesBillsState(rentIncludesBillsFormModel = rentIncludesBillsFormModel)

        // Act & Assert
        assertFalse(state.doesRentIncludeBills())
    }

    @Test
    fun `doesRentIncludeBills returns false when rentIncludesBills is null`() {
        // Arrange
        val rentIncludesBillsFormModel =
            RentIncludesBillsFormModel().apply {
                rentIncludesBills = null
            }
        val state = buildTestRentIncludesBillsState(rentIncludesBillsFormModel = rentIncludesBillsFormModel)

        // Act & Assert
        assertFalse(state.doesRentIncludeBills())
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
}
