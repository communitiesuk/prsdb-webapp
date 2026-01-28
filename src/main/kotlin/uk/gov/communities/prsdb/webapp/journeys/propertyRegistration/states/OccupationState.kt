package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states

import uk.gov.communities.prsdb.webapp.constants.enums.BillsIncluded
import uk.gov.communities.prsdb.webapp.constants.enums.RentFrequency
import uk.gov.communities.prsdb.webapp.helpers.converters.MessageKeyConverter
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
                formModel = billsIncludedFormModel,
            )
        }

    fun getCustomRentFrequencyIfSelected(): String? =
        if (isRentFrequencyCustom()) {
            rentFrequency.formModel.customRentFrequency.replaceFirstChar { it.uppercase() }
        } else {
            null
        }

    fun getFormattedRentAmountComponents(): List<String>? {
        val rentAmount = rentAmount.formModelOrNull?.rentAmount ?: return null
        val formattedRentAmount = mutableListOf("commonText.poundSign", rentAmount)
        if (isRentFrequencyCustom()) {
            formattedRentAmount.addAll(
                listOf(" ", "forms.checkPropertyAnswers.tenancyDetails.customFrequencyRentAmountSuffix"),
            )
        }
        return formattedRentAmount
    }

    fun getFormattedBillsIncludedListComponents(): List<String>? {
        val billsIncludedDataModel = getBillsIncludedOrNull()!!
        return billsIncludedDataModel.standardBillsIncludedListAsEnums.map { bill ->
            if (bill != BillsIncluded.SOMETHING_ELSE) {
                MessageKeyConverter.convert(bill)
            } else {
                billsIncludedDataModel.customBillsIncluded!!.replaceFirstChar { it.uppercase() }
            }
        }
    }

    private fun isRentFrequencyCustom(): Boolean = rentFrequency.formModelOrNull?.rentFrequency == RentFrequency.OTHER
}
