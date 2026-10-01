package uk.gov.communities.prsdb.webapp.models.dataModels

import uk.gov.communities.prsdb.webapp.constants.enums.PaymentStatus

data class PaymentStatusCheckDataModel(
    val paymentId: String,
    val status: PaymentStatus,
) {
    fun isInProgress() = status == PaymentStatus.CREATED

    fun isCapturable() = status == PaymentStatus.CAPTURABLE

    fun isCaptured() = status == PaymentStatus.SUCCEEDED

    fun isFailedOrCancelled() = status == PaymentStatus.FAILED || status == PaymentStatus.CANCELLED
}
