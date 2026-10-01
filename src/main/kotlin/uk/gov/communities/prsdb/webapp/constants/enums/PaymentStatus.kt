package uk.gov.communities.prsdb.webapp.constants.enums

import uk.gov.communities.prsdb.webapp.models.dataModels.govUkPay.GovUkPayPaymentStatus

enum class PaymentStatus {
    CREATED,
    CAPTURABLE,
    SUCCEEDED,
    FAILED,
    CANCELLED,
    ;

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

        fun fromGovUkPayStatusWhenCancelling(govUkPayStatus: GovUkPayPaymentStatus): PaymentStatus =
            when (govUkPayStatus) {
                GovUkPayPaymentStatus.SUCCESS -> SUCCEEDED
                GovUkPayPaymentStatus.FAILED, GovUkPayPaymentStatus.ERROR -> FAILED
                GovUkPayPaymentStatus.CANCELLED -> CANCELLED
                GovUkPayPaymentStatus.CREATED,
                GovUkPayPaymentStatus.STARTED,
                GovUkPayPaymentStatus.SUBMITTED,
                GovUkPayPaymentStatus.CAPTURABLE,
                    -> throw IllegalArgumentException("GOV.UK Pay status $govUkPayStatus is not a finished status")
            }
    }
}
