package uk.gov.communities.prsdb.webapp.journeys.shared.payment

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
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.Mock
import org.mockito.Mockito.lenient
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.enums.PaymentStatus
import uk.gov.communities.prsdb.webapp.exceptions.CyaDataHasChangedException
import uk.gov.communities.prsdb.webapp.exceptions.PaymentQuoteExpiredException
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.InternalStep
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.shared.states.PaymentState
import uk.gov.communities.prsdb.webapp.models.dataModels.PaymentStatusCheckDataModel
import uk.gov.communities.prsdb.webapp.services.PaymentReconciliationService
import uk.gov.communities.prsdb.webapp.services.PaymentService
import kotlin.test.assertIs

@ExtendWith(MockitoExtension::class)
class PaymentStatusCheckStepConfigTests {
    @Mock
    private lateinit var mockPaymentService: PaymentService

    @Mock
    private lateinit var mockPaymentReconciliationService: PaymentReconciliationService

    @Mock
    private lateinit var mockState: PaymentState

    @Mock
    private lateinit var mockDependencies: PaymentDependencies

    @Mock
    private lateinit var mockPaymentHandler: PaymentHandler

    private lateinit var stepConfig: PaymentStatusCheckStepConfig

    private val journeyId = "journey-123"
    private val paymentId = "payment-123"
    private val paymentReference = "payment-reference-123"
    private val defaultDestination = Destination.ExternalUrl("default")

    @BeforeEach
    fun setUp() {
        stepConfig = PaymentStatusCheckStepConfig(mockPaymentService, mockPaymentReconciliationService)
        lenient().`when`(mockState.dependencies).thenReturn(mockDependencies)
        lenient().`when`(mockDependencies.paymentHandler).thenReturn(mockPaymentHandler)
    }

    @Test
    fun `afterStepIsReached does not finalise a payment that is still in progress on GOV UK Pay`() {
        // Arrange
        stubGovUkPayStatus(PaymentStatus.CREATED)

        // Act
        stepConfig.afterStepIsReached(mockState)

        // Assert
        verify(mockState).paymentOutcome = PaymentOutcome.IN_PROGRESS
        verify(mockPaymentHandler, never()).finalisePayment(any(), any())
    }

    @ParameterizedTest
    @MethodSource("provideNonCreatedPaymentStatusesAndOutcomes")
    fun `afterStepIsReached stores the outcome for a payment no longer stored as created without asking GOV UK Pay`(
        storedPaymentStatus: PaymentStatus,
        expectedOutcome: PaymentOutcome,
    ) {
        // Arrange
        stubStoredPaymentStatus(storedPaymentStatus)

        // Act
        stepConfig.afterStepIsReached(mockState)

        // Assert
        verify(mockState).paymentOutcome = expectedOutcome
        verify(mockPaymentService, never()).getGovUkPayPaymentStatus(any())
        verify(mockPaymentHandler, never()).finalisePayment(any(), any())
        verifyNoInteractions(mockPaymentReconciliationService)
    }

    @ParameterizedTest
    @EnumSource(value = PaymentStatus::class, names = ["CAPTURABLE", "SUCCEEDED", "FAILED", "CANCELLED"])
    fun `afterStepIsReached finalises the payment with the GOV UK Pay status through the payment handler`(govUkPayStatus: PaymentStatus) {
        // Arrange
        stubGovUkPayStatus(govUkPayStatus)
        whenever(mockPaymentHandler.finalisePayment(paymentId, govUkPayStatus)).thenReturn(PaymentStatus.SUCCEEDED)

        // Act
        stepConfig.afterStepIsReached(mockState)

        // Assert
        verify(mockPaymentHandler).finalisePayment(paymentId, govUkPayStatus)
    }

    @Test
    fun `afterStepIsReached stores the outcome for the finalised payment status rather than the GOV UK Pay status`() {
        // Arrange
        stubFinalisedPaymentStatus(PaymentStatus.CANCELLING)

        // Act
        stepConfig.afterStepIsReached(mockState)

        // Assert
        verify(mockState).paymentOutcome = PaymentOutcome.NOT_COMPLETED
    }

    @Test
    fun `afterStepIsReached stores a quote expired outcome and clears the payment reference when the quote has expired`() {
        // Arrange
        stubGovUkPayStatus(PaymentStatus.CAPTURABLE)
        whenever(mockPaymentHandler.finalisePayment(paymentId, PaymentStatus.CAPTURABLE))
            .thenThrow(PaymentQuoteExpiredException("Quote expired"))

        // Act
        stepConfig.afterStepIsReached(mockState)

        // Assert
        verify(mockState).paymentOutcome = PaymentOutcome.QUOTE_EXPIRED
        verify(mockState).paymentReference = null
        verify(mockPaymentHandler, never()).isPropertyAlreadyRegistered()
    }

    @Test
    fun `afterStepIsReached throws when the finalised payment is still stored as capturable`() {
        // Arrange
        stubFinalisedPaymentStatus(PaymentStatus.CAPTURABLE)

        // Act & Assert
        assertThrows<IllegalStateException> { stepConfig.afterStepIsReached(mockState) }
    }

    @ParameterizedTest
    @EnumSource(value = PaymentStatus::class, names = ["FAILED", "CANCELLING", "CANCELLED", "MANUAL_INTERVENTION"])
    fun `afterStepIsReached marks the property as already registered when the payment did not succeed and the address is now registered`(
        finalisedPaymentStatus: PaymentStatus,
    ) {
        // Arrange
        stubFinalisedPaymentStatus(finalisedPaymentStatus)
        whenever(mockPaymentHandler.isPropertyAlreadyRegistered()).thenReturn(true)

        // Act
        stepConfig.afterStepIsReached(mockState)

        // Assert
        verify(mockDependencies).isPropertyAlreadyRegistered = true
    }

    @Test
    fun `afterStepIsReached marks the property as not already registered when the payment failed and the address is available`() {
        // Arrange
        stubFinalisedPaymentStatus(PaymentStatus.FAILED)
        whenever(mockPaymentHandler.isPropertyAlreadyRegistered()).thenReturn(false)

        // Act
        stepConfig.afterStepIsReached(mockState)

        // Assert
        verify(mockDependencies).isPropertyAlreadyRegistered = false
    }

    @ParameterizedTest
    @EnumSource(value = PaymentStatus::class, names = ["SUCCEEDED", "CREATED"])
    fun `afterStepIsReached does not check whether the property is already registered when the payment has not failed`(
        finalisedPaymentStatus: PaymentStatus,
    ) {
        // Arrange
        stubFinalisedPaymentStatus(finalisedPaymentStatus)

        // Act
        stepConfig.afterStepIsReached(mockState)

        // Assert
        verify(mockPaymentHandler, never()).isPropertyAlreadyRegistered()
        verify(mockDependencies, never()).isPropertyAlreadyRegistered = anyOrNull()
    }

    @Test
    fun `afterStepIsReached does not check whether the property is already registered when the payment is in progress on GOV UK Pay`() {
        // Arrange
        stubGovUkPayStatus(PaymentStatus.CREATED)

        // Act
        stepConfig.afterStepIsReached(mockState)

        // Assert
        verify(mockPaymentHandler, never()).isPropertyAlreadyRegistered()
        verify(mockDependencies, never()).isPropertyAlreadyRegistered = anyOrNull()
    }

    @Test
    fun `afterStepIsReached checks the status of the payment the user has returned from`() {
        // Arrange
        stubGovUkPayStatus(PaymentStatus.CREATED)

        // Act
        stepConfig.afterStepIsReached(mockState)

        // Assert
        verify(mockPaymentHandler).getStoredPaymentStatus(paymentReference)
        verify(mockPaymentService).getGovUkPayPaymentStatus(paymentId)
    }

    @Test
    fun `afterStepIsReached throws without checking any payment when the journey has no payment reference`() {
        // Arrange
        whenever(mockState.journeyId).thenReturn(journeyId)
        whenever(mockState.paymentReference).thenReturn(null)

        // Act & Assert
        assertThrows<IllegalStateException> { stepConfig.afterStepIsReached(mockState) }
        verifyNoInteractions(mockPaymentService, mockPaymentHandler)
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
        verifyNoInteractions(mockPaymentService, mockPaymentHandler)
    }

    @Test
    fun `resolveNextDestination on success resolves the next URL before deleting the journey`() {
        // Arrange
        whenever(mockState.paymentOutcome).thenReturn(PaymentOutcome.SUCCESS)
        val defaultDestination = mock<Destination.ExternalUrl>()
        whenever(defaultDestination.toUrlStringOrNull()).thenReturn("/confirmation")

        // Act
        val destination = stepConfig.resolveNextDestination(mockState, defaultDestination)

        // Assert
        val inOrder = inOrder(defaultDestination, mockState)
        inOrder.verify(defaultDestination).toUrlStringOrNull()
        inOrder.verify(mockState).deleteJourney()
        assertEquals("/confirmation", assertIs<Destination.ExternalUrl>(destination).externalUrl)
    }

    @Test
    fun `resolveNextDestination on success uses the exit step's next destination without checking whether it is reachable`() {
        // Arrange
        whenever(mockState.paymentOutcome).thenReturn(PaymentOutcome.SUCCESS)
        val mockExitStep = mock<InternalStep<*, *>>()
        lenient().`when`(mockExitStep.isStepReachable).thenReturn(false)
        whenever(mockExitStep.getNextDestination()).thenReturn(Destination.ExternalUrl("/confirmation"))

        // Act
        val destination = stepConfig.resolveNextDestination(mockState, Destination.NavigationalStep(mockExitStep))

        // Assert
        assertEquals("/confirmation", assertIs<Destination.ExternalUrl>(destination).externalUrl)
        verify(mockState).deleteJourney()
    }

    @Test
    fun `resolveNextDestination on success deletes the journey even when there is no next destination`() {
        // Arrange
        whenever(mockState.paymentOutcome).thenReturn(PaymentOutcome.SUCCESS)
        whenever(mockState.journeyId).thenReturn(journeyId)

        // Act & Assert
        assertThrows<IllegalStateException> { stepConfig.resolveNextDestination(mockState, Destination.Nowhere()) }
        verify(mockState).deleteJourney()
    }

    @ParameterizedTest
    @EnumSource(value = PaymentOutcome::class, names = ["FAILURE", "NOT_COMPLETED", "IN_PROGRESS", "QUOTE_EXPIRED"])
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

    @ParameterizedTest
    @NullSource
    @ValueSource(booleans = [false])
    fun `resolveNextDestination returns the default destination when the payment failed and the property is not already registered`(
        isPropertyAlreadyRegistered: Boolean?,
    ) {
        // Arrange
        whenever(mockState.paymentOutcome).thenReturn(PaymentOutcome.FAILURE)
        whenever(mockDependencies.isPropertyAlreadyRegistered).thenReturn(isPropertyAlreadyRegistered)

        // Act
        val result = stepConfig.resolveNextDestination(mockState, defaultDestination)

        // Assert
        assertEquals(defaultDestination, result)
    }

    @ParameterizedTest
    @EnumSource(value = PaymentOutcome::class, names = ["FAILURE", "NOT_COMPLETED"])
    fun `resolveNextDestination redirects to the already registered step when the payment was unsuccessful and the address is registered`(
        paymentOutcome: PaymentOutcome,
    ) {
        // Arrange
        whenever(mockState.paymentOutcome).thenReturn(paymentOutcome)
        whenever(mockDependencies.isPropertyAlreadyRegistered).thenReturn(true)
        val mockAlreadyRegisteredStep = mock<RequestableStep<*, *, *>>()
        whenever(mockAlreadyRegisteredStep.currentJourneyId).thenReturn(journeyId)
        whenever(mockDependencies.alreadyRegisteredStep).thenReturn(mockAlreadyRegisteredStep)

        // Act
        val result = stepConfig.resolveNextDestination(mockState, defaultDestination)

        // Assert
        assertEquals(mockAlreadyRegisteredStep, assertIs<Destination.VisitableStep>(result).step)
        verify(mockState, never()).deleteJourney()
    }

    @ParameterizedTest
    @EnumSource(value = PaymentOutcome::class, names = ["SUCCESS", "IN_PROGRESS", "QUOTE_EXPIRED"])
    fun `resolveNextDestination ignores the already registered flag when the payment has not failed`(paymentOutcome: PaymentOutcome) {
        // Arrange
        whenever(mockState.paymentOutcome).thenReturn(paymentOutcome)
        lenient().`when`(mockDependencies.isPropertyAlreadyRegistered).thenReturn(true)

        // Act
        val result = stepConfig.resolveNextDestination(mockState, defaultDestination)

        // Assert
        assertEquals(defaultDestination.toUrlStringOrNull(), result.toUrlStringOrNull())
    }

    @ParameterizedTest
    @EnumSource(value = PaymentStatus::class, names = ["CANCELLING", "CANCELLED"])
    fun `afterStepIsReached marks a capturable payment for cancellation and throws when answers changed since they were confirmed`(
        storedStatus: PaymentStatus,
    ) {
        // Arrange
        stubGovUkPayStatus(PaymentStatus.CAPTURABLE)
        stubAnswersChangedSinceConfirmation()
        whenever(mockPaymentReconciliationService.markForCancellation(paymentId)).thenReturn(storedStatus)

        // Act & Assert
        assertThrows<CyaDataHasChangedException> { stepConfig.afterStepIsReached(mockState) }
        verify(mockPaymentReconciliationService).markForCancellation(paymentId)
        verify(mockPaymentHandler, never()).finalisePayment(any(), any())
    }

    @Test
    fun `afterStepIsReached reports success when the payment succeeded before it could be marked for cancellation`() {
        // Arrange
        stubGovUkPayStatus(PaymentStatus.CAPTURABLE)
        stubAnswersChangedSinceConfirmation()
        whenever(mockPaymentReconciliationService.markForCancellation(paymentId)).thenReturn(PaymentStatus.SUCCEEDED)

        // Act
        stepConfig.afterStepIsReached(mockState)

        // Assert
        verify(mockState).paymentOutcome = PaymentOutcome.SUCCESS
        verify(mockPaymentHandler, never()).finalisePayment(any(), any())
    }

    @Test
    fun `afterStepIsReached finalises a capturable payment when answers are unchanged since they were confirmed`() {
        // Arrange
        stubFinalisedPaymentStatus(PaymentStatus.SUCCEEDED)
        whenever(mockDependencies.answersChangedSinceConfirmation).thenReturn(false)

        // Act
        stepConfig.afterStepIsReached(mockState)

        // Assert
        verifyNoInteractions(mockPaymentReconciliationService)
        verify(mockPaymentHandler).finalisePayment(paymentId, PaymentStatus.CAPTURABLE)
    }

    private fun stubAnswersChangedSinceConfirmation() {
        whenever(mockDependencies.answersChangedSinceConfirmation).thenReturn(true)
    }

    private fun stubStoredPaymentStatus(status: PaymentStatus) {
        whenever(mockState.paymentReference).thenReturn(paymentReference)
        whenever(mockPaymentHandler.getStoredPaymentStatus(paymentReference))
            .thenReturn(PaymentStatusCheckDataModel(paymentId = paymentId, status = status))
    }

    private fun stubGovUkPayStatus(status: PaymentStatus) {
        stubStoredPaymentStatus(PaymentStatus.CREATED)
        whenever(mockPaymentService.getGovUkPayPaymentStatus(paymentId))
            .thenReturn(PaymentStatusCheckDataModel(paymentId = paymentId, status = status))
    }

    private fun stubFinalisedPaymentStatus(finalisedPaymentStatus: PaymentStatus) {
        stubGovUkPayStatus(PaymentStatus.CAPTURABLE)
        whenever(mockPaymentHandler.finalisePayment(paymentId, PaymentStatus.CAPTURABLE)).thenReturn(finalisedPaymentStatus)
    }

    companion object {
        @JvmStatic
        fun provideNonCreatedPaymentStatusesAndOutcomes() =
            listOf(
                Arguments.of(PaymentStatus.SUCCEEDED, PaymentOutcome.SUCCESS),
                Arguments.of(PaymentStatus.FAILED, PaymentOutcome.FAILURE),
                Arguments.of(PaymentStatus.CANCELLING, PaymentOutcome.NOT_COMPLETED),
                Arguments.of(PaymentStatus.CANCELLED, PaymentOutcome.NOT_COMPLETED),
                Arguments.of(PaymentStatus.MANUAL_INTERVENTION, PaymentOutcome.NOT_COMPLETED),
            )
    }
}
