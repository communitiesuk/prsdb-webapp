package uk.gov.communities.prsdb.webapp.journeys.shared.payment

import uk.gov.communities.prsdb.webapp.constants.enums.PaymentStatus
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper
import uk.gov.communities.prsdb.webapp.models.dataModels.PaymentCreationResult
import uk.gov.communities.prsdb.webapp.models.dataModels.PaymentStatusCheckDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.PropertyPaymentQuote
import java.time.LocalDate

interface PaymentHandler {
    fun getQuote(quoteDate: LocalDate = LocalDate.now(DateTimeHelper.UK_ZONE)): PropertyPaymentQuote

    fun createPayment(quote: PropertyPaymentQuote): PaymentCreationResult

    fun getStoredPaymentStatus(paymentReference: String): PaymentStatusCheckDataModel

    fun finalisePayment(
        paymentId: String,
        govUkPayStatus: PaymentStatus,
    ): PaymentStatus

    fun isAddressTaken(): Boolean
}
