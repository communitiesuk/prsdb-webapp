package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockPaymentData
import java.time.Instant
import java.time.LocalDate
import kotlinx.datetime.LocalDate as KotlinLocalDate

class PaymentSummaryViewModelTests {
    @Test
    fun `from returns the payment, payment date and renewal date rows in order`() {
        // Arrange
        val payment =
            MockPaymentData.createPayment(
                amountInPence = 61,
                paymentCreatedAt = Instant.parse("2026-12-16T10:00:00Z"),
            )
        val renewalDate = LocalDate.of(2027, 12, 16)

        // Act
        val viewModel = PaymentSummaryViewModel.from(payment, renewalDate)

        // Assert
        assertEquals(
            listOf(
                SummaryListRowViewModel("registerProperty.confirmation.paymentSummary.payment", "£0.61"),
                SummaryListRowViewModel("registerProperty.confirmation.paymentSummary.paymentDate", KotlinLocalDate(2026, 12, 16)),
                SummaryListRowViewModel("registerProperty.confirmation.paymentSummary.renewalDate", renewalDate),
            ),
            viewModel.rows,
        )
    }

    @Test
    fun `from uses the UK date for a payment made just after midnight during British Summer Time`() {
        // Arrange
        // 23:30 UTC on 15 June is 00:30 BST on 16 June
        val payment = MockPaymentData.createPayment(paymentCreatedAt = Instant.parse("2026-06-15T23:30:00Z"))

        // Act
        val viewModel = PaymentSummaryViewModel.from(payment, LocalDate.of(2027, 6, 16))

        // Assert
        assertEquals(KotlinLocalDate(2026, 6, 16), viewModel.rows[1].fieldValue)
    }
}
