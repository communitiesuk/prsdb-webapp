package uk.gov.communities.prsdb.webapp.enums

import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.MethodSource
import uk.gov.communities.prsdb.webapp.constants.enums.PaymentStatus
import uk.gov.communities.prsdb.webapp.models.dataModels.govUkPay.GovUkPayPaymentStatus
import kotlin.test.assertEquals

class PaymentStatusTests {
    @ParameterizedTest
    @MethodSource("provideFinishedGovUkPayStatuses")
    fun `fromGovUkPayStatusWhenCancelling maps a finished GOV UK Pay status to the matching payment status`(
        govUkPayStatus: GovUkPayPaymentStatus,
        expectedStatus: PaymentStatus,
    ) {
        assertEquals(expectedStatus, PaymentStatus.fromGovUkPayStatusWhenCancelling(govUkPayStatus))
    }

    @ParameterizedTest
    @EnumSource(value = GovUkPayPaymentStatus::class, names = ["CREATED", "STARTED", "SUBMITTED", "CAPTURABLE"])
    fun `fromGovUkPayStatusWhenCancelling throws for a GOV UK Pay status that is not finished`(govUkPayStatus: GovUkPayPaymentStatus) {
        assertThrows<IllegalArgumentException> { PaymentStatus.fromGovUkPayStatusWhenCancelling(govUkPayStatus) }
    }

    companion object {
        @JvmStatic
        fun provideFinishedGovUkPayStatuses() =
            listOf(
                Arguments.of(GovUkPayPaymentStatus.SUCCESS, PaymentStatus.SUCCEEDED),
                Arguments.of(GovUkPayPaymentStatus.FAILED, PaymentStatus.FAILED),
                Arguments.of(GovUkPayPaymentStatus.ERROR, PaymentStatus.FAILED),
                Arguments.of(GovUkPayPaymentStatus.CANCELLED, PaymentStatus.CANCELLED),
            )
    }
}
