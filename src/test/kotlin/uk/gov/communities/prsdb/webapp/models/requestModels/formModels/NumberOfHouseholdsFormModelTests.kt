package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.constants.PROVIDE_THIS_LATER_BUTTON_ACTION_NAME

class NumberOfHouseholdsFormModelTests {
    @Test
    fun `number of households is valid when provide this later action is selected`() {
        val formModel =
            NumberOfHouseholdsFormModel().apply {
                action = PROVIDE_THIS_LATER_BUTTON_ACTION_NAME
                numberOfHouseholds = ""
            }

        assertTrue(formModel.numberOfHouseholdsIsValidForAction())
    }

    @Test
    fun `number of households is invalid when no action is selected and the value is blank`() {
        val formModel =
            NumberOfHouseholdsFormModel().apply {
                action = null
                numberOfHouseholds = ""
            }

        assertFalse(formModel.numberOfHouseholdsIsValidForAction())
    }
}
