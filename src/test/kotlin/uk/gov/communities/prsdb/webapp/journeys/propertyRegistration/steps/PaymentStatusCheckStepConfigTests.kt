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
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.Mock
import org.mockito.Mockito.lenient
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.enums.PaymentStatus
import uk.gov.communities.prsdb.webapp.exceptions.CyaDataHasChangedException
import uk.gov.communities.prsdb.webapp.exceptions.PaymentQuoteExpiredException
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.PropertyRegistrationDataModelFactory
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.PropertyRegistrationJourneyState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.PropertyDetailsTask
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.PropertyRegistrationAddressTask
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.PaymentStatusCheckDataModel
import uk.gov.communities.prsdb.webapp.services.AddressAvailabilityService
import uk.gov.communities.prsdb.webapp.services.PaymentReconciliationService
import uk.gov.communities.prsdb.webapp.services.PaymentService
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockPropertyRegistrationData
import kotlin.test.assertIs

@ExtendWith(MockitoExtension::class)
class PaymentStatusCheckStepConfigTests {
    @Mock
    private lateinit var mockPaymentService: PaymentService

    @Mock
    private lateinit var mockPaymentReconciliationService: PaymentReconciliationService

    @Mock
    private lateinit var mockPropertyRegistrationDataModelFactory: PropertyRegistrationDataModelFactory

    @Mock
    private lateinit var mockAddressAvailabilityService: AddressAvailabilityService

    @Mock
    private lateinit var mockState: PropertyRegistrationJourneyState

    @Mock
    private lateinit var mockPropertyDetailsTask: PropertyDetailsTask

    @Mock
    private lateinit var mockAddressTask: PropertyRegistrationAddressTask

    private lateinit var stepConfig: PaymentStatusCheckStepConfig

    private val journeyId = "journey-123"
    private val paymentId = "payment-123"
    private val paymentReference = "payment-reference-123"
    private val uprn = 123L
    private val registrationData = MockPropertyRegistrationData.createPropertyRegistrationDataModel()
    private val defaultDestination = Destination.ExternalUrl("default")

    @BeforeEach
    fun setUp() {
        stepConfig =
            PaymentStatusCheckStepConfig(
                mockPaymentService,
                mockPaymentReconciliationService,
                mockPropertyRegistrationDataModelFactory,
                mockAddressAvailabilityService,
            )
        lenient().`when`(mockState.propertyDetailsTask).thenReturn(mockPropertyDetailsTask)
        lenient().`when`(mockPropertyDetailsTask.addressTask).thenReturn(mockAddressTask)
        lenient().`when`(mockAddressTask.getAddress()).thenReturn(AddressDataModel("1 Example Road, EG1 2AB", uprn = uprn))
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
        verify(mockPaymentService, never()).finalisePayment(any(), any(), any())
        verifyNoInteractions(mockPaymentReconciliationService)
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
            .thenReturn(PaymentStatus.SUCCEEDED)

        // Act
        stepConfig.afterStepIsReached(mockState)

        // Assert
        verify(mockPaymentService).finalisePayment(paymentId, govUkPayStatus, registrationData)
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
        whenever(mockPropertyRegistrationDataModelFactory.fromJourneyState(mockState)).thenReturn(registrationData)
        whenever(mockPaymentService.finalisePayment(paymentId, PaymentStatus.CAPTURABLE, registrationData))
            .thenThrow(PaymentQuoteExpiredException("Quote expired"))

        // Act
        stepConfig.afterStepIsReached(mockState)

        // Assert
        verify(mockState).paymentOutcome = PaymentOutcome.QUOTE_EXPIRED
        verify(mockState).paymentReference = null
        verifyNoInteractions(mockAddressAvailabilityService)
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
    fun `afterStepIsReached marks the address as already registered when the payment did not succeed and the address has been registered`(
        finalisedPaymentStatus: PaymentStatus,
    ) {
        // Arrange
        stubFinalisedPaymentStatus(finalisedPaymentStatus)
        whenever(mockAddressAvailabilityService.isAddressOwned(uprn)).thenReturn(true)

        // Act
        stepConfig.afterStepIsReached(mockState)

        // Assert
        verify(mockAddressTask).isAddressAlreadyRegistered = true
    }

    @Test
    fun `afterStepIsReached marks the address as not already registered when the payment failed and the address is available`() {
        // Arrange
        stubFinalisedPaymentStatus(PaymentStatus.CANCELLED)
        whenever(mockAddressAvailabilityService.isAddressOwned(uprn)).thenReturn(false)

        // Act
        stepConfig.afterStepIsReached(mockState)

        // Assert
        verify(mockAddressTask).isAddressAlreadyRegistered = false
    }

    @Test
    fun `afterStepIsReached clears the already registered flag without checking registration when the address has no UPRN`() {
        // Arrange
        stubFinalisedPaymentStatus(PaymentStatus.CANCELLED)
        whenever(mockAddressTask.getAddress()).thenReturn(AddressDataModel("1 Example Road, EG1 2AB", uprn = null))

        // Act
        stepConfig.afterStepIsReached(mockState)

        // Assert
        verifyNoInteractions(mockAddressAvailabilityService)
        verify(mockAddressTask).isAddressAlreadyRegistered = null
    }

    @ParameterizedTest
    @EnumSource(value = PaymentStatus::class, names = ["SUCCEEDED", "CREATED"])
    fun `afterStepIsReached does not check whether the address is registered when the payment has not failed`(
        finalisedPaymentStatus: PaymentStatus,
    ) {
        // Arrange
        stubFinalisedPaymentStatus(finalisedPaymentStatus)

        // Act
        stepConfig.afterStepIsReached(mockState)

        // Assert
        verifyNoInteractions(mockAddressAvailabilityService)
        verify(mockAddressTask, never()).isAddressAlreadyRegistered = anyOrNull()
    }

    @Test
    fun `afterStepIsReached does not check whether the address is registered when the payment is still in progress on GOV UK Pay`() {
        // Arrange
        stubGovUkPayStatus(PaymentStatus.CREATED)

        // Act
        stepConfig.afterStepIsReached(mockState)

        // Assert
        verifyNoInteractions(mockAddressAvailabilityService)
        verify(mockAddressTask, never()).isAddressAlreadyRegistered = anyOrNull()
    }

    @Test
    fun `afterStepIsReached checks the status of the payment the user has returned from`() {
        // Arrange
        stubGovUkPayStatus(PaymentStatus.CREATED)

        // Act
        stepConfig.afterStepIsReached(mockState)

        // Assert
        verify(mockPaymentService).getStoredPropertyRegistrationPaymentStatus(journeyId, paymentReference)
        verify(mockPaymentService).getGovUkPayPaymentStatus(paymentId)
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
    fun `resolveNextDestination returns the default destination when the payment failed and the address is not already registered`(
        isAddressAlreadyRegistered: Boolean?,
    ) {
        // Arrange
        whenever(mockState.paymentOutcome).thenReturn(PaymentOutcome.FAILURE)
        whenever(mockAddressTask.isAddressAlreadyRegistered).thenReturn(isAddressAlreadyRegistered)

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
        whenever(mockAddressTask.isAddressAlreadyRegistered).thenReturn(true)
        val mockAlreadyRegisteredStep = mock<AlreadyRegisteredStep>()
        whenever(mockAlreadyRegisteredStep.currentJourneyId).thenReturn(journeyId)
        whenever(mockAddressTask.alreadyRegisteredStep).thenReturn(mockAlreadyRegisteredStep)

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
        lenient().`when`(mockAddressTask.isAddressAlreadyRegistered).thenReturn(true)

        // Act
        val result = stepConfig.resolveNextDestination(mockState, defaultDestination)

        // Assert
        assertEquals(defaultDestination, result)
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
        verify(mockPaymentService, never()).finalisePayment(any(), any(), any())
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
        verify(mockPaymentService, never()).finalisePayment(any(), any(), any())
    }

    @Test
    fun `afterStepIsReached finalises a capturable payment when answers are unchanged since they were confirmed`() {
        // Arrange
        stubFinalisedPaymentStatus(PaymentStatus.SUCCEEDED)
        whenever(mockState.answersChangedSinceConfirmation).thenReturn(false)

        // Act
        stepConfig.afterStepIsReached(mockState)

        // Assert
        verifyNoInteractions(mockPaymentReconciliationService)
        verify(mockPaymentService).finalisePayment(paymentId, PaymentStatus.CAPTURABLE, registrationData)
    }

    private fun stubAnswersChangedSinceConfirmation() {
        whenever(mockState.answersChangedSinceConfirmation).thenReturn(true)
    }

    private fun stubStoredPaymentStatus(status: PaymentStatus) {
        whenever(mockState.journeyId).thenReturn(journeyId)
        whenever(mockState.paymentReference).thenReturn(paymentReference)
        whenever(mockPaymentService.getStoredPropertyRegistrationPaymentStatus(journeyId, paymentReference))
            .thenReturn(PaymentStatusCheckDataModel(paymentId = paymentId, status = status))
    }

    private fun stubGovUkPayStatus(status: PaymentStatus) {
        stubStoredPaymentStatus(PaymentStatus.CREATED)
        whenever(mockPaymentService.getGovUkPayPaymentStatus(paymentId))
            .thenReturn(PaymentStatusCheckDataModel(paymentId = paymentId, status = status))
    }

    private fun stubFinalisedPaymentStatus(finalisedPaymentStatus: PaymentStatus) {
        stubGovUkPayStatus(PaymentStatus.CAPTURABLE)
        whenever(mockPropertyRegistrationDataModelFactory.fromJourneyState(mockState)).thenReturn(registrationData)
        whenever(mockPaymentService.finalisePayment(paymentId, PaymentStatus.CAPTURABLE, registrationData))
            .thenReturn(finalisedPaymentStatus)
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
