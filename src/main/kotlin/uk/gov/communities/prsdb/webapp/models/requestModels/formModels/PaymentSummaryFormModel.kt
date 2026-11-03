package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import java.time.LocalDate
import java.time.format.DateTimeParseException

class PaymentSummaryFormModel : FormModel {
    var quoteDate: String? = null

    fun getQuoteDateOrNull(): LocalDate? =
        try {
            quoteDate?.let { LocalDate.parse(it) }
        } catch (exception: DateTimeParseException) {
            null
        }
}
