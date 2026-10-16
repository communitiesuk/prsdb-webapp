package uk.gov.communities.prsdb.webapp.models.viewModels.emailModels

import uk.gov.communities.prsdb.webapp.helpers.PaymentHelper
import java.time.Instant

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
            "payment created at" to paymentCreatedAt.toString(),
            "reason" to reason,
        )
}
