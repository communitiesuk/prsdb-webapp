package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states

import org.springframework.context.MessageSource
import uk.gov.communities.prsdb.webapp.exceptions.NotNullFormModelValueIsNullException.Companion.notNullValue
import uk.gov.communities.prsdb.webapp.helpers.BillsIncludedHelper
import uk.gov.communities.prsdb.webapp.helpers.RentDataHelper
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.BedroomsStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.BillsIncludedStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.FurnishedStatusStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HouseholdStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.OccupiedStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.RentAmountStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.RentFrequencyStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.RentIncludesBillsStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.TenantsStep
import uk.gov.communities.prsdb.webapp.models.dataModels.BillsIncludedDataModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.RentAmountFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.RentFrequencyFormModel

interface OccupationState : JourneyState {
    val occupied: OccupiedStep
    val households: HouseholdStep
    val tenants: TenantsStep
    val bedrooms: BedroomsStep
    val rentIncludesBills: RentIncludesBillsStep
    val billsIncluded: BillsIncludedStep
    val furnishedStatus: FurnishedStatusStep
    val rentFrequency: RentFrequencyStep
    val rentAmount: RentAmountStep
    val occupiedValueToPrePopulate: Boolean?

    fun getBillsIncludedOrNull(): BillsIncludedDataModel? =
        billsIncluded.formModelOrNull?.let { billsIncludedFormModel ->
            BillsIncludedDataModel.fromFormData(
                formModel = billsIncludedFormModel,
            )
        }

    fun getCustomRentFrequencyIfSelected(): String? =
        if (hasCustomRentFrequency()) {
            rentFrequency.formModel.customRentFrequency.replaceFirstChar { it.uppercase() }
        } else {
            null
        }

    fun getRentAmount(messageSource: MessageSource): String =
        RentDataHelper.getRentAmount(
            rentAmount.formModel.notNullValue(RentAmountFormModel::rentAmount),
            rentFrequency.formModel.notNullValue(RentFrequencyFormModel::rentFrequency),
            messageSource,
        )

    fun getBillsIncluded(messageSource: MessageSource): String =
        BillsIncludedHelper.getBillsIncludedForCYAStep(
            getBillsIncludedOrNull()!!,
            messageSource,
        )

    private fun hasCustomRentFrequency(): Boolean =
        RentDataHelper.hasCustomRentFrequency(
            rentFrequency.formModel.notNullValue(RentFrequencyFormModel::rentFrequency),
        )
}
