package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.NullSource
import org.mockito.Mock
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.enums.PaymentStatus
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.PropertyRegistrationDataModelFactory
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.PropertyRegistrationJourneyState
import uk.gov.communities.prsdb.webapp.models.dataModels.PaymentStatusCheckDataModel
import uk.gov.communities.prsdb.webapp.services.PaymentService
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockPropertyRegistrationData

@ExtendWith(MockitoExtension::class)
class PaymentStatusCheckStepConfigTests {
    @Mock
    private lateinit var mockPaymentService: PaymentService

    @Mock
    private lateinit var mockPropertyRegistrationDataModelFactory: PropertyRegistrationDataModelFactory

    @Mock
    private lateinit var mockState: PropertyRegistrationJourneyState

    private lateinit var stepConfig: PaymentStatusCheckStepConfig

    private val journeyId = "journey-123"
    private val paymentId = "payment-123"
    private val paymentReference = "payment-reference-123"
    private val registrationData = MockPropertyRegistrationData.createPropertyRegistrationDataModel()
    private val defaultDestination = Destination.ExternalUrl("default")

    @BeforeEach
    fun setUp() {
        stepConfig =
            PaymentStatusCheckStepConfig(
                mockPaymentService,
                mockPropertyRegistrationDataModelFactory,
            )
    }

    @Test
    fun `afterStepIsReached does not finalise a payment that is still in progress on GOV UK Pay`() {
        // Arrange
        stubGovUkPayStatus(PaymentStatus.CREATED)

        // Act
        stepConfig.afterStepIsReached(mockState)

        // Assert
        verify(mockState).paymentOutcome = PaymentOutcome.IN_PROGRESS
        verify(mockPaymentService, never()).finalisePayment(any(), any(), any())
    }

    @ParameterizedTest
    @EnumSource(value = PaymentStatus::class, names = ["CAPTURABLE", "SUCCEEDED", "FAILED", "CANCELLED"])
    fun `afterStepIsReached finalises the payment with the GOV UK Pay status and the journey's registration data`(
        govUkPayStatus: PaymentStatus,
    ) {
        // Arrange
        stubGovUkPayStatus(govUkPayStatus)
        whenever(mockPropertyRegistrationDataModelFactory.fromJourneyState(mockState)).thenReturn(registrationData)
        whenever(mockPaymentService.finalisePayment(paymentId, govUkPayStatus, registrationData))
            .thenReturn(govUkPayStatus)

        // Act
        stepConfig.afterStepIsReached(mockState)

        // Assert
        verify(mockPaymentService).finalisePayment(paymentId, govUkPayStatus, registrationData)
    }

    @ParameterizedTest
    @MethodSource("provideFinalisedPaymentStatusesAndOutcomes")
    fun `afterStepIsReached stores the outcome for the finalised payment status`(
        finalisedPaymentStatus: PaymentStatus,
        expectedOutcome: PaymentOutcome,
    ) {
        // Arrange
        stubGovUkPayStatus(PaymentStatus.CAPTURABLE)
        whenever(mockPropertyRegistrationDataModelFactory.fromJourneyState(mockState)).thenReturn(registrationData)
        whenever(mockPaymentService.finalisePayment(paymentId, PaymentStatus.CAPTURABLE, registrationData))
            .thenReturn(finalisedPaymentStatus)

        // Act
        stepConfig.afterStepIsReached(mockState)

        // Assert
        verify(mockState).paymentOutcome = expectedOutcome
    }

    @Test
    fun `afterStepIsReached checks the status of the payment the user has returned from`() {
        // Arrange
        stubGovUkPayStatus(PaymentStatus.CREATED)

        // Act
        stepConfig.afterStepIsReached(mockState)

        // Assert
        verify(mockPaymentService).getPropertyRegistrationPaymentStatus(journeyId, paymentReference)
    }

    @Test
    fun `afterStepIsReached throws without checking any payment when the journey has no payment reference`() {
        // Arrange
        whenever(mockState.journeyId).thenReturn(journeyId)
        whenever(mockState.paymentReference).thenReturn(null)

        // Act & Assert
        assertThrows<IllegalStateException> { stepConfig.afterStepIsReached(mockState) }
        verifyNoInteractions(mockPaymentService)
    }

    @ParameterizedTest
    @NullSource
    @EnumSource(PaymentOutcome::class)
    fun `mode returns the payment outcome stored in the state`(paymentOutcome: PaymentOutcome?) {
        // Arrange
        whenever(mockState.paymentOutcome).thenReturn(paymentOutcome)

        // Act
        val result = stepConfig.mode(mockState)

        // Assert
        assertEquals(paymentOutcome, result)
    }

    @Test
    fun `mode does not call the payment service`() {
        // Arrange
        whenever(mockState.paymentOutcome).thenReturn(PaymentOutcome.SUCCESS)

        // Act
        stepConfig.mode(mockState)

        // Assert
        verifyNoInteractions(mockPaymentService)
    }

    @Test
    fun `resolveNextDestination deletes the journey and returns the default destination when the payment succeeded`() {
        // Arrange
        whenever(mockState.paymentOutcome).thenReturn(PaymentOutcome.SUCCESS)

        // Act
        val result = stepConfig.resolveNextDestination(mockState, defaultDestination)

        // Assert
        verify(mockState).deleteJourney()
        assertEquals(defaultDestination, result)
    }

    @ParameterizedTest
    @EnumSource(value = PaymentOutcome::class, names = ["FAILURE", "IN_PROGRESS"])
    fun `resolveNextDestination keeps the journey and returns the default destination when the payment has not succeeded`(
        paymentOutcome: PaymentOutcome,
    ) {
        // Arrange
        whenever(mockState.paymentOutcome).thenReturn(paymentOutcome)

        // Act
        val result = stepConfig.resolveNextDestination(mockState, defaultDestination)

        // Assert
        verify(mockState, never()).deleteJourney()
        assertEquals(defaultDestination, result)
    }

    private fun stubGovUkPayStatus(status: PaymentStatus) {
        whenever(mockState.journeyId).thenReturn(journeyId)
        whenever(mockState.paymentReference).thenReturn(paymentReference)
        whenever(mockPaymentService.getPropertyRegistrationPaymentStatus(journeyId, paymentReference))
            .thenReturn(PaymentStatusCheckDataModel(paymentId = paymentId, status = status, isCancellable = false))
    }

    companion object {
        @JvmStatic
        fun provideFinalisedPaymentStatusesAndOutcomes() =
            listOf(
                Arguments.of(PaymentStatus.SUCCEEDED, PaymentOutcome.SUCCESS),
                Arguments.of(PaymentStatus.CAPTURABLE, PaymentOutcome.IN_PROGRESS),
                Arguments.of(PaymentStatus.FAILED, PaymentOutcome.FAILURE),
                Arguments.of(PaymentStatus.CANCELLED, PaymentOutcome.FAILURE),
            )
    }
}
