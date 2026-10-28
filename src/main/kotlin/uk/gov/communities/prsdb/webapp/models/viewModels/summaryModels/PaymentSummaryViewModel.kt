package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels

import kotlinx.datetime.toKotlinLocalDate
import uk.gov.communities.prsdb.webapp.database.entity.Payment
import uk.gov.communities.prsdb.webapp.helpers.PaymentHelper
import uk.gov.communities.prsdb.webapp.helpers.extensions.addRow
import java.time.LocalDate

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
                    addRow("registerProperty.confirmation.paymentSummary.payment", PaymentHelper.formatPence(payment.amountInPence))
                    addRow("registerProperty.confirmation.paymentSummary.paymentDate", payment.quoteDate.toKotlinLocalDate())
                    addRow("registerProperty.confirmation.paymentSummary.renewalDate", renewalDate)
                },
            )
    }
}
