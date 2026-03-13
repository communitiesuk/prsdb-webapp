package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states

import org.springframework.context.MessageSource
import uk.gov.communities.prsdb.webapp.exceptions.NotNullFormModelValueIsNullException.Companion.notNullValue
import uk.gov.communities.prsdb.webapp.helpers.RentDataHelper
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.RentAmountStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.RentFrequencyStep
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.RentAmountFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.RentFrequencyFormModel

interface RentFrequencyAndAmountState : JourneyState {
    val rentFrequency: RentFrequencyStep
    val rentAmount: RentAmountStep

    fun getRentAmount(messageSource: MessageSource): String =
        RentDataHelper.getRentAmount(
            rentAmount.formModel.notNullValue(RentAmountFormModel::rentAmount),
            rentFrequency.formModel.notNullValue(RentFrequencyFormModel::rentFrequency),
            messageSource,
        )

    fun getCustomRentFrequencyIfSelected(): String? =
        if (hasCustomRentFrequency()) {
            rentFrequency.formModel.customRentFrequency.replaceFirstChar { it.uppercase() }
        } else {
            null
        }

    private fun hasCustomRentFrequency(): Boolean =
        RentDataHelper.hasCustomRentFrequency(
            rentFrequency.formModel.notNullValue(RentFrequencyFormModel::rentFrequency),
        )
}
