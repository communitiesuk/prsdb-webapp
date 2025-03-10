package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class NumberOfPeopleFormModelTests {
    @Test
    fun `numberOfPeople is invalid if it is lower than numberOfHouseholds`() {
        val numberOfPeopleFormModel = NumberOfPeopleFormModel(numberOfPeople = "1", numberOfHouseholds = "2")
        assertFalse(numberOfPeopleFormModel.isNotLessThanNumberOfHouseholds())
    }

    @Test
    fun `numberOfPeople is valid if it is the same as the numberOfHouseholds`() {
        val numberOfPeopleFormModel = NumberOfPeopleFormModel(numberOfPeople = "2", numberOfHouseholds = "2")
        assertTrue(numberOfPeopleFormModel.isNotLessThanNumberOfHouseholds())
    }

    @Test
    fun `numberOfPeople is valid if it is higher than the numberOfHouseholds`() {
        val numberOfPeopleFormModel = NumberOfPeopleFormModel(numberOfPeople = "3", numberOfHouseholds = "2")
        assertTrue(numberOfPeopleFormModel.isNotLessThanNumberOfHouseholds())
    }
}
