package uk.gov.communities.prsdb.webapp.models.dataModels

import uk.gov.communities.prsdb.webapp.constants.enums.PaymentCheckOutcome

data class PaymentStatusCheckDataModel(
    val paymentId: String,
    val outcome: PaymentCheckOutcome,
) {
    fun isInProgress() = outcome == PaymentCheckOutcome.IN_PROGRESS

    fun isCapturable() = outcome == PaymentCheckOutcome.CAPTURABLE

    fun isCaptured() = outcome == PaymentCheckOutcome.CAPTURED

    fun isFailed() = outcome == PaymentCheckOutcome.FAILED
}
