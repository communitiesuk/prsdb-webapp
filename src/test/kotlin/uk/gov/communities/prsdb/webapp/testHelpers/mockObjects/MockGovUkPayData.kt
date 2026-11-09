package uk.gov.communities.prsdb.webapp.testHelpers.mockObjects

import org.springframework.http.HttpStatus
import uk.gov.communities.prsdb.webapp.exceptions.GovUkPayException
import uk.gov.communities.prsdb.webapp.models.dataModels.govUkPay.GovUkPayCreatedPayment
import uk.gov.communities.prsdb.webapp.models.dataModels.govUkPay.GovUkPayLink
import uk.gov.communities.prsdb.webapp.models.dataModels.govUkPay.GovUkPayPayment
import uk.gov.communities.prsdb.webapp.models.dataModels.govUkPay.GovUkPayPaymentLinks
import uk.gov.communities.prsdb.webapp.models.dataModels.govUkPay.GovUkPayPaymentState
import uk.gov.communities.prsdb.webapp.models.dataModels.govUkPay.GovUkPayPaymentStatus
import java.time.Instant

class MockGovUkPayData {
    companion object {
        const val DEFAULT_PAYMENT_ID = "payment-id"

        const val DEFAULT_NEXT_URL = "https://pay.example.test/next"

        fun createGovUkPayPayment(
            paymentId: String = DEFAULT_PAYMENT_ID,
            status: GovUkPayPaymentStatus = GovUkPayPaymentStatus.CREATED,
            amount: Int = 1000,
            reference: String = "reference",
            description: String = "Register your rental property",
            createdDate: Instant = Instant.now(),
            nextUrl: String? = null,
            code: String? = null,
        ): GovUkPayPayment {
            val finished = status in GovUkPayPaymentStatus.FINISHED_STATUSES
            return GovUkPayPayment(
                paymentId = paymentId,
                amount = amount,
                reference = reference,
                description = description,
                createdDate = createdDate,
                state = GovUkPayPaymentState(status = status, finished = finished, code = code),
                links = GovUkPayPaymentLinks(nextUrl = nextUrl?.let { GovUkPayLink(href = it) }),
            )
        }

        fun createGovUkPayCreatedPayment(
            paymentId: String = DEFAULT_PAYMENT_ID,
            amount: Int = 1000,
            reference: String = "reference",
            createdDate: Instant = Instant.now(),
            nextUrl: String = DEFAULT_NEXT_URL,
        ) = GovUkPayCreatedPayment(
            payment =
                createGovUkPayPayment(
                    paymentId = paymentId,
                    amount = amount,
                    reference = reference,
                    createdDate = createdDate,
                    nextUrl = nextUrl,
                ),
            nextUrl = nextUrl,
        )

        fun createGovUkPayUnavailableException() =
            GovUkPayException(HttpStatus.SERVICE_UNAVAILABLE, "P0999", "GOV.UK Pay is unavailable", RuntimeException())

        fun createGovUkPayNotCapturableException() =
            GovUkPayException(HttpStatus.CONFLICT, "P1003", "GOV.UK Pay could not capture this payment", RuntimeException())
    }
}
