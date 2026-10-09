package uk.gov.communities.prsdb.webapp.models.dataModels

import kotlinx.serialization.Serializable
import uk.gov.communities.prsdb.webapp.helpers.LocalDateSerializer
import java.time.LocalDate

@Serializable
data class PropertyRegistrationPaymentQuote(
    val amountInPence: Int,
    @Serializable(with = LocalDateSerializer::class)
    val quoteDate: LocalDate,
    @Serializable(with = LocalDateSerializer::class)
    val renewalDate: LocalDate,
)
