package uk.gov.communities.prsdb.webapp.models.viewModels.emailModels

import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper
import uk.gov.communities.prsdb.webapp.helpers.PaymentHelper
import java.time.Instant
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField
import java.util.Locale

data class PaymentRequiresManualInterventionEmail(
    val paymentId: String,
    val paymentReference: String,
    val amountInPence: Int,
    val paymentCreatedAt: Instant,
    val reason: String,
) : EmailTemplateModel {
    override val template = EmailTemplate.PAYMENT_REQUIRES_MANUAL_INTERVENTION_EMAIL

    override fun toHashMap(): HashMap<String, String> =
        hashMapOf(
            "payment id" to paymentId,
            "payment reference" to paymentReference,
            "amount" to PaymentHelper.formatPence(amountInPence),
            "payment created at" to paymentCreatedAtFormatter.format(paymentCreatedAt),
            "reason" to reason,
        )

    companion object {
        private val paymentCreatedAtFormatter =
            DateTimeFormatterBuilder()
                .appendPattern("d MMMM uuuu 'at' h:mm")
                .appendText(ChronoField.AMPM_OF_DAY, mapOf(0L to "am", 1L to "pm"))
                .appendPattern(" (z)")
                .toFormatter(Locale.UK)
                .withZone(DateTimeHelper.UK_ZONE)
    }
}
