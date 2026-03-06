package uk.gov.communities.prsdb.webapp.testHelpers.builders

import uk.gov.communities.prsdb.webapp.constants.enums.BillsIncluded
import uk.gov.communities.prsdb.webapp.constants.enums.FurnishedStatus
import uk.gov.communities.prsdb.webapp.constants.enums.RentFrequency
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.BedroomsStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.BillsIncludedStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.FurnishedStatusStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HouseholdStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.OccupiedStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.RentAmountStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.RentFrequencyStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.RentIncludesBillsStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.TenantsStep
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.BillsIncludedFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.FormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.FurnishedStatusFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NumberOfBedroomsFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NumberOfHouseholdsFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NumberOfPeopleFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.OccupancyFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.RentAmountFormModel
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
        submittedValueMap.remove(HouseholdStep.ROUTE_SEGMENT)
        submittedValueMap.remove(TenantsStep.ROUTE_SEGMENT)
        submittedValueMap.remove(BedroomsStep.ROUTE_SEGMENT)
        submittedValueMap.remove(RentIncludesBillsStep.ROUTE_SEGMENT)
        submittedValueMap.remove(BillsIncludedStep.ROUTE_SEGMENT)
        submittedValueMap.remove(FurnishedStatusStep.ROUTE_SEGMENT)
        submittedValueMap.remove(RentFrequencyStep.ROUTE_SEGMENT)
        submittedValueMap.remove(RentAmountStep.ROUTE_SEGMENT)
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
        withSubmittedValue(OccupiedStep.ROUTE_SEGMENT, occupancyFormModel)
        return self()
    }

    fun withHouseholds(households: Int = 2): SelfType {
        val numberOfHouseholdsFormModel =
            NumberOfHouseholdsFormModel().apply {
                numberOfHouseholds = households.toString()
            }
        withSubmittedValue(HouseholdStep.ROUTE_SEGMENT, numberOfHouseholdsFormModel)
        return self()
    }

    fun withPeople(people: Int = 4): SelfType {
        val numberOfPeopleFormModel =
            NumberOfPeopleFormModel().apply {
                numberOfPeople = people.toString()
            }
        withSubmittedValue(TenantsStep.ROUTE_SEGMENT, numberOfPeopleFormModel)
        return self()
    }

    fun withBedrooms(bedrooms: Int = 3): SelfType {
        val numberOfBedroomsFormModel =
            NumberOfBedroomsFormModel().apply {
                numberOfBedrooms = bedrooms.toString()
            }
        withSubmittedValue(BedroomsStep.ROUTE_SEGMENT, numberOfBedroomsFormModel)
        return self()
    }

    fun withRentIncludesBills(rentIncludesBills: Boolean = true): SelfType {
        val rentIncludesBillsFormModel =
            RentIncludesBillsFormModel().apply {
                this.rentIncludesBills = rentIncludesBills
            }
        withSubmittedValue(RentIncludesBillsStep.ROUTE_SEGMENT, rentIncludesBillsFormModel)
        return self()
    }

    fun withBillsIncluded(billsIncluded: MutableList<String?> = mutableListOf(BillsIncluded.ELECTRICITY.toString())): SelfType {
        val billsIncludedFormModel =
            BillsIncludedFormModel().apply {
                this.billsIncluded = billsIncluded
            }
        withSubmittedValue(BillsIncludedStep.ROUTE_SEGMENT, billsIncludedFormModel)
        return self()
    }

    fun withFurnished(furnishedStatus: FurnishedStatus = FurnishedStatus.FURNISHED): SelfType {
        val furnishedStatusFormModel =
            FurnishedStatusFormModel().apply {
                this.furnishedStatus = furnishedStatus
            }
        withSubmittedValue(FurnishedStatusStep.ROUTE_SEGMENT, furnishedStatusFormModel)
        return self()
    }

    fun withRentFrequency(rentFrequency: RentFrequency = RentFrequency.MONTHLY): SelfType {
        val rentFrequencyFormModel =
            RentFrequencyFormModel().apply {
                this.rentFrequency = rentFrequency
            }
        withSubmittedValue(RentFrequencyStep.ROUTE_SEGMENT, rentFrequencyFormModel)
        return self()
    }

    fun withRentAmount(rentAmount: String = "400"): SelfType {
        val rentAmountFormModel =
            RentAmountFormModel().apply {
                this.rentAmount = rentAmount
            }
        withSubmittedValue(RentAmountStep.ROUTE_SEGMENT, rentAmountFormModel)
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
        rentAmount: String = "400",
    ): SelfType {
        withOccupancyStatus(true)
        withHouseholds(households)
        withPeople(people)
        withBedrooms(bedrooms)
        withRentIncludesBills(includesBills)
        withBillsIncluded(billsIncluded)
        withFurnished(furnishedStatus)
        withRentFrequency(rentFrequency)
        withRentAmount(rentAmount)
        @Suppress("UNCHECKED_CAST")
        return self()
    }
}
