package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNull
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.enums.BillsIncluded
import uk.gov.communities.prsdb.webapp.helpers.BillsIncludedHelper
import uk.gov.communities.prsdb.webapp.journeys.AbstractJourneyState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.BillsIncludedStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.RentIncludesBillsStep
import uk.gov.communities.prsdb.webapp.models.dataModels.BillsIncludedDataModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.BillsIncludedFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.RentIncludesBillsFormModel
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockMessageSource

class RentIncludesBillsStateTests {
    private val mockMessageSource = MockMessageSource()

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
        val state = buildTestRentIncludesBillsState(rentIncludesBillsFormModelShouldBeNull = true)

        // Act & Assert
        assertFalse(state.doesRentIncludeBills())
    }

    @Test
    fun `getBillsIncludedOrNull returns null when doesRentIncludeBills is false`() {
        // Arrange
        val rentIncludesBillsFormModel =
            RentIncludesBillsFormModel().apply {
                rentIncludesBills = false
            }
        val state = buildTestRentIncludesBillsState(rentIncludesBillsFormModel = rentIncludesBillsFormModel)

        // Act & Assert
        assertNull(state.getBillsIncludedOrNull())
    }

    @Test
    fun `getBillsIncludedOrNull returns null when doesRentIncludeBills is true but billsIncluded formModelOrNull is null`() {
        // Arrange
        val rentIncludesBillsFormModel =
            RentIncludesBillsFormModel().apply {
                rentIncludesBills = true
            }
        val state =
            buildTestRentIncludesBillsState(
                rentIncludesBillsFormModel = rentIncludesBillsFormModel,
                billsIncludedFormModelShouldBeNull = true,
            )

        // Act & Assert
        assertNull(state.getBillsIncludedOrNull())
    }

    @Test
    fun `getBillsIncludedOrNull returns data model when doesRentIncludeBills is true and billsIncluded formModel is not null`() {
        // Arrange
        val rentIncludesBillsFormModel =
            RentIncludesBillsFormModel().apply {
                rentIncludesBills = true
            }
        val billsIncludedFormModel =
            BillsIncludedFormModel().apply {
                billsIncluded = mutableListOf(BillsIncluded.ELECTRICITY.toString(), BillsIncluded.GAS.toString())
            }
        val state =
            buildTestRentIncludesBillsState(
                rentIncludesBillsFormModel = rentIncludesBillsFormModel,
                billsIncludedFormModel = billsIncludedFormModel,
            )
        val expectedBillsIncludedDataModel = BillsIncludedDataModel.fromFormData(billsIncludedFormModel)

        // Act
        val result = state.getBillsIncludedOrNull()

        // Assert
        assertEquals(expectedBillsIncludedDataModel, result)
    }

    @Test
    fun `getBillsIncluded returns string from BillsIncludedHelper#getBillsIncludedForCYAStep`() {
        // Arrange
        val rentIncludesBillsFormModel =
            RentIncludesBillsFormModel().apply {
                rentIncludesBills = true
            }
        val billsIncludedFormModel =
            BillsIncludedFormModel().apply {
                billsIncluded = mutableListOf(BillsIncluded.ELECTRICITY.toString(), BillsIncluded.GAS.toString())
            }
        val state =
            buildTestRentIncludesBillsState(
                rentIncludesBillsFormModel = rentIncludesBillsFormModel,
                billsIncludedFormModel = billsIncludedFormModel,
            )
        val expectedBillsIncludedString =
            BillsIncludedHelper.getBillsIncludedForCYAStep(
                BillsIncludedDataModel.fromFormData(billsIncludedFormModel),
                messageSource = mockMessageSource,
            )

        // Act
        val result = state.getBillsIncluded(messageSource = mockMessageSource)

        // Assert
        assertEquals(expectedBillsIncludedString, result)
    }

    private fun buildTestRentIncludesBillsState(
        rentIncludesBillsFormModel: RentIncludesBillsFormModel = RentIncludesBillsFormModel(),
        rentIncludesBillsFormModelShouldBeNull: Boolean = false,
        billsIncludedFormModel: BillsIncludedFormModel = BillsIncludedFormModel(),
        billsIncludedFormModelShouldBeNull: Boolean = false,
    ): RentIncludesBillsState =
        object : AbstractJourneyState(journeyStateService = mock()), RentIncludesBillsState {
            override val rentIncludesBills =
                mock<RentIncludesBillsStep>().apply {
                    if (rentIncludesBillsFormModelShouldBeNull) {
                        whenever(this.formModelOrNull).thenReturn(null)
                    } else {
                        whenever(this.formModelOrNull).thenReturn(rentIncludesBillsFormModel)
                    }
                }

            override val billsIncluded =
                mock<BillsIncludedStep>().apply {
                    if (billsIncludedFormModelShouldBeNull) {
                        whenever(this.formModelOrNull).thenReturn(null)
                    } else {
                        whenever(this.formModelOrNull).thenReturn(billsIncludedFormModel)
                    }
                }
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
