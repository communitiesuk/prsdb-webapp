package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

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
