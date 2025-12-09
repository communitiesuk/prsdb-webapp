package uk.gov.communities.prsdb.webapp.testHelpers.builders

import uk.gov.communities.prsdb.webapp.forms.steps.RegisterPropertyStepId
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.FormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NumberOfHouseholdsFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NumberOfPeopleFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.OccupancyFormModel

interface OccupancyStateBuilder<SelfType : OccupancyStateBuilder<SelfType>> {
    val submittedValueMap: MutableMap<String, FormModel>

    fun withSubmittedValue(
        key: String,
        value: FormModel,
    ): SelfType

    fun self(): SelfType

    fun withNoTenants(): SelfType {
        submittedValueMap.remove("number-of-households")
        submittedValueMap.remove("number-of-people")
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

    fun withTenants(
        households: Int = 2,
        people: Int = 4,
    ): SelfType {
        withOccupancyStatus(true)
        withHouseholds(households)
        val numberOfPeopleFormModel =
            NumberOfPeopleFormModel().apply {
                numberOfPeople = people.toString()
            }
        withSubmittedValue(RegisterPropertyStepId.NumberOfPeople.urlPathSegment, numberOfPeopleFormModel)
        @Suppress("UNCHECKED_CAST")
        return self()
    }
}
