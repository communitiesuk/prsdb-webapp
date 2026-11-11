package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.PropertyRegistrationJourneyState
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.AlwaysTrueValidator

@ExtendWith(MockitoExtension::class)
class PaymentFailedStepConfigTests {
    @Mock
    lateinit var mockState: PropertyRegistrationJourneyState

    @Test
    fun `chooseTemplate returns payment not completed template when payment outcome is NOT_COMPLETED`() {
        // Arrange
        val stepConfig = setupStepConfig()
        whenever(mockState.paymentOutcome).thenReturn(PaymentOutcome.NOT_COMPLETED)

        // Act
        val result = stepConfig.chooseTemplate(mockState)

        // Assert
        assertEquals("forms/paymentNotCompletedForm", result)
    }

    @Test
    fun `chooseTemplate returns payment failed template when payment outcome is FAILURE`() {
        // Arrange
        val stepConfig = setupStepConfig()
        whenever(mockState.paymentOutcome).thenReturn(PaymentOutcome.FAILURE)

        // Act
        val result = stepConfig.chooseTemplate(mockState)

        // Assert
        assertEquals("forms/paymentFailedForm", result)
    }

    private fun setupStepConfig(): PaymentFailedStepConfig {
        val stepConfig = PaymentFailedStepConfig()
        stepConfig.urlPath = PaymentFailedStep.ROUTE_SEGMENT
        stepConfig.validator = AlwaysTrueValidator()
        return stepConfig
    }
}
