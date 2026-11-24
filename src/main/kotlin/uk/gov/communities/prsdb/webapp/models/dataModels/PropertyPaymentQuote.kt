package uk.gov.communities.prsdb.webapp.models.dataModels

import kotlinx.serialization.Serializable
import uk.gov.communities.prsdb.webapp.helpers.LocalDateSerializer
import java.time.LocalDate

@Serializable
data class PropertyPaymentQuote(
    val amountInPence: Int,
    @Serializable(with = LocalDateSerializer::class)
    val quoteDate: LocalDate,
    @Serializable(with = LocalDateSerializer::class)
    val renewalDate: LocalDate,
    @Serializable(with = LocalDateSerializer::class)
    val paymentStartDate: LocalDate = quoteDate,
) {
    val isPaymentRequired: Boolean
        get() = amountInPence > 0
}
