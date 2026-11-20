package uk.gov.communities.prsdb.webapp.journeys.propertyReactivation

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebComponent
import uk.gov.communities.prsdb.webapp.constants.enums.LicensingType
import uk.gov.communities.prsdb.webapp.exceptions.NotNullFormModelValueIsNullException.Companion.notNullValue
import uk.gov.communities.prsdb.webapp.journeys.propertyReactivation.ReactivationTaskType.LICENSING
import uk.gov.communities.prsdb.webapp.journeys.propertyReactivation.ReactivationTaskType.TENANCY_DETAILS
import uk.gov.communities.prsdb.webapp.models.dataModels.PropertyReactivationDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.ReactivationLicensingDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.ReactivationTenancyDetailsDataModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.FurnishedStatusFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NewNumberOfPeopleFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NumberOfHouseholdsFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.RentFrequencyFormModel

@PrsdbWebComponent
class PropertyReactivationDataModelFactory {
    fun fromJourneyState(state: PropertyReactivationJourneyState): PropertyReactivationDataModel {
        val requiredTaskTypes = state.requiredTaskTypes
        return PropertyReactivationDataModel(
            licensing = if (LICENSING in requiredTaskTypes) getLicensing(state) else null,
            tenancyDetails = if (TENANCY_DETAILS in requiredTaskTypes) getTenancyDetails(state) else null,
        )
    }

    private fun getLicensing(state: PropertyReactivationJourneyState): ReactivationLicensingDataModel {
        val licensingType = state.licensingTask.getLicensingType()
        check(licensingType != LicensingType.PROVIDE_LATER) { "Licensing cannot be provided later when reactivating a property" }
        return ReactivationLicensingDataModel(licensingType, state.licensingTask.getLicenceNumberOrNull())
    }

    private fun getTenancyDetails(state: PropertyReactivationJourneyState): ReactivationTenancyDetailsDataModel {
        val tenancyDetailsTask = state.tenancyDetailsTask
        val billsIncluded = tenancyDetailsTask.rentIncludesBillsTask.getBillsIncludedOrNull()
        return ReactivationTenancyDetailsDataModel(
            numberOfHouseholds =
                tenancyDetailsTask.householdsAndTenantsTask.households.formModel
                    .notNullValue(NumberOfHouseholdsFormModel::numberOfHouseholds)
                    .toInt(),
            numberOfPeople =
                tenancyDetailsTask.householdsAndTenantsTask.tenants.formModel
                    .notNullValue(NewNumberOfPeopleFormModel::numberOfPeople)
                    .toInt(),
            billsIncludedList = billsIncluded?.standardBillsIncludedListAsString,
            customBillsIncluded = billsIncluded?.customBillsIncluded,
            furnishedStatus = tenancyDetailsTask.furnishedStatus.formModel.notNullValue(FurnishedStatusFormModel::furnishedStatus),
            rentFrequency =
                tenancyDetailsTask.rentFrequencyAndAmountTask.rentFrequency.formModel
                    .notNullValue(RentFrequencyFormModel::rentFrequency),
            customRentFrequency = tenancyDetailsTask.rentFrequencyAndAmountTask.getCustomRentFrequencyIfSelected(),
            rentAmount =
                tenancyDetailsTask.rentFrequencyAndAmountTask.rentAmount.formModel.rentAmount
                    .toBigDecimal(),
        )
    }
}
