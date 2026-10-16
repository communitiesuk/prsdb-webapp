package uk.gov.communities.prsdb.webapp.constants.enums

import uk.gov.communities.prsdb.webapp.models.dataModels.govUkPay.GovUkPayPaymentStatus

// Stored by ordinal, so new entries must only be appended
enum class PaymentStatus {
    CREATED,
    INDETERMINATE,
    SUCCEEDED,
    FAILED, // Includes payments cancelled by the user
    CANCELLED, // Only payments cancelled by our service
    CAPTURABLE, // Only reported by GOV.UK Pay, never stored
    MANUAL_INTERVENTION, // Never reported by GOV.UK Pay; support must check whether the payment was taken
    ;

    fun isFailedOrCancelled() = this == FAILED || this == CANCELLED

    companion object {
        val IN_PROGRESS_STATUSES = listOf(CREATED, INDETERMINATE, CAPTURABLE)

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
