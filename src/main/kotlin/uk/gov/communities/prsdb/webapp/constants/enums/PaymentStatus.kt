package uk.gov.communities.prsdb.webapp.constants.enums

import uk.gov.communities.prsdb.webapp.models.dataModels.govUkPay.GovUkPayPaymentStatus

// Stored by ordinal, so changing the order needs a migration
enum class PaymentStatus {
    CREATED,
    CANCELLING, // Only stored by us, never reported by GOV.UK Pay
    SUCCEEDED,
    FAILED, // Includes payments cancelled by the user
    CANCELLED, // Only payments cancelled by our service
    MANUAL_INTERVENTION, // Only stored by us, never reported by GOV.UK Pay
    CAPTURABLE, // Reported by GOV.UK Pay but never stored
    ;

    fun isFailedOrCancelled() = this == FAILED || this == CANCELLED

    companion object {
        val IN_PROGRESS_STATUSES = listOf(CREATED)

        fun fromGovUKPayStatus(status: GovUkPayPaymentStatus): PaymentStatus =
            when (status) {
                GovUkPayPaymentStatus.CREATED -> CREATED
                GovUkPayPaymentStatus.STARTED -> CREATED
                GovUkPayPaymentStatus.SUBMITTED -> CREATED
                GovUkPayPaymentStatus.CAPTURABLE -> CAPTURABLE
                GovUkPayPaymentStatus.SUCCESS -> SUCCEEDED
                GovUkPayPaymentStatus.FAILED -> FAILED
                GovUkPayPaymentStatus.ERROR -> FAILED
                GovUkPayPaymentStatus.CANCELLED -> CANCELLED
            }
    }
}
