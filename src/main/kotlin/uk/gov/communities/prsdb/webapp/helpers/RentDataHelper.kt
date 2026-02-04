package uk.gov.communities.prsdb.webapp.helpers

import org.springframework.context.MessageSource
import uk.gov.communities.prsdb.webapp.constants.enums.RentFrequency
import uk.gov.communities.prsdb.webapp.helpers.converters.MessageKeyConverter
import uk.gov.communities.prsdb.webapp.helpers.extensions.MessageSourceExtensions.Companion.getMessageForKey

class RentDataHelper {
    companion object {
        fun getRentAmount(
            rentAmount: String,
            rentFrequency: RentFrequency,
            messageSource: MessageSource,
        ): String {
            var formattedRentAmount = "£$rentAmount"
            if (hasCustomRentFrequency(rentFrequency)) {
                val perMonthSuffix =
                    messageSource.getMessageForKey("forms.checkPropertyAnswers.tenancyDetails.customFrequencyRentAmountSuffix")
                formattedRentAmount += " $perMonthSuffix"
            }
            return formattedRentAmount
        }

        fun getRentFrequency(rentFrequency: RentFrequency, customRentFrequency: String?): String {
            return if (!hasCustomRentFrequency(rentFrequency)) {
                MessageKeyConverter.convert(rentFrequency)
            } else {
                customRentFrequency!!.replaceFirstChar { it.uppercase() }
            }
        }
        
        fun hasCustomRentFrequency(rentFrequency: RentFrequency): Boolean = rentFrequency == RentFrequency.OTHER
    }
}
