package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.exceptions.GovUkPayException
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.PropertyRegistrationJourneyState
import uk.gov.communities.prsdb.webapp.services.PaymentReferenceParameterService
import kotlin.test.assertIs

@ExtendWith(MockitoExtension::class)
class PaymentReturnStepConfigTests {
    @Mock
    private lateinit var mockPaymentReferenceParameterService: PaymentReferenceParameterService

    @Mock
    private lateinit var mockState: PropertyRegistrationJourneyState

    @Mock
    private lateinit var mockPaymentStatusCheckStep: PaymentStatusCheckStep

    private lateinit var stepConfig: PaymentReturnStepConfig

    @BeforeEach
    fun setUp() {
        stepConfig = PaymentReturnStepConfig(mockPaymentReferenceParameterService)
        stepConfig.urlPath = PaymentReturnStep.ROUTE_SEGMENT
    }

    @Test
    fun `beforeAttemptingToReachStep returns true when the URL has a payment reference`() {
        // Arrange
        whenever(mockPaymentReferenceParameterService.getParameterOrNull()).thenReturn("payment-reference")

        // Act
        val result = stepConfig.beforeAttemptingToReachStep(mockState)

        // Assert
        assertTrue(result)
    }

    @Test
    fun `beforeAttemptingToReachStep returns true when the URL does not have a payment reference but the journey state does`() {
        // Arrange
        whenever(mockPaymentReferenceParameterService.getParameterOrNull()).thenReturn(null)
        whenever(mockState.paymentReference).thenReturn("payment-reference")

        // Act
        val result = stepConfig.beforeAttemptingToReachStep(mockState)

        // Assert
        assertTrue(result)
    }

    @Test
    fun `beforeAttemptingToReachStep returns false when neither the URL nor the journey state has a payment reference`() {
        // Arrange
        whenever(mockPaymentReferenceParameterService.getParameterOrNull()).thenReturn(null)
        whenever(mockState.paymentReference).thenReturn(null)

        // Act
        val result = stepConfig.beforeAttemptingToReachStep(mockState)

        // Assert
        assertFalse(result)
    }

    @Test
    fun `afterStepIsReached stores the payment reference from the URL in the journey state`() {
        // Arrange
        whenever(mockPaymentReferenceParameterService.getParameterOrNull()).thenReturn("payment-reference")

        // Act
        stepConfig.afterStepIsReached(mockState)

        // Assert
        verify(mockState).paymentReference = "payment-reference"
    }

    @Test
    fun `afterStepIsReached keeps the stored payment reference when the URL does not have one`() {
        // Arrange
        whenever(mockPaymentReferenceParameterService.getParameterOrNull()).thenReturn(null)

        // Act
        stepConfig.afterStepIsReached(mockState)

        // Assert
        verify(mockState, never()).paymentReference = anyOrNull()
    }

    @Test
    fun `chooseTemplate returns the payment processing template`() {
        // Act
        val template = stepConfig.chooseTemplate(mockState)

        // Assert
        assertEquals(PAYMENT_PROCESSING_TEMPLATE, template)
    }

    @ParameterizedTest
    @EnumSource(PaymentOutcome::class, names = ["SUCCESS", "FAILURE"])
    fun `resolveChosenTemplate returns the payment status check destination when the payment has finished`(outcome: PaymentOutcome) {
        // Arrange
        val outcomeDestination = Destination.ExternalUrl("outcome-url")
        whenever(mockState.paymentStatusCheckStep).thenReturn(mockPaymentStatusCheckStep)
        whenever(mockPaymentStatusCheckStep.attemptToReachStep()).thenReturn(true)
        whenever(mockState.paymentOutcome).thenReturn(outcome)
        whenever(mockPaymentStatusCheckStep.getNextDestination()).thenReturn(outcomeDestination)

        // Act
        val result = stepConfig.resolveChosenTemplate(mockState, PAYMENT_PROCESSING_TEMPLATE)

        // Assert
        assertSame(outcomeDestination, result)
    }

    @Test
    fun `resolveChosenTemplate marks the payment return step complete so the payment status can be checked`() {
        // Arrange
        whenever(mockState.paymentStatusCheckStep).thenReturn(mockPaymentStatusCheckStep)
        whenever(mockPaymentStatusCheckStep.attemptToReachStep()).thenReturn(true)
        whenever(mockState.paymentOutcome).thenReturn(PaymentOutcome.IN_PROGRESS)

        // Act
        stepConfig.resolveChosenTemplate(mockState, PAYMENT_PROCESSING_TEMPLATE)

        // Assert
        verify(mockState).addStepData(PaymentReturnStep.ROUTE_SEGMENT, mapOf())
    }

    @Test
    fun `resolveChosenTemplate returns the payment processing template when the payment is still in progress`() {
        // Arrange
        whenever(mockState.paymentStatusCheckStep).thenReturn(mockPaymentStatusCheckStep)
        whenever(mockPaymentStatusCheckStep.attemptToReachStep()).thenReturn(true)
        whenever(mockState.paymentOutcome).thenReturn(PaymentOutcome.IN_PROGRESS)

        // Act
        val result = stepConfig.resolveChosenTemplate(mockState, PAYMENT_PROCESSING_TEMPLATE)

        // Assert
        assertEquals(PAYMENT_PROCESSING_TEMPLATE, assertIs<Destination.Template>(result).templateName)
        verify(mockPaymentStatusCheckStep, never()).getNextDestination()
    }

    @Test
    fun `resolveChosenTemplate returns the payment processing template when the payment status check is unreachable`() {
        // Arrange
        whenever(mockState.paymentStatusCheckStep).thenReturn(mockPaymentStatusCheckStep)
        whenever(mockPaymentStatusCheckStep.attemptToReachStep()).thenReturn(false)

        // Act
        val result = stepConfig.resolveChosenTemplate(mockState, PAYMENT_PROCESSING_TEMPLATE)

        // Assert
        assertEquals(PAYMENT_PROCESSING_TEMPLATE, assertIs<Destination.Template>(result).templateName)
        verify(mockPaymentStatusCheckStep, never()).getNextDestination()
    }

    @Test
    fun `resolveChosenTemplate returns the payment processing template when GOV UK Pay cannot be reached`() {
        // Arrange
        whenever(mockState.paymentStatusCheckStep).thenReturn(mockPaymentStatusCheckStep)
        whenever(mockPaymentStatusCheckStep.attemptToReachStep()).thenThrow(GovUkPayException("GOV.UK Pay is down"))

        // Act
        val result = stepConfig.resolveChosenTemplate(mockState, PAYMENT_PROCESSING_TEMPLATE)

        // Assert
        assertEquals(PAYMENT_PROCESSING_TEMPLATE, assertIs<Destination.Template>(result).templateName)
    }

    companion object {
        private const val PAYMENT_PROCESSING_TEMPLATE = "forms/paymentProcessingForm"
    }
}
