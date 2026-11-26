package uk.gov.communities.prsdb.webapp.journeys.shared.payment

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.journeys.shared.states.PaymentState
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.AlwaysTrueValidator

@ExtendWith(MockitoExtension::class)
class PaymentFailedStepConfigTests {
    @Mock
    lateinit var mockState: PaymentState

    @Mock
    lateinit var mockDependencies: PaymentDependencies

    @Test
    fun `getStepSpecificContent includes the failed and not completed outcome text from the journey`() {
        // Arrange
        val stepConfig = setupStepConfig()
        whenever(mockState.dependencies).thenReturn(mockDependencies)
        whenever(mockDependencies.paymentContentKeys)
            .thenReturn(PaymentContentKeys("summary.inset", "failed.outcome", "notCompleted.outcome", "notCompleted.whatToDo"))

        // Act
        val content = stepConfig.getStepSpecificContent(mockState)

        // Assert
        assertEquals("failed.outcome", content["failedOutcomeText"])
        assertEquals("notCompleted.outcome", content["notCompletedOutcomeText"])
        assertEquals("notCompleted.whatToDo", content["notCompletedWhatYouNeedToDoText"])
    }

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
