package uk.gov.communities.prsdb.webapp.services

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.mockito.Mock
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
import uk.gov.communities.prsdb.webapp.constants.enums.PaymentStatus
import uk.gov.communities.prsdb.webapp.database.repository.PaymentRepository
import uk.gov.communities.prsdb.webapp.exceptions.GovUkPayException

@ExtendWith(MockitoExtension::class)
class PaymentReconciliationServiceTests {
    @Mock
    private lateinit var mockPaymentRepository: PaymentRepository

    @Mock
    private lateinit var mockGovUkPayClient: GovUkPayClient

    private lateinit var paymentReconciliationService: PaymentReconciliationService

    @BeforeEach
    fun setUp() {
        paymentReconciliationService = PaymentReconciliationService(mockPaymentRepository, mockGovUkPayClient)
    }

    @Nested
    inner class MarkForCancellation {
        @Test
        fun `marks a created payment for cancellation then cancels it with GOV UK Pay`() {
            // Arrange
            stubConditionalUpdate(listOf(PaymentStatus.CREATED), PaymentStatus.CANCELLING, rowsUpdated = 1)
            stubConditionalUpdate(listOf(PaymentStatus.CANCELLING), PaymentStatus.CANCELLED, rowsUpdated = 1)
            whenever(mockPaymentRepository.findStatusByPaymentId(PAYMENT_ID)).thenReturn(PaymentStatus.CANCELLED)

            // Act
            val result = paymentReconciliationService.markForCancellation(PAYMENT_ID)

            // Assert
            assertEquals(PaymentStatus.CANCELLED, result)
            val inOrder = inOrder(mockPaymentRepository, mockGovUkPayClient)
            inOrder
                .verify(mockPaymentRepository)
                .updateStatusIfCurrentStatusIn(eq(PAYMENT_ID), eq(listOf(PaymentStatus.CREATED)), eq(PaymentStatus.CANCELLING), any())
            inOrder.verify(mockGovUkPayClient).cancelPayment(PAYMENT_ID)
            inOrder
                .verify(mockPaymentRepository)
                .updateStatusIfCurrentStatusIn(eq(PAYMENT_ID), eq(listOf(PaymentStatus.CANCELLING)), eq(PaymentStatus.CANCELLED), any())
        }

        @Test
        fun `leaves the payment cancelling when GOV UK Pay does not cancel it`() {
            // Arrange
            stubConditionalUpdate(listOf(PaymentStatus.CREATED), PaymentStatus.CANCELLING, rowsUpdated = 1)
            doThrow(GovUkPayException(HttpStatus.BAD_REQUEST, "P0501", "Cancellation of payment failed", RuntimeException()))
                .whenever(mockGovUkPayClient)
                .cancelPayment(PAYMENT_ID)
            whenever(mockPaymentRepository.findStatusByPaymentId(PAYMENT_ID)).thenReturn(PaymentStatus.CANCELLING)

            // Act
            val result = paymentReconciliationService.markForCancellation(PAYMENT_ID)

            // Assert
            assertEquals(PaymentStatus.CANCELLING, result)
            verify(mockPaymentRepository, never())
                .updateStatusIfCurrentStatusIn(any(), eq(listOf(PaymentStatus.CANCELLING)), any(), any())
        }

        @ParameterizedTest
        @EnumSource(PaymentStatus::class, names = ["CREATED"], mode = EnumSource.Mode.EXCLUDE)
        fun `returns the stored status without cancelling when the payment is no longer created`(storedStatus: PaymentStatus) {
            // Arrange
            stubConditionalUpdate(listOf(PaymentStatus.CREATED), PaymentStatus.CANCELLING, rowsUpdated = 0)
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
            stubConditionalUpdate(listOf(PaymentStatus.CREATED), PaymentStatus.CANCELLING, rowsUpdated = 0)
            whenever(mockPaymentRepository.findStatusByPaymentId(PAYMENT_ID)).thenReturn(null)

            // Act & Assert
            assertThrows<IllegalStateException> { paymentReconciliationService.markForCancellation(PAYMENT_ID) }
        }
    }

    private fun stubConditionalUpdate(
        expectedStatuses: List<PaymentStatus>,
        newStatus: PaymentStatus,
        rowsUpdated: Int,
    ) {
        whenever(mockPaymentRepository.updateStatusIfCurrentStatusIn(eq(PAYMENT_ID), eq(expectedStatuses), eq(newStatus), any()))
            .thenReturn(rowsUpdated)
    }

    companion object {
        private const val PAYMENT_ID = "payment-id"
    }
}
