package uk.gov.communities.prsdb.webapp.models.dataModels

import java.io.Serializable
import java.time.LocalDate

data class PropertyRegistrationPaymentQuote(
    val amountInPence: Int,
    val quoteDate: LocalDate,
    val renewalDate: LocalDate,
) : Serializable
