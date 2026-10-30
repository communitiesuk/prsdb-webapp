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
                quoteDate = LocalDate.of(2026, 12, 16),
                paymentCreatedAt = Instant.parse("2026-12-17T10:00:00Z"),
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
}
