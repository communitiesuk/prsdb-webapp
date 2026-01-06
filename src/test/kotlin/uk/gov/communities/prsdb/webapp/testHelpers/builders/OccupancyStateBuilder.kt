package uk.gov.communities.prsdb.webapp.testHelpers.builders

import uk.gov.communities.prsdb.webapp.constants.enums.BillsIncluded
import uk.gov.communities.prsdb.webapp.constants.enums.FurnishedStatus
import uk.gov.communities.prsdb.webapp.constants.enums.RentFrequency
import uk.gov.communities.prsdb.webapp.forms.steps.RegisterPropertyStepId
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.BillsIncludedFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.FormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.FurnishedFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NumberOfBedroomsFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NumberOfHouseholdsFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NumberOfPeopleFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.OccupancyFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.RentFrequencyFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.RentIncludesBillsFormModel

interface OccupancyStateBuilder<SelfType : OccupancyStateBuilder<SelfType>> {
    val submittedValueMap: MutableMap<String, FormModel>

    fun withSubmittedValue(
        key: String,
        value: FormModel,
    ): SelfType

    fun self(): SelfType

    fun withNoTenants(): SelfType {
        submittedValueMap.remove(RegisterPropertyStepId.NumberOfHouseholds.urlPathSegment)
        submittedValueMap.remove(RegisterPropertyStepId.NumberOfPeople.urlPathSegment)
        submittedValueMap.remove(RegisterPropertyStepId.NumberOfBedrooms.urlPathSegment)
        submittedValueMap.remove(RegisterPropertyStepId.RentIncludesBills.urlPathSegment)
        submittedValueMap.remove(RegisterPropertyStepId.BillsIncluded.urlPathSegment)
        submittedValueMap.remove(RegisterPropertyStepId.RentFrequency.urlPathSegment)
        return withOccupiedSetToFalse()
    }

    fun withOccupiedSetToFalse(): SelfType {
        val occupancyFormModel =
            OccupancyFormModel().apply {
                occupied = false
            }
        withSubmittedValue("occupancy", occupancyFormModel)
        return self()
    }

    fun withOccupancyStatus(occupied: Boolean): SelfType {
        val occupancyFormModel =
            OccupancyFormModel().apply {
                this.occupied = occupied
            }
        withSubmittedValue(RegisterPropertyStepId.Occupancy.urlPathSegment, occupancyFormModel)
        return self()
    }

    fun withHouseholds(households: Int = 2): SelfType {
        val numberOfHouseholdsFormModel =
            NumberOfHouseholdsFormModel().apply {
                numberOfHouseholds = households.toString()
            }
        withSubmittedValue(RegisterPropertyStepId.NumberOfHouseholds.urlPathSegment, numberOfHouseholdsFormModel)
        return self()
    }

    fun withPeople(people: Int = 4): SelfType {
        val numberOfPeopleFormModel =
            NumberOfPeopleFormModel().apply {
                numberOfPeople = people.toString()
            }
        withSubmittedValue(RegisterPropertyStepId.NumberOfPeople.urlPathSegment, numberOfPeopleFormModel)
        return self()
    }

    fun withBedrooms(bedrooms: Int = 3): SelfType {
        val numberOfBedroomsFormModel =
            NumberOfBedroomsFormModel().apply {
                numberOfBedrooms = bedrooms.toString()
            }
        withSubmittedValue(RegisterPropertyStepId.NumberOfBedrooms.urlPathSegment, numberOfBedroomsFormModel)
        return self()
    }

    fun withRentIncludesBills(rentIncludesBills: Boolean = true): SelfType {
        val rentIncludesBillsFormModel =
            RentIncludesBillsFormModel().apply {
                this.rentIncludesBills = rentIncludesBills
            }
        withSubmittedValue(RegisterPropertyStepId.RentIncludesBills.urlPathSegment, rentIncludesBillsFormModel)
        return self()
    }

    fun withBillsIncluded(billsIncluded: MutableList<String?> = mutableListOf(BillsIncluded.ELECTRICITY.toString())): SelfType {
        val billsIncludedFormModel =
            BillsIncludedFormModel().apply {
                this.billsIncluded = billsIncluded
            }
        withSubmittedValue(RegisterPropertyStepId.BillsIncluded.urlPathSegment, billsIncludedFormModel)
        return self()
    }

    fun withFurnished(furnishedStatus: FurnishedStatus = FurnishedStatus.FURNISHED): SelfType {
        val furnishedFormModel =
            FurnishedFormModel().apply {
                this.furnishedStatus = furnishedStatus
            }
        withSubmittedValue(RegisterPropertyStepId.PropertyFurnished.urlPathSegment, furnishedFormModel)
        return self()
    }

    fun withRentFrequency(rentFrequency: RentFrequency = RentFrequency.MONTHLY): SelfType {
        val rentFrequencyFormModel =
            RentFrequencyFormModel().apply {
                this.rentFrequency = rentFrequency
            }
        withSubmittedValue(RegisterPropertyStepId.RentFrequency.urlPathSegment, rentFrequencyFormModel)
        return self()
    }

    fun withTenants(
        households: Int = 2,
        people: Int = 4,
        bedrooms: Int = 3,
        includesBills: Boolean = true,
        billsIncluded: MutableList<String?> = mutableListOf(BillsIncluded.ELECTRICITY.toString()),
        furnishedStatus: FurnishedStatus = FurnishedStatus.FURNISHED,
        rentFrequency: RentFrequency = RentFrequency.MONTHLY,
    ): SelfType {
        withOccupancyStatus(true)
        withHouseholds(households)
        withPeople(people)
        withBedrooms(bedrooms)
        withRentIncludesBills(includesBills)
        withBillsIncluded(billsIncluded)
        withFurnished(furnishedStatus)
        withRentFrequency(rentFrequency)
        @Suppress("UNCHECKED_CAST")
        return self()
    }
}
