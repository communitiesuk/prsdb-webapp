package uk.gov.communities.prsdb.webapp.helpers

import org.springframework.context.MessageSource
import uk.gov.communities.prsdb.webapp.helpers.extensions.MessageSourceExtensions.Companion.getMessageForKey

class RentAmountHelper {
    companion object {
        fun getRentAmount(
            rentAmount: String,
            hasCustomRentFrequency: Boolean,
            messageSource: MessageSource,
        ): String {
            var formattedRentAmount = "£$rentAmount"
            if (hasCustomRentFrequency) {
                val perMonthSuffix =
                    messageSource.getMessageForKey("forms.checkPropertyAnswers.tenancyDetails.customFrequencyRentAmountSuffix")
                formattedRentAmount += " $perMonthSuffix"
            }
            return formattedRentAmount
        }
    }
}
