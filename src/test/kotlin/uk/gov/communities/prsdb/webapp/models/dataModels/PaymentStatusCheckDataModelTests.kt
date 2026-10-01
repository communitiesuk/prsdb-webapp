package uk.gov.communities.prsdb.webapp.models.dataModels

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import uk.gov.communities.prsdb.webapp.constants.enums.PaymentStatus

class PaymentStatusCheckDataModelTests {
    @ParameterizedTest
    @EnumSource(PaymentStatus::class)
    fun `helper methods return true only for the matching status`(status: PaymentStatus) {
        // Act
        val paymentStatusCheck = PaymentStatusCheckDataModel(PAYMENT_ID, status)

        // Assert
        assertEquals(status == PaymentStatus.CREATED, paymentStatusCheck.isCreated())
        assertEquals(status == PaymentStatus.CAPTURABLE, paymentStatusCheck.isCapturable())
        assertEquals(status == PaymentStatus.CREATED || status == PaymentStatus.CAPTURABLE, paymentStatusCheck.isInProgress())
        assertEquals(status == PaymentStatus.SUCCEEDED, paymentStatusCheck.isSucceeded())
        assertEquals(status == PaymentStatus.FAILED || status == PaymentStatus.CANCELLED, paymentStatusCheck.isFailedOrCancelled())
    }

    companion object {
        private const val PAYMENT_ID = "payment-id"
    }
}
