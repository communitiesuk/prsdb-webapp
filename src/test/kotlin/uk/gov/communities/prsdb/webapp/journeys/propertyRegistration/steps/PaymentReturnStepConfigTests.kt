package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.PropertyRegistrationJourneyState
import uk.gov.communities.prsdb.webapp.services.PaymentReferenceParameterService

@ExtendWith(MockitoExtension::class)
class PaymentReturnStepConfigTests {
    @Mock
    private lateinit var mockPaymentReferenceParameterService: PaymentReferenceParameterService

    @Mock
    private lateinit var mockState: PropertyRegistrationJourneyState

    private lateinit var stepConfig: PaymentReturnStepConfig

    @BeforeEach
    fun setUp() {
        stepConfig = PaymentReturnStepConfig(mockPaymentReferenceParameterService)
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
}
