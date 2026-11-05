package uk.gov.communities.prsdb.webapp.services

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.EnumSource
import org.mockito.Mock
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.eq
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import uk.gov.communities.prsdb.webapp.clients.GovUkPayClient
import uk.gov.communities.prsdb.webapp.constants.enums.PaymentManualInterventionReason
import uk.gov.communities.prsdb.webapp.constants.enums.PaymentStatus
import uk.gov.communities.prsdb.webapp.database.entity.Payment
import uk.gov.communities.prsdb.webapp.database.repository.PaymentRepository
import uk.gov.communities.prsdb.webapp.exceptions.GovUkPayException
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.PaymentRequiresManualInterventionEmail
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockPaymentData.Companion.createPayment
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class PaymentReconciliationServiceTests {
    @Mock
    private lateinit var mockPaymentRepository: PaymentRepository

    @Mock
    private lateinit var mockGovUkPayClient: GovUkPayClient

    @Mock
    private lateinit var mockManualInterventionEmailSender: EmailNotificationService<PaymentRequiresManualInterventionEmail>

    private lateinit var paymentReconciliationService: PaymentReconciliationService

    @BeforeEach
    fun setUp() {
        paymentReconciliationService =
            PaymentReconciliationService(
                MANUAL_INTERVENTION_EMAIL_ADDRESS,
                mockPaymentRepository,
                mockGovUkPayClient,
                mockManualInterventionEmailSender,
            )
    }

    @Nested
    inner class MarkForCancellation {
        @Test
        fun `marks a created payment for cancellation then cancels it with GOV UK Pay`() {
            // Arrange
            stubConditionalUpdate(PaymentStatus.CREATED, PaymentStatus.CANCELLING, rowsUpdated = 1)
            stubConditionalUpdate(PaymentStatus.CANCELLING, PaymentStatus.CANCELLED, rowsUpdated = 1)
            whenever(mockPaymentRepository.findStatusByPaymentId(PAYMENT_ID)).thenReturn(PaymentStatus.CANCELLED)

            // Act
            val result = paymentReconciliationService.markForCancellation(PAYMENT_ID)

            // Assert
            assertEquals(PaymentStatus.CANCELLED, result)
            val inOrder = inOrder(mockPaymentRepository, mockGovUkPayClient)
            inOrder
                .verify(mockPaymentRepository)
                .updateStatusIfCurrentStatusIs(eq(PAYMENT_ID), eq(PaymentStatus.CREATED), eq(PaymentStatus.CANCELLING), any())
            inOrder.verify(mockGovUkPayClient).cancelPayment(PAYMENT_ID)
            inOrder
                .verify(mockPaymentRepository)
                .updateStatusIfCurrentStatusIs(eq(PAYMENT_ID), eq(PaymentStatus.CANCELLING), eq(PaymentStatus.CANCELLED), any())
        }

        @Test
        fun `leaves the payment cancelling when GOV UK Pay does not cancel it`() {
            // Arrange
            stubConditionalUpdate(PaymentStatus.CREATED, PaymentStatus.CANCELLING, rowsUpdated = 1)
            doThrow(GovUkPayException(HttpStatus.BAD_REQUEST, "P0501", "Cancellation of payment failed", RuntimeException()))
                .whenever(mockGovUkPayClient)
                .cancelPayment(PAYMENT_ID)
            whenever(mockPaymentRepository.findStatusByPaymentId(PAYMENT_ID)).thenReturn(PaymentStatus.CANCELLING)

            // Act
            val result = paymentReconciliationService.markForCancellation(PAYMENT_ID)

            // Assert
            assertEquals(PaymentStatus.CANCELLING, result)
            verify(mockPaymentRepository, never())
                .updateStatusIfCurrentStatusIs(any(), eq(PaymentStatus.CANCELLING), any(), any())
        }

        @ParameterizedTest
        @EnumSource(PaymentStatus::class, names = ["CREATED"], mode = EnumSource.Mode.EXCLUDE)
        fun `returns the stored status without cancelling when the payment is no longer created`(storedStatus: PaymentStatus) {
            // Arrange
            stubConditionalUpdate(PaymentStatus.CREATED, PaymentStatus.CANCELLING, rowsUpdated = 0)
            whenever(mockPaymentRepository.findStatusByPaymentId(PAYMENT_ID)).thenReturn(storedStatus)

            // Act
            val result = paymentReconciliationService.markForCancellation(PAYMENT_ID)

            // Assert
            assertEquals(storedStatus, result)
            verify(mockGovUkPayClient, never()).cancelPayment(any())
        }

        @Test
        fun `throws when the payment does not exist`() {
            // Arrange
            stubConditionalUpdate(PaymentStatus.CREATED, PaymentStatus.CANCELLING, rowsUpdated = 0)
            whenever(mockPaymentRepository.findStatusByPaymentId(PAYMENT_ID)).thenReturn(null)

            // Act & Assert
            assertThrows<IllegalStateException> { paymentReconciliationService.markForCancellation(PAYMENT_ID) }
        }
    }

    @Nested
    inner class RecordManualIntervention {
        @Test
        fun `records manual intervention then emails support when the payment has the expected status`() {
            // Arrange
            val payment = createPayment(paymentId = PAYMENT_ID)
            stubConditionalUpdate(PaymentStatus.CREATED, PaymentStatus.MANUAL_INTERVENTION, rowsUpdated = 1)
            whenever(mockPaymentRepository.findById(PAYMENT_ID)).thenReturn(Optional.of(payment))
            whenever(mockPaymentRepository.findStatusByPaymentId(PAYMENT_ID)).thenReturn(PaymentStatus.MANUAL_INTERVENTION)

            // Act
            val result = paymentReconciliationService.recordManualIntervention(PAYMENT_ID, REASON, PaymentStatus.CREATED)

            // Assert
            assertEquals(PaymentStatus.MANUAL_INTERVENTION, result)
            verify(mockManualInterventionEmailSender).sendEmail(
                MANUAL_INTERVENTION_EMAIL_ADDRESS,
                PaymentRequiresManualInterventionEmail(
                    payment.paymentId,
                    payment.reference,
                    payment.amountInPence,
                    payment.paymentCreatedAt,
                    REASON.description,
                ),
            )
        }

        @Test
        fun `returns the stored status without emailing support when the payment no longer has the expected status`() {
            // Arrange
            stubConditionalUpdate(PaymentStatus.CREATED, PaymentStatus.MANUAL_INTERVENTION, rowsUpdated = 0)
            whenever(mockPaymentRepository.findStatusByPaymentId(PAYMENT_ID)).thenReturn(PaymentStatus.SUCCEEDED)

            // Act
            val result = paymentReconciliationService.recordManualIntervention(PAYMENT_ID, REASON, PaymentStatus.CREATED)

            // Assert
            assertEquals(PaymentStatus.SUCCEEDED, result)
            verifyNoInteractions(mockManualInterventionEmailSender)
        }
    }

    @Nested
    inner class ReconcilePayments {
        @Test
        fun `marks created payments older than two hours for cancellation`() {
            // Arrange
            val abandonedPayment = createPayment(paymentId = PAYMENT_ID, paymentCreatedAt = NOW.minus(3, ChronoUnit.HOURS))
            whenever(mockPaymentRepository.findAllByStatusAndPaymentCreatedAtBefore(PaymentStatus.CREATED, NOW.minus(2, ChronoUnit.HOURS)))
                .thenReturn(listOf(abandonedPayment))
            stubConditionalUpdate(PaymentStatus.CREATED, PaymentStatus.CANCELLING, rowsUpdated = 1)
            stubConditionalUpdate(PaymentStatus.CANCELLING, PaymentStatus.CANCELLED, rowsUpdated = 1)
            whenever(mockPaymentRepository.findStatusByPaymentId(PAYMENT_ID)).thenReturn(PaymentStatus.CANCELLED)

            // Act
            val failureCount = paymentReconciliationService.reconcilePayments(NOW)

            // Assert
            assertEquals(0, failureCount)
            verify(mockGovUkPayClient).cancelPayment(PAYMENT_ID)
        }

        @Test
        fun `records a cancelling payment as cancelled when GOV UK Pay cancels it`() {
            // Arrange
            stubCancellingPayments(createCancellingPayment())

            // Act
            val failureCount = paymentReconciliationService.reconcilePayments(NOW)

            // Assert
            assertEquals(0, failureCount)
            verify(mockPaymentRepository)
                .updateStatusIfCurrentStatusIs(eq(PAYMENT_ID), eq(PaymentStatus.CANCELLING), eq(PaymentStatus.CANCELLED), any())
            verify(mockGovUkPayClient, never()).getPaymentWithRetry(any())
        }

        @ParameterizedTest
        @CsvSource("FAILED, FAILED", "CANCELLED, CANCELLED")
        fun `records the GOV UK Pay status when GOV UK Pay does not cancel a payment that has failed or been cancelled`(
            govUkPayStatus: GovUkPayPaymentStatus,
            expectedStatus: PaymentStatus,
        ) {
            // Arrange
            stubCancellingPayments(createCancellingPayment())
            stubGovUkPayDoesNotCancel()
            whenever(mockGovUkPayClient.getPaymentWithRetry(PAYMENT_ID)).thenReturn(createGovUkPayPayment(PAYMENT_ID, govUkPayStatus))

            // Act
            val failureCount = paymentReconciliationService.reconcilePayments(NOW)

            // Assert
            assertEquals(0, failureCount)
            verify(mockPaymentRepository)
                .updateStatusIfCurrentStatusIs(eq(PAYMENT_ID), eq(PaymentStatus.CANCELLING), eq(expectedStatus), any())
        }

        @Test
        fun `records manual intervention when GOV UK Pay took the payment after cancellation was requested`() {
            // Arrange
            val payment = createCancellingPayment()
            stubGovUkPayTookCancellingPayment(payment)

            // Act
            val failureCount = paymentReconciliationService.reconcilePayments(NOW)

            // Assert
            assertEquals(0, failureCount)
            verify(mockManualInterventionEmailSender).sendEmail(
                MANUAL_INTERVENTION_EMAIL_ADDRESS,
                PaymentRequiresManualInterventionEmail(
                    payment.paymentId,
                    payment.reference,
                    payment.amountInPence,
                    payment.paymentCreatedAt,
                    PaymentManualInterventionReason.TAKEN_AFTER_CANCELLATION_REQUESTED.description,
                ),
            )
        }

        @Test
        fun `counts a failure when the support email for a payment taken after cancellation was requested fails`() {
            // Arrange
            stubGovUkPayTookCancellingPayment(createCancellingPayment())
            doThrow(RuntimeException()).whenever(mockManualInterventionEmailSender).sendEmail(any(), any())

            // Act
            val failureCount = paymentReconciliationService.reconcilePayments(NOW)

            // Assert
            assertEquals(1, failureCount)
            verify(mockPaymentRepository)
                .updateStatusIfCurrentStatusIs(eq(PAYMENT_ID), eq(PaymentStatus.CANCELLING), eq(PaymentStatus.MANUAL_INTERVENTION), any())
        }

        @ParameterizedTest
        @EnumSource(GovUkPayPaymentStatus::class, names = ["CREATED", "STARTED", "SUBMITTED", "CAPTURABLE"])
        fun `leaves the payment cancelling when GOV UK Pay has not finished with it`(govUkPayStatus: GovUkPayPaymentStatus) {
            // Arrange
            stubCancellingPayments(createCancellingPayment())
            stubGovUkPayDoesNotCancel()
            whenever(mockGovUkPayClient.getPaymentWithRetry(PAYMENT_ID)).thenReturn(createGovUkPayPayment(PAYMENT_ID, govUkPayStatus))

            // Act
            val failureCount = paymentReconciliationService.reconcilePayments(NOW)

            // Assert
            assertEquals(0, failureCount)
            verify(mockPaymentRepository, never()).updateStatusIfCurrentStatusIs(any(), any(), any(), any())
        }

        @ParameterizedTest
        @CsvSource("120, 0", "122, 1")
        fun `leaves an unreadable payment cancelling and counts it as a failure once GOV UK Pay should have expired it`(
            ageInHours: Long,
            expectedFailureCount: Int,
        ) {
            // Arrange
            stubCancellingPayments(createCancellingPayment(paymentCreatedAt = NOW.minus(ageInHours, ChronoUnit.HOURS)))
            stubGovUkPayDoesNotCancel()
            whenever(mockGovUkPayClient.getPaymentWithRetry(PAYMENT_ID))
                .thenThrow(GovUkPayException(HttpStatus.NOT_FOUND, "P0200", "Not found", RuntimeException()))

            // Act
            val failureCount = paymentReconciliationService.reconcilePayments(NOW)

            // Assert
            assertEquals(expectedFailureCount, failureCount)
            verify(mockPaymentRepository, never()).updateStatusIfCurrentStatusIs(any(), any(), any(), any())
        }

        @Test
        fun `counts an exception on a payment as a failure and carries on with the other payments`() {
            // Arrange
            val abandonedPayment = createPayment(paymentId = "abandoned-payment-id", paymentCreatedAt = NOW.minus(3, ChronoUnit.HOURS))
            whenever(mockPaymentRepository.findAllByStatusAndPaymentCreatedAtBefore(eq(PaymentStatus.CREATED), any()))
                .thenReturn(listOf(abandonedPayment))
            stubCancellingPayments(createCancellingPayment(paymentId = "failing-payment-id"), createCancellingPayment())
            whenever(mockPaymentRepository.updateStatusIfCurrentStatusIs(eq("abandoned-payment-id"), any(), any(), any()))
                .thenThrow(RuntimeException())
            whenever(mockPaymentRepository.updateStatusIfCurrentStatusIs(eq("failing-payment-id"), any(), any(), any()))
                .thenThrow(RuntimeException())
            stubConditionalUpdate(PaymentStatus.CANCELLING, PaymentStatus.CANCELLED, rowsUpdated = 1)

            // Act
            val failureCount = paymentReconciliationService.reconcilePayments(NOW)

            // Assert
            assertEquals(2, failureCount)
            verify(mockPaymentRepository)
                .updateStatusIfCurrentStatusIs(eq(PAYMENT_ID), eq(PaymentStatus.CANCELLING), eq(PaymentStatus.CANCELLED), any())
        }

        private fun createCancellingPayment(
            paymentId: String = PAYMENT_ID,
            paymentCreatedAt: Instant = NOW.minus(1, ChronoUnit.HOURS),
        ) = createPayment(paymentId = paymentId, paymentCreatedAt = paymentCreatedAt, status = PaymentStatus.CANCELLING)

        private fun stubCancellingPayments(vararg payments: Payment) {
            whenever(mockPaymentRepository.findAllByStatus(PaymentStatus.CANCELLING)).thenReturn(payments.toList())
        }

        private fun stubGovUkPayTookCancellingPayment(payment: Payment) {
            stubCancellingPayments(payment)
            stubGovUkPayDoesNotCancel()
            whenever(mockGovUkPayClient.getPaymentWithRetry(PAYMENT_ID))
                .thenReturn(createGovUkPayPayment(PAYMENT_ID, GovUkPayPaymentStatus.SUCCESS))
            stubConditionalUpdate(PaymentStatus.CANCELLING, PaymentStatus.MANUAL_INTERVENTION, rowsUpdated = 1)
            whenever(mockPaymentRepository.findById(PAYMENT_ID)).thenReturn(Optional.of(payment))
        }

        private fun stubGovUkPayDoesNotCancel() {
            doThrow(GovUkPayException(HttpStatus.BAD_REQUEST, "P0501", "Cancellation of payment failed", RuntimeException()))
                .whenever(mockGovUkPayClient)
                .cancelPayment(PAYMENT_ID)
        }
    }

    private fun stubConditionalUpdate(
        expectedStatus: PaymentStatus,
        newStatus: PaymentStatus,
        rowsUpdated: Int,
    ) {
        whenever(mockPaymentRepository.updateStatusIfCurrentStatusIs(eq(PAYMENT_ID), eq(expectedStatus), eq(newStatus), any()))
            .thenReturn(rowsUpdated)
    }

    companion object {
        private const val PAYMENT_ID = "payment-id"
        private const val MANUAL_INTERVENTION_EMAIL_ADDRESS = "payments-support@example.com"
        private val REASON = PaymentManualInterventionReason.CAPTURED_BUT_NOT_REGISTERED
        private val NOW = Instant.parse("2026-05-01T12:00:00Z")
    }
}
