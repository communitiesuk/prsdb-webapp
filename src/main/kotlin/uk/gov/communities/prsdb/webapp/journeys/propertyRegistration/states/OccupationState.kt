package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states

import uk.gov.communities.prsdb.webapp.constants.enums.RentFrequency
import uk.gov.communities.prsdb.webapp.exceptions.NotNullFormModelValueIsNullException
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
import uk.gov.communities.prsdb.webapp.models.dataModels.RentAmountDataModel

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

    fun getBillsIncluded(): BillsIncludedDataModel? = BillsIncludedDataModel.fromFormDataOrNull()

    fun getRentAmount(): RentAmountDataModel =
        RentAmountDataModel.fromFormDataOrNull() ?: throw NotNullFormModelValueIsNullException("No rent amount found in OccupationState")

    fun getCustomRentFrequencyIfSelected(): String? =
        if (rentFrequency.formModelOrNull?.rentFrequency == RentFrequency.OTHER) {
            rentFrequency.formModelOrNull?.customRentFrequency
        } else {
            null
        }

    private fun BillsIncludedDataModel.Companion.fromFormDataOrNull() =
        billsIncluded.formModelOrNull?.let { billsIncludedFormModel ->
            BillsIncludedDataModel.fromFormData(
                billsIncluded = billsIncludedFormModel.billsIncluded,
                customBillsIncluded = billsIncludedFormModel.customBillsIncluded,
            )
        }

    private fun RentAmountDataModel.Companion.fromFormDataOrNull(): RentAmountDataModel? =
        rentAmount.formModelOrNull?.let { rentAmountFormModel ->
            RentAmountDataModel.fromFormData(
                rentAmount = rentAmountFormModel.rentAmount,
                isCustomRentFrequency = rentFrequency.formModelOrNull?.rentFrequency == RentFrequency.OTHER,
            )
        }
}
