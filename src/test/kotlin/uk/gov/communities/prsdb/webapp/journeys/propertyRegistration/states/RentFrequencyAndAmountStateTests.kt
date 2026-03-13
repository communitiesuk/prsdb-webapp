package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Named
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.enums.RentFrequency
import uk.gov.communities.prsdb.webapp.helpers.RentDataHelper
import uk.gov.communities.prsdb.webapp.journeys.AbstractJourneyState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.RentAmountStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.RentFrequencyStep
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.RentAmountFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.RentFrequencyFormModel
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockMessageSource

class RentFrequencyAndAmountStateTests {
    private val mockMessageSource = MockMessageSource()

    @ParameterizedTest(name = "{1} when rentFrequency is {0}")
    @MethodSource("provideRentFrequencyScenarios")
    fun `getCustomRentFrequencyIfSelected returns`(
        expectedResult: String?,
        rentFrequencyValue: RentFrequency,
        customRentFrequencyValue: String,
    ) {
        // Arrange
        val rentFrequencyFormModel =
            RentFrequencyFormModel().apply {
                rentFrequency = rentFrequencyValue
                customRentFrequency = customRentFrequencyValue
            }
        val state =
            buildTestRentFrequencyAndAmountState(
                rentFrequencyValue = rentFrequencyValue,
                customRentFrequencyValue = customRentFrequencyValue,
            )

        // Act & Assert
        assertEquals(expectedResult, state.getCustomRentFrequencyIfSelected())
    }

    @Test
    fun `getRentAmount returns string from RentDataHelper#getRentAmount when rentFrequency`() {
        // Arrange
        val rentFrequencyValue = RentFrequency.MONTHLY
        val rentAmountValue = "100"

        val state = buildTestRentFrequencyAndAmountState(rentFrequencyValue = rentFrequencyValue, rentAmountValue = rentAmountValue)
        val expectedRentAmountString =
            RentDataHelper.getRentAmount(
                rentAmount = rentAmountValue,
                rentFrequency = rentFrequencyValue,
                messageSource = mockMessageSource,
            )

        // Act
        val result = state.getRentAmount(messageSource = mockMessageSource)

        // Assert
        assertEquals(expectedRentAmountString, result)
    }

    @Test
    fun `getRentAmount returns string from RentDataHelper#getRentAmount when rentFrequency is custom`() {
        // Arrange
        val rentFrequencyValue = RentFrequency.OTHER
        val customRentFrequencyValue = "Daily"
        val rentAmountValue = "100"

        val state = buildTestRentFrequencyAndAmountState(rentFrequencyValue, customRentFrequencyValue, rentAmountValue)
        val expectedRentAmountString =
            RentDataHelper.getRentAmount(
                rentAmount = rentAmountValue,
                rentFrequency = rentFrequencyValue,
                messageSource = mockMessageSource,
            )

        // Act
        val result = state.getRentAmount(messageSource = mockMessageSource)

        // Assert
        assertEquals(expectedRentAmountString, result)
    }

    private fun buildTestRentFrequencyAndAmountState(
        rentFrequencyValue: RentFrequency = RentFrequency.MONTHLY,
        customRentFrequencyValue: String = "",
        rentAmountValue: String = "100",
    ): RentFrequencyAndAmountState {
        val rentFrequencyFormModel =
            RentFrequencyFormModel().apply {
                rentFrequency = rentFrequencyValue
                customRentFrequency = customRentFrequencyValue
            }
        val rentAmountFormModel =
            RentAmountFormModel().apply {
                rentAmount = rentAmountValue
            }
        return object : AbstractJourneyState(journeyStateService = mock()), RentFrequencyAndAmountState {
            override val rentFrequency =
                mock<RentFrequencyStep>().apply {
                    whenever(this.formModel).thenReturn(rentFrequencyFormModel)
                }

            override val rentAmount =
                mock<RentAmountStep>().apply {
                    whenever(this.formModel).thenReturn(rentAmountFormModel)
                }
        }
    }

    companion object {
        @JvmStatic
        private fun provideRentFrequencyScenarios() =
            arrayOf(
                arguments("Custom rent frequency", Named.of("custom", RentFrequency.OTHER), "custom rent frequency"),
                arguments(null, Named.of("weekly", RentFrequency.WEEKLY), ""),
                arguments(null, Named.of("four weekly", RentFrequency.FOUR_WEEKLY), ""),
                arguments(null, Named.of("monthly", RentFrequency.MONTHLY), ""),
            )
    }
}
