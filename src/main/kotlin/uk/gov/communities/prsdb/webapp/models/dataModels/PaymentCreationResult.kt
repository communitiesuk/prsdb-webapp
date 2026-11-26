package uk.gov.communities.prsdb.webapp.models.dataModels

sealed interface PaymentCreationResult {
    data class NewPayment(
        val nextUrl: String,
    ) : PaymentCreationResult

    data object ConcurrentPaymentStarted : PaymentCreationResult
}
