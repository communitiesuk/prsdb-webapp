package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels

import kotlinx.datetime.toKotlinInstant
import uk.gov.communities.prsdb.webapp.database.entity.Payment
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper
import uk.gov.communities.prsdb.webapp.helpers.extensions.addRow
import java.math.BigDecimal
import java.text.NumberFormat
import java.time.LocalDate
import java.util.Locale

data class PaymentSummaryViewModel(
    val rows: List<SummaryListRowViewModel>,
) {
    companion object {
        fun from(
            payment: Payment,
            renewalDate: LocalDate,
        ): PaymentSummaryViewModel =
            PaymentSummaryViewModel(
                mutableListOf<SummaryListRowViewModel>().apply {
                    addRow("registerProperty.confirmation.paymentSummary.payment", formatPence(payment.amountInPence))
                    addRow(
                        "registerProperty.confirmation.paymentSummary.paymentDate",
                        DateTimeHelper.getDateInUK(payment.paymentCreatedAt.toKotlinInstant()),
                    )
                    addRow("registerProperty.confirmation.paymentSummary.renewalDate", renewalDate)
                },
            )

        private fun formatPence(amountInPence: Int): String =
            NumberFormat.getCurrencyInstance(Locale.UK).format(BigDecimal.valueOf(amountInPence.toLong(), 2))
    }
}
