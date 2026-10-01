package uk.gov.communities.prsdb.webapp.models.dataModels

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import uk.gov.communities.prsdb.webapp.constants.enums.PaymentCheckOutcome

class PaymentStatusCheckDataModelTests {
    @ParameterizedTest
    @EnumSource(PaymentCheckOutcome::class)
    fun `helper methods return true only for the matching outcome`(outcome: PaymentCheckOutcome) {
        // Act
        val paymentStatusCheck = PaymentStatusCheckDataModel(PAYMENT_ID, outcome)

        // Assert
        assertEquals(outcome == PaymentCheckOutcome.IN_PROGRESS, paymentStatusCheck.isInProgress())
        assertEquals(outcome == PaymentCheckOutcome.CAPTURABLE, paymentStatusCheck.isCapturable())
        assertEquals(outcome == PaymentCheckOutcome.CAPTURED, paymentStatusCheck.isCaptured())
        assertEquals(outcome == PaymentCheckOutcome.FAILED, paymentStatusCheck.isFailed())
    }

    companion object {
        private const val PAYMENT_ID = "payment-id"
    }
}
