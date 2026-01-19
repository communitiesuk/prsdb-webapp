package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states

import uk.gov.communities.prsdb.webapp.constants.enums.BillsIncluded
import uk.gov.communities.prsdb.webapp.constants.enums.RentFrequency
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

    fun getBillsIncludedOrNull(): BillsIncludedDataModel? =
        billsIncluded.formModelOrNull?.let { billsIncludedFormModel ->
            BillsIncludedDataModel.fromFormData(
                billsIncluded = billsIncludedFormModel.billsIncluded,
                customBillsIncluded = billsIncludedFormModel.customBillsIncluded,
            )
        }

    fun getRentAmountOrNull(): RentAmountDataModel? =
        rentAmount.formModelOrNull?.let { rentAmountFormModel ->
            RentAmountDataModel.fromFormData(
                rentAmount = rentAmountFormModel.rentAmount,
            )
        }

    fun getCustomRentFrequencyIfSelected(): String? =
        if (isRentFrequencyCustom()) {
            rentFrequency.formModelOrNull?.customRentFrequency
        } else {
            null
        }

    fun getFormattedRentAmountOrNull(): List<String>? {
        val rentAmountDataModel = getRentAmountOrNull() ?: return null
        val formattedRentAmount = mutableListOf("commonText.poundSign", "${rentAmountDataModel.rentAmount} ")
        if (isRentFrequencyCustom()) formattedRentAmount.add("forms.checkPropertyAnswers.tenancyDetails.customFrequencyRentAmountSuffix")
        return formattedRentAmount
    }

    fun getFormattedAllBillsIncludedListOrNull(): List<Any>? {
        val allBillsIncludedList: MutableList<Any?> = mutableListOf()
        val billsIncludedDataModel = getBillsIncludedOrNull() ?: return null
        billsIncludedDataModel.standardBillsIncludedListAsEnums.forEach { bill ->
            if (bill != BillsIncluded.SOMETHING_ELSE) {
                allBillsIncludedList.add(bill)
                allBillsIncludedList.add(", ")
            }
        }
        if (billsIncludedDataModel.customBillsIncludedIfRequired != null) {
            allBillsIncludedList.add(billsIncludedDataModel.customBillsIncludedIfRequired)
        } else {
            allBillsIncludedList.removeLast()
        }
        return allBillsIncludedList.filterNotNull().ifEmpty { null }
    }

    private fun isRentFrequencyCustom(): Boolean = rentFrequency.formModelOrNull?.rentFrequency == RentFrequency.OTHER
}
