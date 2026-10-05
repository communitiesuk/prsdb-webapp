package uk.gov.communities.prsdb.webapp.constants.enums

import uk.gov.communities.prsdb.webapp.models.dataModels.govUkPay.GovUkPayPaymentStatus

enum class PaymentStatus {
    CREATED,
    CAPTURABLE,
    SUCCEEDED,
    FAILED, // Includes payments cancelled by the user
    CANCELLED, // Only payments cancelled by our service
    ;

    fun isFailedOrCancelled() = this == FAILED || this == CANCELLED

    companion object {
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
