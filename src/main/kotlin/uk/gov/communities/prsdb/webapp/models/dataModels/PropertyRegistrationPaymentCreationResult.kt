package uk.gov.communities.prsdb.webapp.models.dataModels

sealed interface PropertyRegistrationPaymentCreationResult {
    data class NewPayment(
        val nextUrl: String,
    ) : PropertyRegistrationPaymentCreationResult

    data object ConcurrentPaymentStarted : PropertyRegistrationPaymentCreationResult
}
