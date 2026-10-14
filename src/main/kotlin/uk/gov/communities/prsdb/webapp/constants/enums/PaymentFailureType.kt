package uk.gov.communities.prsdb.webapp.constants.enums

// Error codes: https://docs.payments.service.gov.uk/api_reference/#errors-caused-by-payment-statuses
enum class PaymentFailureType {
    PAYMENT_METHOD_REJECTED,
    PAYMENT_EXPIRED,
    CANCELLED_BY_USER,
    CANCELLED_BY_SERVICE,
    PAYMENT_PROVIDER_ERROR,
    UNKNOWN,
    ;

    companion object {
        fun fromGovUkPayCode(code: String?): PaymentFailureType =
            when (code) {
                "P0010" -> PAYMENT_METHOD_REJECTED
                "P0020" -> PAYMENT_EXPIRED
                "P0030" -> CANCELLED_BY_USER
                "P0040" -> CANCELLED_BY_SERVICE
                "P0050" -> PAYMENT_PROVIDER_ERROR
                else -> UNKNOWN
            }
    }
}
