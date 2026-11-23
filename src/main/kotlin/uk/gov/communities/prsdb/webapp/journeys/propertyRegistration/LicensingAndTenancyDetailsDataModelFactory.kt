package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebComponent
import uk.gov.communities.prsdb.webapp.constants.enums.LicensingType
import uk.gov.communities.prsdb.webapp.exceptions.NotNullFormModelValueIsNullException.Companion.notNullValue
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.LicensingState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.TenancyDetailsState
import uk.gov.communities.prsdb.webapp.models.dataModels.LicensingDetailsDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.TenancyDetailsDataModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.FurnishedStatusFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NewNumberOfPeopleFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NumberOfHouseholdsFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.RentFrequencyFormModel

@PrsdbWebComponent
class LicensingAndTenancyDetailsDataModelFactory {
    fun fromLicensingState(state: LicensingState): LicensingDetailsDataModel {
        val licensingType = state.getLicensingType()
        check(licensingType != LicensingType.PROVIDE_LATER) { "Licensing can only be provided later when registering a property" }
        return LicensingDetailsDataModel(licensingType, state.getLicenceNumberOrNull())
    }

    fun fromTenancyDetailsState(state: TenancyDetailsState): TenancyDetailsDataModel {
        val billsIncluded = state.rentIncludesBillsTask.getBillsIncludedOrNull()
        return TenancyDetailsDataModel(
            numberOfHouseholds =
                state.householdsAndTenantsTask.households.formModel
                    .notNullValue(NumberOfHouseholdsFormModel::numberOfHouseholds)
                    .toInt(),
            numberOfPeople =
                state.householdsAndTenantsTask.tenants.formModel
                    .notNullValue(NewNumberOfPeopleFormModel::numberOfPeople)
                    .toInt(),
            billsIncludedList = billsIncluded?.standardBillsIncludedListAsString,
            customBillsIncluded = billsIncluded?.customBillsIncluded,
            furnishedStatus = state.furnishedStatus.formModel.notNullValue(FurnishedStatusFormModel::furnishedStatus),
            rentFrequency =
                state.rentFrequencyAndAmountTask.rentFrequency.formModel
                    .notNullValue(RentFrequencyFormModel::rentFrequency),
            customRentFrequency = state.rentFrequencyAndAmountTask.getCustomRentFrequencyIfSelected(),
            rentAmount =
                state.rentFrequencyAndAmountTask.rentAmount.formModel.rentAmount
                    .toBigDecimal(),
        )
    }
}
