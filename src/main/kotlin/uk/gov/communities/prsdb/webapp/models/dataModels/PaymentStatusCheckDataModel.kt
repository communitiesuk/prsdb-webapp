package uk.gov.communities.prsdb.webapp.models.dataModels

import uk.gov.communities.prsdb.webapp.constants.enums.PaymentFailureType
import uk.gov.communities.prsdb.webapp.constants.enums.PaymentStatus

data class PaymentStatusCheckDataModel(
    val paymentId: String,
    val status: PaymentStatus,
    val isCancellable: Boolean,
    val failureType: PaymentFailureType? = null,
) {
    fun isCreated() = status == PaymentStatus.CREATED

    fun isCapturable() = status == PaymentStatus.CAPTURABLE

    fun isInProgress() = status in PaymentStatus.IN_PROGRESS_STATUSES

    fun isSucceeded() = status == PaymentStatus.SUCCEEDED

    fun isFailedOrCancelled() = status.isFailedOrCancelled()
}
